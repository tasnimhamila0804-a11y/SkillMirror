package com.skillmirror.dashboard;

import com.skillmirror.analysis.Analysis;
import com.skillmirror.analysis.AnalysisService;
import com.skillmirror.analysis.dto.AnalysisDtos.AnalysisSummaryDto;
import com.skillmirror.analysis.dto.AnalysisDtos.GapDto;
import com.skillmirror.common.ApiException;
import com.skillmirror.dashboard.dto.DashboardDtos.DashboardDto;
import com.skillmirror.dashboard.dto.DashboardDtos.PrioritySkillDto;
import com.skillmirror.dashboard.dto.DashboardDtos.RecommendedActionDto;
import com.skillmirror.dashboard.dto.DashboardDtos.ScorePointDto;
import com.skillmirror.dashboard.dto.DashboardDtos.SkillProgressDto;
import com.skillmirror.dashboard.dto.DashboardDtos.StatsDto;
import com.skillmirror.dashboard.dto.DashboardDtos.TimelineEventDto;
import com.skillmirror.job.JobOfferRepository;
import com.skillmirror.learning.LearningPath;
import com.skillmirror.learning.LearningPathRepository;
import com.skillmirror.learning.LearningTaskRepository;
import com.skillmirror.learning.SkillEvidence;
import com.skillmirror.learning.SkillEvidenceRepository;
import com.skillmirror.learning.dto.LearningDtos.EvidenceDto;
import com.skillmirror.learning.dto.LearningDtos.LearningPathSummaryDto;
import com.skillmirror.learning.LearningService;
import com.skillmirror.profile.UserProjectRepository;
import com.skillmirror.skill.SkillStatus;
import com.skillmirror.skill.UserSkill;
import com.skillmirror.skill.UserSkillRepository;
import com.skillmirror.user.User;
import com.skillmirror.user.UserRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

/**
 * Dashboard and progress indicators — every value comes from the database and backend
 * calculations, never from hard-coded statistics.
 */
