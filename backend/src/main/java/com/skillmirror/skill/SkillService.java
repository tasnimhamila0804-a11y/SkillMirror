package com.skillmirror.skill;

import com.skillmirror.common.ApiException;
import com.skillmirror.skill.dto.SkillDtos.AddSkillRequest;
import com.skillmirror.skill.dto.SkillDtos.SkillDto;
import com.skillmirror.skill.dto.SkillDtos.UpdateSkillRequest;
import com.skillmirror.skill.dto.SkillDtos.UserSkillDto;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Skill reference and "my skills" use cases. */
@Service
public class SkillService {

    private final SkillRepository skillRepository;
    private final UserSkillRepository userSkillRepository;

    public SkillService(SkillRepository skillRepository, UserSkillRepository userSkillRepository) {
        this.skillRepository = skillRepository;
        this.userSkillRepository = userSkillRepository;
    }

    public List<SkillDto> reference(String category, String q) {
        List<Skill> skills;
        if (q != null && !q.isBlank()) {
            skills = skillRepository.findByNameContainingIgnoreCaseOrderByName(q.trim());
        } else if (category != null && !category.isBlank()) {
            skills = skillRepository.findByCategoryOrderByName(parseCategory(category));
        } else {
            skills = skillRepository.findAllByOrderByName();
        }
        if (category != null && !category.isBlank() && q != null && !q.isBlank()) {
            SkillCategory cat = parseCategory(category);
            skills = skills.stream().filter(s -> s.getCategory() == cat).toList();
        }
        return skills.stream().map(SkillService::toDto).toList();
    }

    public SkillDto get(Long id) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Compétence introuvable."));
        return toDto(skill);
    }

    public List<UserSkillDto> mySkills(Long userId) {
        return userSkillRepository.findByUserIdOrderById(userId).stream().map(SkillService::toDto).toList();
    }

    @Transactional
    public UserSkillDto addSkill(Long userId, AddSkillRequest request) {
        Skill skill = resolveSkill(request, userId);
        if (userSkillRepository.findByUserIdAndSkillId(userId, skill.getId()).isPresent()) {
            throw ApiException.conflict("SKILL_ALREADY_ADDED", "Cette compétence est déjà dans votre profil.");
        }
        UserSkill userSkill = new UserSkill();
        userSkill.setUserId(userId);
        applySkill(userSkill, skill);
        userSkill.setLevel(SkillLevel.from(request.level()));
        userSkill.setStatus(SkillStatus.DECLARED);
        userSkill.setNotes(request.notes());
        return toDto(userSkillRepository.save(userSkill));
    }

    @Transactional
    public UserSkillDto updateSkill(Long userId, Long id, UpdateSkillRequest request) {
        UserSkill userSkill = require(userId, id);
        if (request.level() != null && !request.level().isBlank()) {
            SkillLevel newLevel = SkillLevel.from(request.level());
            // A demonstrated skill cannot silently regress to "unknown" through self-assessment.
            if (!(userSkill.getStatus() == SkillStatus.DEMONSTRATED && newLevel == SkillLevel.UNKNOWN)) {
                userSkill.setLevel(newLevel);
            }
        }
        if (request.notes() != null) {
            userSkill.setNotes(request.notes());
        }
        userSkill.touch();
        return toDto(userSkillRepository.save(userSkill));
    }

    @Transactional
    public void removeSkill(Long userId, Long id) {
        UserSkill userSkill = require(userId, id);
        userSkillRepository.delete(userSkill);
    }

    @Transactional
    public Skill resolveOrCreate(String rawName, String rawCategory, Long createdBy) {
        String name = rawName == null ? "" : rawName.trim().replaceAll("\\s+", " ");
        if (name.isEmpty()) {
            throw ApiException.badRequest("SKILL_NAME_REQUIRED", "Le nom de la compétence est requis.");
        }
        String normalized = Skill.normalize(name);
        Skill existing = skillRepository.findByNormalizedName(normalized).orElse(null);
        if (existing != null) return existing;

        Skill skill = new Skill();
        skill.setName(name.length() > 120 ? name.substring(0, 120) : name);
        skill.setNormalizedName(normalized.length() > 140 ? normalized.substring(0, 140) : normalized);
        skill.setCategory(parseCategory(rawCategory));
        skill.setCustom(true);
        skill.setCreatedBy(createdBy);
        return skillRepository.save(skill);
    }

    public UserSkill requireUserSkill(Long userId, Long userSkillId) {
        return require(userId, userSkillId);
    }

    public List<UserSkill> rawUserSkills(Long userId) {
        return userSkillRepository.findByUserIdOrderById(userId);
    }

    private UserSkill require(Long userId, Long id) {
        return userSkillRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> ApiException.notFound("Compétence introuvable dans votre profil."));
    }

    private Skill resolveSkill(AddSkillRequest request, Long userId) {
        if (request.skillId() != null) {
            return skillRepository.findById(request.skillId())
                    .orElseThrow(() -> ApiException.notFound("Compétence introuvable."));
        }
        if (request.name() == null || request.name().isBlank()) {
            throw ApiException.badRequest("SKILL_REQUIRED", "Sélectionnez ou saisissez une compétence.");
        }
        return resolveOrCreate(request.name(), request.category(), userId);
    }

    private void applySkill(UserSkill userSkill, Skill skill) {
        userSkill.setSkillId(skill.getId());
        userSkill.setSkillName(skill.getName());
        userSkill.setNormalizedName(skill.getNormalizedName());
        userSkill.setCategory(skill.getCategory().name());
    }

    public static SkillCategory parseCategory(String raw) {
        if (raw == null || raw.isBlank()) return SkillCategory.OTHER;
        try {
            return SkillCategory.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return SkillCategory.OTHER;
        }
    }

    public static SkillDto toDto(Skill skill) {
        return new SkillDto(skill.getId(), skill.getName(), skill.getCategory().name(), skill.isCustom());
    }

    public static UserSkillDto toDto(UserSkill userSkill) {
        return new UserSkillDto(userSkill.getId(), userSkill.getSkillId(), userSkill.getSkillName(),
                userSkill.getCategory(), userSkill.getLevel().name(), userSkill.getStatus().name(),
                userSkill.getNotes(), userSkill.getUpdatedAt());
    }
}
