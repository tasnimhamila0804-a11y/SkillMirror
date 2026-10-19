package com.skillmirror.admin;

import com.skillmirror.admin.dto.AdminDtos.AdminSkillDto;
import com.skillmirror.admin.dto.AdminDtos.AdminUserDto;
import com.skillmirror.admin.dto.AdminDtos.AiUsageReportDto;
import com.skillmirror.admin.dto.AdminDtos.ErrorDto;
import com.skillmirror.admin.dto.AdminDtos.OverviewDto;
import com.skillmirror.admin.dto.AdminDtos.SkillRequest;
import com.skillmirror.admin.dto.AdminDtos.UsageAggDto;
import com.skillmirror.admin.dto.AdminDtos.UsageItemDto;
import com.skillmirror.admin.dto.AdminDtos.UserUpdateRequest;
import com.skillmirror.ai.AiService;
import com.skillmirror.ai.AiUsageLog;
import com.skillmirror.ai.AiUsageRepository;
import com.skillmirror.analysis.AnalysisRepository;
import com.skillmirror.common.ApiException;
import com.skillmirror.common.ErrorLog;
import com.skillmirror.common.ErrorLogRepository;
import com.skillmirror.job.JobOfferRepository;
import com.skillmirror.learning.LearningPathRepository;
import com.skillmirror.learning.SkillEvidenceRepository;
import com.skillmirror.skill.Skill;
import com.skillmirror.skill.SkillRepository;
import com.skillmirror.skill.SkillService;
import com.skillmirror.skill.UserSkillRepository;
import com.skillmirror.user.User;
import com.skillmirror.user.UserRepository;
import com.skillmirror.user.UserRole;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Administration use cases. Least privilege: admins manage accounts, the skill reference,
 * AI usage and technical errors — they never access private documents or personal profile content.
 */
@Service
public class AdminService {

    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;
    private final SkillRepository skillRepository;
    private final AnalysisRepository analysisRepository;
    private final JobOfferRepository jobRepository;
    private final LearningPathRepository pathRepository;
    private final SkillEvidenceRepository evidenceRepository;
    private final ErrorLogRepository errorLogRepository;
    private final AiUsageRepository aiUsageRepository;
    private final SettingsService settingsService;
    private final AiService aiService;

    public AdminService(UserRepository userRepository, UserSkillRepository userSkillRepository,
                        SkillRepository skillRepository, AnalysisRepository analysisRepository,
                        JobOfferRepository jobRepository, LearningPathRepository pathRepository,
                        SkillEvidenceRepository evidenceRepository, ErrorLogRepository errorLogRepository,
                        AiUsageRepository aiUsageRepository, SettingsService settingsService,
                        AiService aiService) {
        this.userRepository = userRepository;
        this.userSkillRepository = userSkillRepository;
        this.skillRepository = skillRepository;
        this.analysisRepository = analysisRepository;
        this.jobRepository = jobRepository;
        this.pathRepository = pathRepository;
        this.evidenceRepository = evidenceRepository;
        this.errorLogRepository = errorLogRepository;
        this.aiUsageRepository = aiUsageRepository;
        this.settingsService = settingsService;
        this.aiService = aiService;
    }

    // --- users ---------------------------------------------------------------

