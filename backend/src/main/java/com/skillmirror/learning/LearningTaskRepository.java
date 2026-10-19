package com.skillmirror.learning;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LearningTaskRepository extends JpaRepository<LearningTask, Long> {

    List<LearningTask> findByLearningPathIdOrderByPositionAscIdAsc(Long learningPathId);

    Optional<LearningTask> findByIdAndLearningPathId(Long id, Long learningPathId);

    Optional<LearningTask> findTopByLearningPathIdOrderByIdDesc(Long learningPathId);

    long countByLearningPathIdAndDoneTrue(Long learningPathId);

    long countByLearningPathId(Long learningPathId);
}
