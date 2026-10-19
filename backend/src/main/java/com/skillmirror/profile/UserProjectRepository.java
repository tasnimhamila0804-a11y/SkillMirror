package com.skillmirror.profile;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProjectRepository extends JpaRepository<UserProject, Long> {

    List<UserProject> findByUserIdOrderByIdDesc(Long userId);

    Optional<UserProject> findByIdAndUserId(Long id, Long userId);
}