    public List<AdminUserDto> users(String query, int page, int size) {
        PageRequest pageable = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> result = (query == null || query.isBlank())
                ? userRepository.findAll(pageable)
                : userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        query.trim(), query.trim(), pageable);
        return result.getContent().stream().map(this::toUserDto).toList();
    }

    @Transactional
    public AdminUserDto updateUser(Long callerId, Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("Utilisateur introuvable."));
        if (Boolean.FALSE.equals(request.enabled()) && user.getId().equals(callerId)) {
            throw ApiException.badRequest("CANNOT_DISABLE_SELF",
                    "Vous ne pouvez pas désactiver votre propre compte.");
        }
        if (request.role() != null) {
            UserRole role;
            try {
                role = UserRole.valueOf(request.role().trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                throw ApiException.badRequest("INVALID_ROLE", "Rôle invalide.");
            }
            if (role == UserRole.USER && user.getRole() == UserRole.ADMIN && user.getId().equals(callerId)) {
                throw ApiException.badRequest("CANNOT_DEMOTE_SELF",
                        "Vous ne pouvez pas retirer votre propre rôle administrateur.");
            }
            user.setRole(role);
        }
        if (request.enabled() != null) {
            user.setEnabled(request.enabled());
        }
        user.touch();
        return toUserDto(userRepository.save(user));
    }

    private AdminUserDto toUserDto(User user) {
        return new AdminUserDto(user.getId(), user.getName(), user.getEmail(), user.getRole().name(),
                user.isEnabled(), user.isProfileCompleted(), user.getCreatedAt(), user.getLastLoginAt(),
                userSkillRepository.countByUserId(user.getId()),
                analysisRepository.countByUserId(user.getId()));
    }

    // --- skill reference -------------------------------------------------------

    public List<AdminSkillDto> skills() {
        return skillRepository.findAllByOrderByName().stream()
                .map(s -> new AdminSkillDto(s.getId(), s.getName(), s.getCategory().name(), s.isCustom(),
                        s.getCreatedBy()))
                .toList();
    }

    @Transactional
    public AdminSkillDto createSkill(Long adminId, SkillRequest request) {
        String normalized = Skill.normalize(request.name());
        if (normalized.isEmpty()) {
            throw ApiException.badRequest("SKILL_NAME_REQUIRED", "Le nom de la compétence est requis.");
        }
        if (skillRepository.existsByNormalizedName(normalized)) {
            throw ApiException.conflict("SKILL_EXISTS", "Cette compétence existe déjà dans le référentiel.");
        }
        Skill skill = new Skill();
        skill.setName(request.name().trim());
        skill.setNormalizedName(normalized);
        skill.setCategory(SkillService.parseCategory(request.category()));
        skill.setCustom(false);
        skill.setCreatedBy(adminId);
        skill = skillRepository.save(skill);
        return new AdminSkillDto(skill.getId(), skill.getName(), skill.getCategory().name(), skill.isCustom(),
                skill.getCreatedBy());
    }

    @Transactional
    public AdminSkillDto updateSkill(Long id, SkillRequest request) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Compétence introuvable."));
        String normalized = Skill.normalize(request.name());
        skillRepository.findByNormalizedName(normalized).ifPresent(existing -> {
            if (!existing.getId().equals(skill.getId())) {
                throw ApiException.conflict("SKILL_EXISTS", "Une compétence porte déjà ce nom.");
            }
        });
        skill.setName(request.name().trim());
        skill.setNormalizedName(normalized);
        skill.setCategory(SkillService.parseCategory(request.category()));
        skillRepository.save(skill);
        return new AdminSkillDto(skill.getId(), skill.getName(), skill.getCategory().name(), skill.isCustom(),
                skill.getCreatedBy());
    }

    @Transactional
    public void deleteSkill(Long id) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Compétence introuvable."));
        long usedBy = userSkillRepository.countBySkillId(id);
        if (usedBy > 0) {
            throw ApiException.conflict("SKILL_IN_USE",
                    "Compétence utilisée par " + usedBy + " profil(s) : suppression refusée.");
        }
        skillRepository.delete(skill);
    }

    // --- supervision ------------------------------------------------------------

    public AiUsageReportDto aiUsage() {
        long total = aiUsageRepository.count();
        long simulated = aiUsageRepository.countByMode("SIMULATED");
        long live = aiUsageRepository.countByMode("LIVE");
        long errors = aiUsageRepository.countByStatus("ERROR");
        long promptTokens = aiUsageRepository.promptTokensSince(Instant.EPOCH);
        long completionTokens = aiUsageRepository.completionTokensSince(Instant.EPOCH);

        List<UsageAggDto> byPurpose = new ArrayList<>();
        for (Object[] row : aiUsageRepository.aggregateByPurpose()) {
            byPurpose.add(new UsageAggDto(String.valueOf(row[0]), String.valueOf(row[1]),
                    ((Number) row[2]).longValue(), ((Number) row[3]).longValue(),
                    ((Number) row[4]).longValue()));
        }
        List<UsageItemDto> recent = aiUsageRepository.findTop50ByOrderByCreatedAtDesc().stream()
                .map(AdminService::toUsageItem)
                .toList();
        return new AiUsageReportDto(total, simulated, live, errors, promptTokens, completionTokens,
                byPurpose, recent);
    }

    public List<ErrorDto> errors() {
        return errorLogRepository.findTop100ByOrderByCreatedAtDesc().stream()
                .map(AdminService::toError)
                .toList();
    }

    public OverviewDto overview() {
        long paths = pathRepository.count();
        long validatedPaths = countValidated();
        return new OverviewDto(
                userRepository.count(),
                userRepository.countByEnabled(true),
                userRepository.countByRole(UserRole.ADMIN),
                skillRepository.count(),
                jobRepository.count(),
                analysisRepository.count(),
                paths,
                validatedPaths,
                evidenceRepository.count(),
                aiUsageRepository.count(),
                errorLogRepository.count(),
                aiService.isLiveMode());
    }

    private long countValidated() {
        // count of validated paths without a repository method for "any user"
        return pathRepository.findAll().stream()
                .filter(p -> "VALIDATED".equals(p.getStatus()) || "COMPLETED".equals(p.getStatus()))
                .count();
    }

    private static UsageItemDto toUsageItem(AiUsageLog log) {
        return new UsageItemDto(log.getId(), log.getUserId(), log.getPurpose(), log.getMode(), log.getModel(),
                log.getPromptTokens(), log.getCompletionTokens(), log.getLatencyMs(), log.getStatus(),
                log.getErrorMessage(), log.getCreatedAt());
    }

    private static ErrorDto toError(ErrorLog log) {
        return new ErrorDto(log.getId(), log.getLevel(), log.getMessage(), log.getPath(), log.getMethod(),
                log.getUserId(), log.getCreatedAt());
    }
}
