package com.skillmirror.learning;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LearningPathRepository extends JpaRepository<LearningPath, Long> {

    List<LearningPath> findByUserIdOrderByIdDesc(Long userId);

    List<LearningPath> findByUserIdAndStatusOrderByIdDesc(Long userId, String status);

    Optional<LearningPath> findByIdAndUserId(Long id, Long userId);

    Optional<LearningPath> findTopByUserIdAndAnalysisDetailIdAndStatusNotIn(Long userId, Long analysisDetailId,
                                                                            List<String> statuses);

    long countByUserIdAndStatus(Long userId, String status);
}
