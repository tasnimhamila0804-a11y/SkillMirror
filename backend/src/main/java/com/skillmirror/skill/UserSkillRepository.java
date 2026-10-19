package com.skillmirror.skill;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {

    List<UserSkill> findByUserIdOrderById(Long userId);

    Optional<UserSkill> findByUserIdAndSkillId(Long userId, Long skillId);

    Optional<UserSkill> findByUserIdAndNormalizedName(Long userId, String normalizedName);

    Optional<UserSkill> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);

    List<UserSkill> findByUserIdAndStatus(Long userId, SkillStatus status);

    long countBySkillId(Long skillId);
}
