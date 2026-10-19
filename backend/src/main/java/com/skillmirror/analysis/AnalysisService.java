package com.skillmirror.analysis;

import com.skillmirror.ai.AiService;
import com.skillmirror.ai.DemoIntelligence;
import com.skillmirror.ai.dto.AiDtos.AnalysisText;
import com.skillmirror.common.ApiException;
import com.skillmirror.job.JobOffer;
import com.skillmirror.job.JobOfferRepository;
import com.skillmirror.job.JobService;
import com.skillmirror.job.JobSkill;
import com.skillmirror.analysis.dto.AnalysisDtos.AnalysisDetailDto;
import com.skillmirror.analysis.dto.AnalysisDtos.AnalysisResultDto;
import com.skillmirror.analysis.dto.AnalysisDtos.AnalysisSummaryDto;
import com.skillmirror.analysis.dto.AnalysisDtos.Counts;
import com.skillmirror.analysis.dto.AnalysisDtos.GapDto;
import com.skillmirror.skill.SkillLevel;
import com.skillmirror.skill.SkillStatus;
import com.skillmirror.skill.UserSkill;
import com.skillmirror.skill.UserSkillRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Career Gap Engine — the analytical core of SkillMirror.
 *
 * <p>Score = Σ(weight × mastery) / Σ(weight) × 100, computed exclusively on the backend.
 * Mastery rules (explicit, never inferred from silence):
 * <ul>
 *   <li>missing skill → 0.00 (never assumed acquired)</li>
 *   <li>declared but level unknown → 0.15 (insufficiently documented)</li>
 *   <li>level ≥ required: demonstrated 1.00 / evaluated 0.90 / declared 0.75</li>
 *   <li>one level below required → 0.50 (0.60 when demonstrated)</li>
 *   <li>two or more levels below → 0.25 (0.35 when demonstrated)</li>
 * </ul>
 * The score is an internal compatibility indicator, never a hiring probability.
 */
@Service
public class AnalysisService {

    private final JobOfferRepository jobRepository;
    private final JobService jobService;
    private final UserSkillRepository userSkillRepository;
    private final AnalysisRepository analysisRepository;
    private final AnalysisDetailRepository detailRepository;
    private final AiService aiService;
    private final DemoIntelligence demoIntelligence;

    public AnalysisService(JobOfferRepository jobRepository, JobService jobService,
                           UserSkillRepository userSkillRepository, AnalysisRepository analysisRepository,
                           AnalysisDetailRepository detailRepository, AiService aiService,
                           DemoIntelligence demoIntelligence) {
        this.jobRepository = jobRepository;
        this.jobService = jobService;
        this.userSkillRepository = userSkillRepository;
        this.analysisRepository = analysisRepository;
        this.detailRepository = detailRepository;
        this.aiService = aiService;
        this.demoIntelligence = demoIntelligence;
    }

    private record Eval(JobSkill jobSkill, UserSkill userSkill, String matchCategory, double mastery,
                        String note) {
        double gapScore() {
            return weightWithFallback() * (1.0 - mastery);
        }

        int weightWithFallback() {
            return Math.max(1, jobSkill.getWeight());
        }
    }

