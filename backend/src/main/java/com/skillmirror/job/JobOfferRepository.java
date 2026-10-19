package com.skillmirror.job;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobOfferRepository extends JpaRepository<JobOffer, Long> {

    List<JobOffer> findByUserIdOrderByIdDesc(Long userId);

    Optional<JobOffer> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);
}
