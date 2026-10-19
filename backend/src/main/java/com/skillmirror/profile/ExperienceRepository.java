package com.skillmirror.profile;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExperienceRepository extends JpaRepository<Experience, Long> {

    List<Experience> findByUserIdOrderByIdDesc(Long userId);

    Optional<Experience> findByIdAndUserId(Long id, Long userId);
}
