package com.skillmirror.learning;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillmirror.ai.AiService;
import com.skillmirror.ai.dto.AiDtos.GeneratedProject;
import com.skillmirror.analysis.AnalysisDetail;
import com.skillmirror.analysis.AnalysisService;
import com.skillmirror.common.ApiException;
import com.skillmirror.learning.dto.LearningDtos.CompletionCheckDto;
import com.skillmirror.learning.dto.LearningDtos.GenerateRequest;
import com.skillmirror.learning.dto.LearningDtos.LearningPathDto;
import com.skillmirror.learning.dto.LearningDtos.LearningPathSummaryDto;
import com.skillmirror.learning.dto.LearningDtos.TaskDto;
import com.skillmirror.learning.dto.LearningDtos.TaskUpdateRequest;
import com.skillmirror.profile.Profile;
import com.skillmirror.profile.ProfileService;
import com.skillmirror.skill.Skill;
import com.skillmirror.skill.SkillCategory;
import com.skillmirror.skill.SkillLevel;
import com.skillmirror.skill.SkillRepository;
import com.skillmirror.skill.SkillService;
import com.skillmirror.skill.SkillStatus;
import com.skillmirror.skill.UserSkill;
import com.skillmirror.skill.UserSkillRepository;
import com.skillmirror.ai.DemoIntelligence;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Project-to-Proof use cases. Validation of a path NEVER happens automatically from task
 * completion alone: the backend enforces explicit criteria (all tasks done + a submitted proof)
 * before upgrading the skill status.
 */
@Service
public class LearningService {

    private static final Logger log = LoggerFactory.getLogger(LearningService.class);

    private final LearningPathRepository pathRepository;
    private final LearningTaskRepository taskRepository;
    private final SkillEvidenceRepository evidenceRepository;
    private final AnalysisService analysisService;
    private final ProfileService profileService;
    private final SkillRepository skillRepository;
    private final UserSkillRepository userSkillRepository;
    private final AiService aiService;
    private final DemoIntelligence demoIntelligence;
    private final ObjectMapper objectMapper;

    public LearningService(LearningPathRepository pathRepository, LearningTaskRepository taskRepository,
                           SkillEvidenceRepository evidenceRepository, AnalysisService analysisService,
                           ProfileService profileService,
                           SkillRepository skillRepository, UserSkillRepository userSkillRepository,
                           AiService aiService, DemoIntelligence demoIntelligence, ObjectMapper objectMapper) {
        this.pathRepository = pathRepository;
        this.taskRepository = taskRepository;
        this.evidenceRepository = evidenceRepository;
        this.analysisService = analysisService;
        this.profileService = profileService;
        this.skillRepository = skillRepository;
        this.userSkillRepository = userSkillRepository;
        this.aiService = aiService;
        this.demoIntelligence = demoIntelligence;
        this.objectMapper = objectMapper;
    }

    // --- generation ---------------------------------------------------------

    @Transactional
    public List<LearningPathSummaryDto> generate(Long userId, GenerateRequest request) {
        if (request.analysisId() == null) {
            throw ApiException.badRequest("ANALYSIS_REQUIRED", "Sélectionnez une analyse de référence.");
        }
        analysisService.requireAnalysis(userId, request.analysisId());
        int limit = request.limit() == null ? 3 : Math.max(1, Math.min(5, request.limit()));

        List<AnalysisDetail> gaps = analysisService.prioritizedGaps(request.analysisId()).stream()
                .filter(d -> "MISSING".equals(d.getMatchCategory()) || "PARTIAL".equals(d.getMatchCategory()))
                .toList();

        Profile profile = profileService.requireEntity(userId);
        List<LearningPath> created = new ArrayList<>();

        for (AnalysisDetail gap : gaps) {
            if (created.size() >= limit) break;
            if (pathRepository.findTopByUserIdAndAnalysisDetailIdAndStatusNotIn(
                    userId, gap.getId(), List.of("COMPLETED", "VALIDATED")).isPresent()) {
                continue; // an active path already targets this gap
            }
            created.add(createPathForGap(userId, gap, profile));
        }
        if (created.isEmpty()) {
            throw ApiException.conflict("NO_NEW_GAPS",
                    "Tous les écarts prioritaires ont déjà un parcours actif. Terminez-les avant d'en générer d'autres.");
        }
        return created.stream().map(this::toSummary).toList();
    }

