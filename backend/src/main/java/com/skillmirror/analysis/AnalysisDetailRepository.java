package com.skillmirror.analysis;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisDetailRepository extends JpaRepository<AnalysisDetail, Long> {

    List<AnalysisDetail> findByAnalysisIdOrderByPriorityAscIdAsc(Long analysisId);

    List<AnalysisDetail> findByAnalysisIdAndPriorityGreaterThanOrderByPriorityAsc(Long analysisId, int priority);

    void deleteByAnalysisId(Long analysisId);
}
