package com.skillmirror.analysis;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisRepository extends JpaRepository<Analysis, Long> {

    List<Analysis> findByUserIdOrderByIdDesc(Long userId);

    Optional<Analysis> findByIdAndUserId(Long id, Long userId);

    Optional<Analysis> findTopByUserIdOrderByIdDesc(Long userId);

    long countByUserId(Long userId);
}