    private LearningPath createPathForGap(Long userId, AnalysisDetail gap, Profile profile) {
        SkillLevel userLevel = gap.getUserLevel() == null ? SkillLevel.UNKNOWN : SkillLevel.from(gap.getUserLevel());
        SkillCategory category = SkillService.parseCategory(gap.getCategory());
        String difficulty = resolveDifficulty(gap.getRequiredLevel(), userLevel,
                profile == null ? null : profile.getPreferredDifficulty());
        int hours = profile != null && profile.getHoursPerWeek() != null && profile.getHoursPerWeek() > 0
                ? profile.getHoursPerWeek() : 6;

        GeneratedProject project = aiService.generateProject(
                gap.getSkillName(), category, userLevel.name(), difficulty,
                profile == null ? null : profile.getTargetJob(), hours,
                profile == null ? null : profile.getLearningPreference(), userId);

        LearningPath path = new LearningPath();
        path.setUserId(userId);
        path.setAnalysisId(gap.getAnalysisId());
        path.setAnalysisDetailId(gap.getId());
        path.setSkillName(gap.getSkillName());
        Skill reference = skillRepository.findByNormalizedName(gap.getNormalizedName()).orElse(null);
        if (reference == null) {
            reference = skillRepository.findByNormalizedName(
                    demoIntelligence.canonical(gap.getNormalizedName())).orElse(null);
        }
        if (reference != null) path.setSkillId(reference.getId());
        path.setTitle(project.title());
        path.setDescription(project.description());
        path.setDifficulty(project.difficulty());
        path.setObjectives(writeList(project.objectives()));
        path.setTools(writeList(project.tools()));
        path.setSteps(writeList(project.steps()));
        path.setEvaluationCriteria(writeList(project.evaluationCriteria()));
        path.setExpectedResult(project.expectedResult());
        path.setProofRequired(project.proofRequired());
        path.setStatus("NOT_STARTED");
        path.setAiGenerated(true);
        path.setSimulatedAi(project.simulated());
        path = pathRepository.save(path);

        List<LearningTask> tasks = new ArrayList<>();
        int position = 0;
        for (String taskTitle : project.tasks()) {
            LearningTask task = new LearningTask();
            task.setLearningPathId(path.getId());
            task.setTitle(taskTitle);
            task.setPosition(position++);
            tasks.add(task);
        }
        taskRepository.saveAll(tasks);
        return path;
    }

    private String resolveDifficulty(String requiredLevel, SkillLevel userLevel, String preferred) {
        String base = requiredLevel != null && !requiredLevel.isBlank() ? requiredLevel
                : (preferred != null && !preferred.isBlank() ? preferred : "INTERMEDIATE");
        if (userLevel != SkillLevel.UNKNOWN) {
            // never overload the user: cap the difficulty one step above their current level
            int target = Math.min(SkillLevel.from(base).getRank() + 1, userLevel.getRank() + 1);
            base = switch (Math.max(1, target)) {
                case 1 -> "BEGINNER";
                case 3 -> "ADVANCED";
                case 4 -> "ADVANCED";
                default -> "INTERMEDIATE";
            };
        }
        return base.toUpperCase(Locale.ROOT);
    }

    // --- reading ------------------------------------------------------------

    public List<LearningPathSummaryDto> list(Long userId, String status) {
        List<LearningPath> paths = (status == null || status.isBlank())
                ? pathRepository.findByUserIdOrderByIdDesc(userId)
                : pathRepository.findByUserIdAndStatusOrderByIdDesc(userId, status.trim().toUpperCase(Locale.ROOT));
        return paths.stream().map(this::toSummary).toList();
    }

    public LearningPathDto get(Long userId, Long id) {
        return toDto(require(userId, id));
    }

    // --- lifecycle ----------------------------------------------------------

    @Transactional
    public LearningPathDto updateStatus(Long userId, Long id, String status) {
        LearningPath path = require(userId, id);
        String target = status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
        switch (target) {
            case "IN_PROGRESS" -> {
                if (!List.of("NOT_STARTED", "PAUSED").contains(path.getStatus())) {
                    throw ApiException.badRequest("INVALID_STATUS",
                            "Ce parcours est déjà terminé ou en cours.");
                }
                path.setStatus("IN_PROGRESS");
            }
            case "PAUSED" -> {
                if (!List.of("NOT_STARTED", "IN_PROGRESS").contains(path.getStatus())) {
                    throw ApiException.badRequest("INVALID_STATUS", "Ce parcours est déjà terminé.");
                }
                path.setStatus("PAUSED");
            }
            default -> throw ApiException.badRequest("INVALID_STATUS",
                    "Statut invalide. Utilisez IN_PROGRESS ou PAUSED.");
        }
        path.touch();
        return toDto(pathRepository.save(path));
    }

