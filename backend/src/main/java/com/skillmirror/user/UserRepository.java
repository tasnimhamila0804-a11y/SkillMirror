package com.skillmirror.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    org.springframework.data.domain.Page<User> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String name, String email, org.springframework.data.domain.Pageable pageable);

    long countByEnabled(boolean enabled);

    long countByRole(com.skillmirror.user.UserRole role);
}