    @Transactional
    public AnalysisResultDto analyze(Long userId, Long jobId) {
        JobOffer job = jobRepository.findByIdAndUserId(jobId, userId)
                .orElseThrow(() -> ApiException.notFound("Offre d'emploi introuvable."));
        List<JobSkill> jobSkills = jobService.rawJobSkills(jobId);
        if (jobSkills.isEmpty()) {
            throw ApiException.badRequest("EXTRACT_FIRST",
                    "Extrayez puis validez les compétences de l'offre avant de lancer l'analyse.");
        }

        Map<String, UserSkill> profileSkills = canonicalProfileMap(userId);

        List<Eval> evals = new ArrayList<>();
        for (JobSkill jobSkill : jobSkills) {
            UserSkill match = profileSkills.get(demoIntelligence.canonical(jobSkill.getNormalizedName()));
            evals.add(evaluate(jobSkill, match));
        }

        double totalWeight = evals.stream().mapToDouble(e -> e.weightWithFallback()).sum();
        double weighted = evals.stream().mapToDouble(e -> e.weightWithFallback() * e.mastery).sum();
        int score = totalWeight == 0 ? 0 : (int) Math.round((weighted / totalWeight) * 100);

        List<Eval> gaps = evals.stream()
                .filter(e -> e.mastery < 0.95)
                .sorted(Comparator.comparingDouble(Eval::gapScore).reversed())
                .toList();
        Map<Long, Integer> priorities = new LinkedHashMap<>();
        for (int i = 0; i < gaps.size(); i++) {
            priorities.put(gaps.get(i).jobSkill().getId(), i + 1);
        }

        Counts counts = counts(evals);
        List<String> topGapNames = gaps.stream().limit(3)
                .map(e -> e.jobSkill().getSkillName()).toList();
        AnalysisText aiText = aiService.summarizeAnalysis(job.getTitle(), score, counts.demonstrated(),
                counts.declared(), counts.partial(), counts.missing(), topGapNames, userId);

        Analysis analysis = new Analysis();
        analysis.setUserId(userId);
        analysis.setJobOfferId(job.getId());
        analysis.setScore(score);
        analysis.setSummary(truncate(aiText.summary(), 2000));
        analysis.setExplanation(buildExplanation(score, evals, counts, gaps, aiText, aiText.simulated()));
        analysis.setSimulatedAi(aiText.simulated());
        analysis = analysisRepository.save(analysis);

        List<AnalysisDetail> details = new ArrayList<>();
        for (Eval eval : evals) {
            AnalysisDetail detail = new AnalysisDetail();
            detail.setAnalysisId(analysis.getId());
            if (eval.userSkill() != null) detail.setUserSkillId(eval.userSkill().getId());
            detail.setSkillName(eval.jobSkill().getSkillName());
            detail.setNormalizedName(eval.jobSkill().getNormalizedName());
            detail.setCategory(eval.jobSkill().getCategory());
            detail.setMatchCategory(eval.matchCategory());
            detail.setRequiredLevel(effectiveRequiredLevel(eval.jobSkill()));
            detail.setUserLevel(eval.userSkill() == null ? null : eval.userSkill().getLevel().name());
            detail.setWeight(eval.weightWithFallback());
            detail.setMasteryScore(BigDecimal.valueOf(eval.mastery).setScale(2, RoundingMode.HALF_UP));
            detail.setPriority(priorities.getOrDefault(eval.jobSkill().getId(), 0));
            detail.setNote(eval.note());
            details.add(detail);
        }
        detailRepository.saveAll(details);

        return toResult(analysis, details, counts);
    }

    public List<AnalysisSummaryDto> history(Long userId) {
        JobOffersById jobs = new JobOffersById();
        return analysisRepository.findByUserIdOrderByIdDesc(userId).stream()
                .map(a -> {
                    JobOffer job = jobs.load(a.getJobOfferId());
                    return new AnalysisSummaryDto(a.getId(), a.getJobOfferId(),
                            job == null ? "Offre supprimée" : job.getTitle(),
                            job == null ? null : job.getCompany(), a.getScore(), a.getSummary(),
                            a.isSimulatedAi(), a.getCreatedAt());
                })
                .toList();
    }

    public AnalysisResultDto get(Long userId, Long analysisId) {
        Analysis analysis = analysisRepository.findByIdAndUserId(analysisId, userId)
                .orElseThrow(() -> ApiException.notFound("Analyse introuvable."));
        List<AnalysisDetail> details = detailRepository.findByAnalysisIdOrderByPriorityAscIdAsc(analysis.getId());
        return toResult(analysis, details, null);
    }

    public List<GapDto> gaps(Long userId, Long analysisId) {
        Analysis analysis = analysisRepository.findByIdAndUserId(analysisId, userId)
                .orElseThrow(() -> ApiException.notFound("Analyse introuvable."));
        return detailRepository.findByAnalysisIdOrderByPriorityAscIdAsc(analysis.getId()).stream()
                .map(d -> new GapDto(d.getSkillName(), d.getMatchCategory(), d.getPriority(), d.getNote()))
                .toList();
    }

    public Analysis requireAnalysis(Long userId, Long analysisId) {
        return analysisRepository.findByIdAndUserId(analysisId, userId)
                .orElseThrow(() -> ApiException.notFound("Analyse introuvable."));
    }