    @Transactional
    public LearningPathDto updateTask(Long userId, Long pathId, Long taskId, TaskUpdateRequest request) {
        LearningPath path = require(userId, pathId);
        LearningTask task = taskRepository.findByIdAndLearningPathId(taskId, pathId)
                .orElseThrow(() -> ApiException.notFound("Tâche introuvable."));

        if (request.done() != null) {
            task.setDone(request.done());
            task.setDoneAt(request.done() ? Instant.now() : null);
            if (request.done() && "NOT_STARTED".equals(path.getStatus())) {
                path.setStatus("IN_PROGRESS");
                path.touch();
                pathRepository.save(path);
            }
        }
        if (request.note() != null) {
            task.setNote(request.note().isBlank() ? null : request.note());
        }
        taskRepository.save(task);
        return toDto(path);
    }

    @Transactional
    public LearningPathDto updateTaskById(Long userId, Long taskId, TaskUpdateRequest request) {
        LearningTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> ApiException.notFound("Tâche introuvable."));
        return updateTask(userId, task.getLearningPathId(), taskId, request);
    }

    public CompletionCheckDto checkCompletion(Long userId, Long id) {
        LearningPath path = require(userId, id);
        long total = taskRepository.countByLearningPathId(path.getId());
        long done = taskRepository.countByLearningPathIdAndDoneTrue(path.getId());
        boolean criteriaDefined = readList(path.getEvaluationCriteria()).size() > 0;
        boolean hasProof = evidenceRepository.findByLearningPathIdOrderByIdDesc(path.getId()).stream()
                .anyMatch(e -> hasContent(e));
        List<String> missing = new ArrayList<>();
        if (total == 0) missing.add("Aucune tâche définie pour ce parcours.");
        if (done < total) missing.add((total - done) + " tâche(s) restante(s).");
        if (!criteriaDefined) missing.add("Critères d'évaluation absents.");
        if (!hasProof) missing.add("Aucune preuve soumise (lien, fichier ou rapport).");
        return new CompletionCheckDto(missing.isEmpty(), missing, (int) done, (int) total, hasProof,
                criteriaDefined);
    }

    /**
     * Completes a path. The backend verifies the explicit criteria before touching any skill status:
     * all tasks done + a defined evaluation criterion + a submitted proof. Completing tasks alone
     * is never enough to certify a skill.
     */
    @Transactional
    public LearningPathDto complete(Long userId, Long id) {
        LearningPath path = require(userId, id);
        if ("VALIDATED".equals(path.getStatus()) || "COMPLETED".equals(path.getStatus())) {
            return toDto(path);
        }
        CompletionCheckDto check = checkCompletion(userId, id);
        if (!check.canComplete()) {
            throw ApiException.badRequest("CRITERIA_NOT_MET",
                    "Validation impossible : " + String.join(" ", check.missing()));
        }

        List<SkillEvidence> evidences = evidenceRepository.findByLearningPathIdOrderByIdDesc(path.getId());
        boolean hasLinkOrFile = evidences.stream().anyMatch(e -> hasStrongProof(e));

        path.setStatus("VALIDATED");
        path.setCompletedAt(Instant.now());
        path.setValidationNote("Validé par le backend : tâches complètes ("
                + check.doneTasks() + "/" + check.totalTasks() + ") et preuve fournie ("
                + (hasLinkOrFile ? "lien ou fichier" : "rapport") + ").");
        path.touch();
        pathRepository.save(path);

        for (SkillEvidence evidence : evidences) {
            evidence.setStatus("VERIFIED");
            evidence.setReviewNote("Preuve acceptée pour le parcours « " + path.getTitle() + " ».");
            evidenceRepository.save(evidence);
        }

        upgradeSkillStatus(userId, path, hasLinkOrFile);
        return toDto(path);
    }

    private void upgradeSkillStatus(Long userId, LearningPath path, boolean hasStrongProof) {
        try {
            String normalized = Skill.normalize(path.getSkillName());
            UserSkill userSkill = userSkillRepository.findByUserIdAndNormalizedName(userId, normalized)
                    .orElse(null);
            if (userSkill == null) {
                userSkill = userSkillRepository.findByUserIdOrderById(userId).stream()
                        .filter(us -> demoIntelligence.canonical(us.getNormalizedName())
                                .equals(demoIntelligence.canonical(normalized)))
                        .findFirst().orElse(null);
            }
            if (userSkill == null) return;

            SkillStatus newStatus = hasStrongProof ? SkillStatus.DEMONSTRATED : SkillStatus.EVALUATED;
            int newRank = Math.max(userSkill.getLevel().getRank(), SkillLevel.INTERMEDIATE.getRank());
            SkillLevel targetLevel = switch (newRank) {
                case 1 -> SkillLevel.BEGINNER;
                case 3 -> SkillLevel.ADVANCED;
                case 4 -> SkillLevel.EXPERT;
                default -> SkillLevel.INTERMEDIATE;
            };
            userSkill.setLevel(targetLevel);
            if (ordinal(newStatus) > ordinal(userSkill.getStatus())) {
                userSkill.setStatus(newStatus);
            }
            userSkill.touch();
            userSkillRepository.save(userSkill);
        } catch (Exception ex) {
            log.warn("Skill status upgrade failed for path {}: {}", path.getId(), ex.getMessage());
        }
    }

    private int ordinal(SkillStatus status) {
        return switch (status) {
            case DECLARED -> 0;
            case EVALUATED -> 1;
            case DEMONSTRATED -> 2;
        };
    }

    private boolean hasContent(SkillEvidence evidence) {
        if (evidence == null) return false;
        if (evidence.getType().equals("LINK")) return evidence.getUrl() != null && !evidence.getUrl().isBlank();
        if (evidence.getType().equals("FILE")) return evidence.getFilePath() != null;
        return evidence.getDescription() != null && evidence.getDescription().length() >= 20;
    }

    private boolean hasStrongProof(SkillEvidence evidence) {
        if (evidence == null) return false;
        return switch (evidence.getType()) {
            case "LINK" -> evidence.getUrl() != null && evidence.getUrl().startsWith("http");
            case "FILE" -> evidence.getFilePath() != null;
            default -> false;
        };
    }

    // --- helpers --------------------------------------------------------------

    private LearningPath require(Long userId, Long id) {
        return pathRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> ApiException.notFound("Parcours introuvable."));
    }

    public LearningPathDto toDto(LearningPath path) {
        List<LearningTask> tasks = taskRepository.findByLearningPathIdOrderByPositionAscIdAsc(path.getId());
        List<SkillEvidence> evidences = evidenceRepository.findByLearningPathIdOrderByIdDesc(path.getId());
        int done = (int) tasks.stream().filter(LearningTask::isDone).count();
        int total = tasks.size();
        int percent = total == 0 ? 0 : Math.round((done * 100f) / total);
        return new LearningPathDto(path.getId(), path.getAnalysisId(), path.getAnalysisDetailId(),
                path.getSkillName(), path.getTitle(), path.getDescription(), path.getDifficulty(),
                readList(path.getObjectives()), readList(path.getTools()), readList(path.getSteps()),
                readList(path.getEvaluationCriteria()), path.getExpectedResult(), path.getProofRequired(),
                path.getStatus(), path.isAiGenerated(), path.isSimulatedAi(), path.getValidationNote(),
                path.getCreatedAt(), path.getCompletedAt(), total, done, percent,
                tasks.stream().map(t -> new TaskDto(t.getId(), t.getTitle(), t.getDescription(), t.getPosition(),
                        t.isDone(), t.getNote(), t.getDoneAt())).toList(),
                evidences.stream().map(e -> new com.skillmirror.learning.dto.LearningDtos.EvidenceDto(
                        e.getId(), e.getSkillId(), e.getSkillName(), e.getLearningPathId(), e.getProjectId(),
                        e.getType(), e.getUrl(), e.getFileName(), e.getDescription(), e.getStatus(),
                        e.getReviewNote(), e.getCreatedAt())).toList());
    }

    public LearningPathSummaryDto toSummary(LearningPath path) {
        long total = taskRepository.countByLearningPathId(path.getId());
        long done = taskRepository.countByLearningPathIdAndDoneTrue(path.getId());
        int percent = total == 0 ? 0 : (int) Math.round((done * 100.0) / total);
        return new LearningPathSummaryDto(path.getId(), path.getSkillName(), path.getTitle(),
                path.getDifficulty(), path.getStatus(), (int) total, (int) done, percent,
                path.isSimulatedAi(), path.getCreatedAt(), path.getCompletedAt());
    }

    private String writeList(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values == null ? List.of() : values);
        } catch (Exception ex) {
            return "[]";
        }
    }

    private List<String> readList(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception ex) {
            return List.of();
        }
    }
}
