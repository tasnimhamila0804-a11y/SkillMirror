package com.skillmirror.learning;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillEvidenceRepository extends JpaRepository<SkillEvidence, Long> {

    List<SkillEvidence> findByUserIdOrderByIdDesc(Long userId);

    List<SkillEvidence> findByLearningPathIdOrderByIdDesc(Long learningPathId);

    Optional<SkillEvidence> findByIdAndUserId(Long id, Long userId);

    Optional<SkillEvidence> findByIdAndLearningPathId(Long id, Long learningPathId);

    long countByUserIdAndStatus(Long userId, String status);
}
