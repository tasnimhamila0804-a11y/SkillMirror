package com.skillmirror.questionnaire;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionnaireResponseRepository extends JpaRepository<QuestionnaireResponse, Long> {

    Optional<QuestionnaireResponse> findByUserId(Long userId);
}