    public List<AnalysisDetail> prioritizedGaps(Long analysisId) {
        return detailRepository.findByAnalysisIdAndPriorityGreaterThanOrderByPriorityAsc(analysisId, 0);
    }

    public List<AnalysisDetail> detailsOf(Long analysisId) {
        return detailRepository.findByAnalysisIdOrderByPriorityAscIdAsc(analysisId);
    }

    public long countByUser(Long userId) {
        return analysisRepository.countByUserId(userId);
    }

    public Analysis latest(Long userId) {
        return analysisRepository.findTopByUserIdOrderByIdDesc(userId).orElse(null);
    }

    // --- scoring rules --------------------------------------------------------

    private Eval evaluate(JobSkill jobSkill, UserSkill userSkill) {
        String requiredLevel = effectiveRequiredLevel(jobSkill);
        SkillLevel required = SkillLevel.valueOf(requiredLevel);

        if (userSkill == null) {
            return new Eval(jobSkill, null, "MISSING", 0.0,
                    "Absente de votre profil : une compétence inconnue n'est jamais considérée comme acquise.");
        }
        if (userSkill.getLevel() == SkillLevel.UNKNOWN) {
            return new Eval(jobSkill, userSkill, "MISSING", 0.15,
                    "Déclarée mais niveau non évalué : insuffisamment documentée pour être comptée comme acquise.");
        }

        int userRank = userSkill.getLevel().getRank();
        int requiredRank = required.getRank();
        boolean demonstrated = userSkill.getStatus() == SkillStatus.DEMONSTRATED;
        boolean evaluated = userSkill.getStatus() == SkillStatus.EVALUATED;

        if (userRank >= requiredRank) {
            if (demonstrated) {
                return new Eval(jobSkill, userSkill, "DEMONSTRATED", 1.0,
                        "Maîtrisée et démontrée par une preuve validée.");
            }
            if (evaluated) {
                return new Eval(jobSkill, userSkill, "DECLARED", 0.90,
                        "Évaluée par un projet validé, sans preuve externe : crédit partiel.");
            }
            return new Eval(jobSkill, userSkill, "DECLARED", 0.75,
                    "Déclarée et auto-évaluée mais non vérifiée : plafonnée à 0,75.");
        }
        if (userRank == requiredRank - 1) {
            return new Eval(jobSkill, userSkill, "PARTIAL", demonstrated ? 0.60 : 0.50,
                    "Niveau " + userSkill.getLevel().name() + " face au niveau " + required.name()
                            + " demandé : partiellement maîtrisée.");
        }
        return new Eval(jobSkill, userSkill, "PARTIAL", demonstrated ? 0.35 : 0.25,
                "Écart de niveau important (" + userSkill.getLevel().name() + " face à " + required.name()
                        + ").");
    }

    private String effectiveRequiredLevel(JobSkill jobSkill) {
        if (jobSkill.getRequiredLevel() == null || jobSkill.getRequiredLevel().isBlank()) {
            return SkillLevel.INTERMEDIATE.name();
        }
        return jobSkill.getRequiredLevel();
    }

    private Counts counts(List<Eval> evals) {
        int demonstrated = 0, declared = 0, partial = 0, missing = 0;
        for (Eval eval : evals) {
            switch (eval.matchCategory()) {
                case "DEMONSTRATED" -> demonstrated++;
                case "DECLARED" -> declared++;
                case "PARTIAL" -> partial++;
                default -> missing++;
            }
        }
        return new Counts(demonstrated, declared, partial, missing, evals.size());
    }

    private Map<String, UserSkill> canonicalProfileMap(Long userId) {
        Map<String, UserSkill> map = new LinkedHashMap<>();
        for (UserSkill userSkill : userSkillRepository.findByUserIdOrderById(userId)) {
            map.putIfAbsent(demoIntelligence.canonical(userSkill.getNormalizedName()), userSkill);
        }
        return map;
    }

