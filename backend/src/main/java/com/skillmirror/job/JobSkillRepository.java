package com.skillmirror.job;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobSkillRepository extends JpaRepository<JobSkill, Long> {

    List<JobSkill> findByJobOfferIdOrderById(Long jobOfferId);

    void deleteByJobOfferId(Long jobOfferId);
}
