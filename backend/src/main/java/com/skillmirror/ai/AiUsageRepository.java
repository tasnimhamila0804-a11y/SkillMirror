package com.skillmirror.ai;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AiUsageRepository extends JpaRepository<AiUsageLog, Long> {

    long countByUserIdAndCreatedAtAfter(Long userId, Instant since);

    @Query("select coalesce(sum(a.promptTokens + a.completionTokens), 0) from AiUsageLog a where a.createdAt >= :since")
    long totalTokensSince(@Param("since") Instant since);

    List<AiUsageLog> findTop50ByOrderByCreatedAtDesc();

    List<AiUsageLog> findTop30ByUserIdOrderByCreatedAtDesc(Long userId);

    long countByCreatedAtAfter(java.time.Instant since);

    long countByModeAndCreatedAtAfter(String mode, java.time.Instant since);

    @Query("select coalesce(sum(a.promptTokens), 0) from AiUsageLog a where a.createdAt >= :since")
    long promptTokensSince(@Param("since") Instant since);

    @Query("select coalesce(sum(a.completionTokens), 0) from AiUsageLog a where a.createdAt >= :since")
    long completionTokensSince(@Param("since") Instant since);

    long countByMode(String mode);

    long countByStatus(String status);

    @Query("""
           select a.purpose, a.mode, count(a),
                  coalesce(sum(a.promptTokens), 0), coalesce(sum(a.completionTokens), 0)
           from AiUsageLog a group by a.purpose, a.mode order by count(a) desc
           """)
    java.util.List<Object[]> aggregateByPurpose();
}
