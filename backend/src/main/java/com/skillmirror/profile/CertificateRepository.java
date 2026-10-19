package com.skillmirror.profile;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    List<Certificate> findByUserIdOrderByIdDesc(Long userId);

    Optional<Certificate> findByIdAndUserId(Long id, Long userId);
}
