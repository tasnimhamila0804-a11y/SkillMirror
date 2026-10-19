package com.skillmirror.questionnaire.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.skillmirror.profile.dto.ProfileDtos.ProfileDto;
import java.time.Instant;
import java.util.List;

public final class QuestionnaireDtos {

    private QuestionnaireDtos() {}

    public record StepDef(String id, int index, String title, String description, List<String> fields) {}

    public record QuestionnaireDefinition(String signature, List<StepDef> steps) {}

    public record SaveRequest(Integer currentStep, JsonNode payload) {}

    public record ResponseDto(Long id, int currentStep, boolean completed, JsonNode payload, Instant updatedAt) {}

    public record CompleteRequest(JsonNode payload) {}

    public record CompleteResponse(ProfileDto profile, int skills, int experiences, int projects,
                                   int certificates, boolean profileCompleted) {}
}
