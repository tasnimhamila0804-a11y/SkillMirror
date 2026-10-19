package com.skillmirror.skill;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillRepository extends JpaRepository<Skill, Long> {

    Optional<Skill> findByNormalizedName(String normalizedName);

    boolean existsByNormalizedName(String normalizedName);

    List<Skill> findByCategoryOrderByName(SkillCategory category);

    List<Skill> findByNameContainingIgnoreCaseOrderByName(String name);

    List<Skill> findAllByOrderByName();
}