@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final JobOfferRepository jobRepository;
    private final AnalysisService analysisService;
    private final LearningPathRepository pathRepository;
    private final LearningTaskRepository taskRepository;
    private final SkillEvidenceRepository evidenceRepository;
    private final UserSkillRepository userSkillRepository;
    private final UserProjectRepository projectRepository;
    private final LearningService learningService;

    public DashboardService(UserRepository userRepository, JobOfferRepository jobRepository,
                            AnalysisService analysisService, LearningPathRepository pathRepository,
                            LearningTaskRepository taskRepository, SkillEvidenceRepository evidenceRepository,
                            UserSkillRepository userSkillRepository, UserProjectRepository projectRepository,
                            LearningService learningService) {
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.analysisService = analysisService;
        this.pathRepository = pathRepository;
        this.taskRepository = taskRepository;
        this.evidenceRepository = evidenceRepository;
        this.userSkillRepository = userSkillRepository;
        this.projectRepository = projectRepository;
        this.learningService = learningService;
    }

    public DashboardDto dashboard(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("Utilisateur introuvable."));

        List<LearningPath> paths = pathRepository.findByUserIdOrderByIdDesc(userId);
        long activePaths = paths.stream()
                .filter(p -> List.of("NOT_STARTED", "IN_PROGRESS", "PAUSED").contains(p.getStatus())).count();
        long completedPaths = paths.stream()
                .filter(p -> List.of("COMPLETED", "VALIDATED").contains(p.getStatus())).count();

        long totalTasks = 0;
        long doneTasks = 0;
        for (LearningPath path : paths) {
            totalTasks += taskRepository.countByLearningPathId(path.getId());
            doneTasks += taskRepository.countByLearningPathIdAndDoneTrue(path.getId());
        }
        int progressRate = totalTasks == 0 ? 0 : (int) Math.round((doneTasks * 100.0) / totalTasks);

        List<UserSkill> skills = userSkillRepository.findByUserIdOrderById(userId);
        long demonstrated = skills.stream().filter(s -> s.getStatus() == SkillStatus.DEMONSTRATED).count();
        long evaluated = skills.stream().filter(s -> s.getStatus() == SkillStatus.EVALUATED).count();
        long declared = skills.stream().filter(s -> s.getStatus() == SkillStatus.DECLARED).count();

        long offersCount = jobRepository.countByUserId(userId);
        long analysesCount = analysisService.countByUser(userId);
        Analysis latest = analysisService.latest(userId);

        Integer lastScore = latest == null ? null : latest.getScore();
        String lastScoreJob = null;
        Instant lastAnalysisDate = null;
        List<PrioritySkillDto> prioritySkills = List.of();
        if (latest != null) {
            lastAnalysisDate = latest.getCreatedAt();
            prioritySkills = analysisService.prioritizedGaps(latest.getId()).stream()
                    .limit(5)
                    .map(d -> new PrioritySkillDto(d.getSkillName(), d.getMatchCategory(), d.getPriority(),
                            d.getNote()))
                    .toList();
            try {
                lastScoreJob = jobRepository.findById(latest.getJobOfferId())
                        .map(j -> j.getTitle()).orElse("Offre supprimée");
            } catch (Exception ignored) {
                lastScoreJob = null;
            }
        }

        StatsDto stats = new StatsDto(activePaths, completedPaths, totalTasks, doneTasks, progressRate,
                skills.size(), demonstrated, evaluated, declared, analysesCount, offersCount);

        List<AnalysisSummaryDto> recentAnalyses = analysisService.history(userId).stream().limit(6).toList();

        boolean emptyState = !user.isProfileCompleted() || (analysesCount == 0 && paths.isEmpty());
        List<RecommendedActionDto> actions = recommendedActions(user, offersCount, analysesCount, latest,
                paths, prioritySkills, demonstrated);

        return new DashboardDto(user.getName().split(" ")[0], user.isProfileCompleted(), lastScore,
                lastScoreJob, lastAnalysisDate, prioritySkills, stats, recentAnalyses, actions, emptyState);
    }

    public com.skillmirror.dashboard.dto.DashboardDtos.ProgressDto progress(Long userId) {
        List<LearningPath> paths = pathRepository.findByUserIdOrderByIdDesc(userId);
        List<LearningPathSummaryDto> pathSummaries = paths.stream().map(learningService::toSummary).toList();

        long totalTasks = 0;
        long doneTasks = 0;
        for (LearningPath path : paths) {
            totalTasks += taskRepository.countByLearningPathId(path.getId());
            doneTasks += taskRepository.countByLearningPathIdAndDoneTrue(path.getId());
        }

        List<ScorePointDto> scoreHistory = analysisService.history(userId).stream()
                .map(a -> new ScorePointDto(a.id(), a.score(), a.jobTitle(), a.createdAt()))
                .sorted(Comparator.comparing((ScorePointDto p) -> p.date()).reversed())
                .toList();

        List<SkillProgressDto> skillProgress = userSkillRepository.findByUserIdOrderById(userId).stream()
                .map(s -> new SkillProgressDto(s.getId(), s.getSkillName(), s.getCategory(), s.getLevel().name(),
                        s.getStatus().name(), s.getUpdatedAt()))
                .toList();

        List<SkillEvidence> rawEvidences = evidenceRepository.findByUserIdOrderByIdDesc(userId);
        List<EvidenceDto> evidences = rawEvidences.stream()
                .map(com.skillmirror.learning.EvidenceService::toDto)
                .toList();
        long verifiedProofs = evidences.stream().filter(e -> "VERIFIED".equals(e.status())).count();

        List<TimelineEventDto> timeline = new ArrayList<>();
        for (AnalysisSummaryDto analysis : analysisService.history(userId)) {
            timeline.add(new TimelineEventDto("ANALYSIS",
                    "Analyse de « " + analysis.jobTitle() + " »",
                    "Score de compatibilité : " + analysis.score() + "/100", analysis.createdAt()));
        }
        for (LearningPath path : paths) {
            if (path.getCompletedAt() != null) {
                timeline.add(new TimelineEventDto("PATH", "Parcours validé : " + path.getTitle(),
                        "Compétence ciblée : " + path.getSkillName(), path.getCompletedAt()));
            }
        }
        for (SkillEvidence evidence : rawEvidences) {
            timeline.add(new TimelineEventDto("EVIDENCE",
                    "Preuve " + label(evidence.getStatus()) + " : " + evidence.getSkillName(),
                    evidence.getUrl() != null ? evidence.getUrl() : (evidence.getFileName() != null
                            ? evidence.getFileName() : "Rapport"), evidence.getCreatedAt()));
        }
        for (var project : projectRepository.findByUserIdOrderByIdDesc(userId)) {
            timeline.add(new TimelineEventDto("PROJECT", "Projet ajouté : " + project.getName(),
                    project.getRepositoryUrl(), project.getCreatedAt()));
        }
        timeline.sort(Comparator.comparing(TimelineEventDto::date).reversed());

        return new com.skillmirror.dashboard.dto.DashboardDtos.ProgressDto(
                pathSummaries, scoreHistory, skillProgress, evidences, timeline.stream().limit(15).toList(),
                totalTasks, doneTasks, verifiedProofs);
    }

    private List<RecommendedActionDto> recommendedActions(User user, long offersCount, long analysesCount,
                                                          Analysis latest, List<LearningPath> paths,
                                                          List<PrioritySkillDto> prioritySkills,
                                                          long demonstratedSkills) {
        List<RecommendedActionDto> actions = new ArrayList<>();
        if (!user.isProfileCompleted()) {
            actions.add(new RecommendedActionDto("Complétez votre profilage",
                    "Répondez au questionnaire initial pour que SkillMirror connaisse vos compétences et vos objectifs.",
                    "/questionnaire", "HIGH"));
        }
        if (user.isProfileCompleted() && offersCount == 0) {
            actions.add(new RecommendedActionDto("Analysez une première offre",
                    "Collez l'offre d'emploi qui vous intéresse : nous extrairons ses compétences exigées.",
                    "/jobs/new", "HIGH"));
        }
        if (offersCount > 0 && analysesCount == 0) {
            actions.add(new RecommendedActionDto("Lancez une analyse de compatibilité",
                    "Comparez votre profil aux exigences de l'offre et découvrez vos écarts prioritaires.",
                    "/jobs", "HIGH"));
        }
        if (latest != null && !prioritySkills.isEmpty()) {
            boolean hasActivePathForTopGap = paths.stream().anyMatch(
                    p -> p.getAnalysisId() != null && p.getAnalysisId().equals(latest.getId())
                            && !"VALIDATED".equals(p.getStatus()));
            if (!hasActivePathForTopGap) {
                actions.add(new RecommendedActionDto(
                        "Générez un projet pour « " + prioritySkills.get(0).skillName() + " »",
                        "Transformez votre plus gros écart en projet concret avec étapes, tâches et preuve attendue.",
                        "/analysis/" + latest.getId(), "MEDIUM"));
            }
        }
        for (LearningPath path : paths) {
            if ("NOT_STARTED".equals(path.getStatus())) {
                actions.add(new RecommendedActionDto("Commencez « " + path.getTitle() + " »",
                        "Aucune tâche n'est encore démarrée sur ce parcours.",
                        "/learning/" + path.getId(), "MEDIUM"));
                break;
            }
        }
        for (LearningPath path : paths) {
            long total = taskRepository.countByLearningPathId(path.getId());
            long done = taskRepository.countByLearningPathIdAndDoneTrue(path.getId());
            if (total > 0 && done == total && "IN_PROGRESS".equals(path.getStatus())) {
                actions.add(new RecommendedActionDto("Soumettez une preuve pour « " + path.getTitle() + " »",
                        "Tâches terminées : ajoutez un lien ou un rapport pour faire valider la compétence.",
                        "/learning/" + path.getId(), "MEDIUM"));
                break;
            }
        }
        if (user.isProfileCompleted() && demonstratedSkills == 0 && analysesCount > 0) {
            actions.add(new RecommendedActionDto("Passez une compétence en « démontrée »",
                    "Une preuve validée (dépôt, rapport) augmente durablement votre score.",
                    "/profile", "LOW"));
        }
        return actions.stream().limit(4).toList();
    }

    private String label(String status) {
        return switch (status) {
            case "VERIFIED" -> "vérifiée";
            case "REJECTED" -> "rejetée";
            default -> "soumise";
        };
    }
}