    private String buildExplanation(int score, List<Eval> evals, Counts counts, List<Eval> gaps,
                                    AnalysisText aiText, boolean simulated) {
        List<String> strengths = evals.stream()
                .filter(e -> e.mastery() >= 0.75)
                .sorted(Comparator.comparingDouble((Eval e) -> e.weightWithFallback() * e.mastery()).reversed())
                .limit(3)
                .map(e -> e.jobSkill().getSkillName() + " (" + e.matchCategory().toLowerCase() + ", poids "
                        + e.weightWithFallback() + ")")
                .toList();
        List<String> topGaps = gaps.stream().limit(5)
                .map(e -> e.jobSkill().getSkillName() + " (" + label(e.matchCategory()) + ", poids "
                        + e.weightWithFallback() + ")")
                .toList();

        StringBuilder sb = new StringBuilder();
        sb.append("Méthode de calcul : score = somme(poids × maîtrise) ÷ somme(poids) × 100, arrondi à l'unité. ")
                .append("Résultat : ").append(score).append("/100.\n");
        sb.append("Facteurs positifs : ")
                .append(strengths.isEmpty() ? "aucun critère pleinement couvert" : String.join(", ", strengths))
                .append(".\n");
        sb.append("Écarts prioritaires : ")
                .append(topGaps.isEmpty() ? "aucun écart détecté" : String.join(", ", topGaps))
                .append(".\n");
        sb.append("Répartition : ").append(counts.demonstrated()).append(" démontrée(s), ")
                .append(counts.declared()).append(" déclarée(s), ").append(counts.partial())
                .append(" partielle(s), ").append(counts.missing()).append(" manquante(s) sur ")
                .append(counts.total()).append(" compétences exigées.\n");
        sb.append("Rappel : ce score est un indicateur interne de compatibilité, ")
                .append("et non une probabilité d'obtenir le poste.");
        if (aiText != null && aiText.explanation() != null && !aiText.explanation().isBlank()
                && aiText.explanation().contains("Next step")) {
            sb.append("\n\nRecommandation IA : ").append(aiText.explanation().trim());
        }
        if (simulated) {
            sb.append("\n\nMode démonstration : les textes générés par l'IA sont simulés (aucune clé API configurée).");
        }
        return sb.toString();
    }

    private String label(String matchCategory) {
        return switch (matchCategory) {
            case "DEMONSTRATED" -> "démontrée";
            case "DECLARED" -> "déclarée";
            case "PARTIAL" -> "partielle";
            default -> "manquante";
        };
    }

    private AnalysisResultDto toResult(Analysis analysis, List<AnalysisDetail> details, Counts counts) {
        JobOffer job = jobRepository.findById(analysis.getJobOfferId()).orElse(null);
        AnalysisSummaryDto summary = new AnalysisSummaryDto(analysis.getId(), analysis.getJobOfferId(),
                job == null ? "Offre supprimée" : job.getTitle(), job == null ? null : job.getCompany(),
                analysis.getScore(), analysis.getSummary(), analysis.isSimulatedAi(), analysis.getCreatedAt());
        Counts c = counts != null ? counts : countsFromDetails(details);
        List<AnalysisDetailDto> dtos = details.stream()
                .map(d -> new AnalysisDetailDto(d.getId(), d.getSkillName(), d.getCategory(),
                        d.getMatchCategory(), d.getRequiredLevel(), d.getUserLevel(), d.getWeight(),
                        d.getMasteryScore(), d.getPriority(), d.getNote(), d.getUserSkillId()))
                .toList();
        return new AnalysisResultDto(summary, analysis.getExplanation(), c, dtos, analysis.isSimulatedAi());
    }

    private Counts countsFromDetails(List<AnalysisDetail> details) {
        int demonstrated = 0, declared = 0, partial = 0, missing = 0;
        for (AnalysisDetail d : details) {
            switch (d.getMatchCategory()) {
                case "DEMONSTRATED" -> demonstrated++;
                case "DECLARED" -> declared++;
                case "PARTIAL" -> partial++;
                default -> missing++;
            }
        }
        return new Counts(demonstrated, declared, partial, missing, details.size());
    }

    /** Small cache to avoid N+1 queries when listing history. */
    private class JobOffersById {
        private final Map<Long, JobOffer> cache = new LinkedHashMap<>();

        JobOffer load(Long id) {
            return cache.computeIfAbsent(id, key -> jobRepository.findById(key).orElse(null));
        }
    }

    private String truncate(String value, int max) {
        if (value == null) return null;
        return value.length() > max ? value.substring(0, max) : value;
    }
}
