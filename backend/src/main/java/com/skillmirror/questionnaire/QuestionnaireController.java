package com.skillmirror.questionnaire;

import com.skillmirror.questionnaire.dto.QuestionnaireDtos.CompleteRequest;
import com.skillmirror.questionnaire.dto.QuestionnaireDtos.CompleteResponse;
import com.skillmirror.questionnaire.dto.QuestionnaireDtos.QuestionnaireDefinition;
import com.skillmirror.questionnaire.dto.QuestionnaireDtos.ResponseDto;
import com.skillmirror.questionnaire.dto.QuestionnaireDtos.SaveRequest;
import com.skillmirror.security.AuthPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/questionnaire")
public class QuestionnaireController {

    private final QuestionnaireService questionnaireService;

    public QuestionnaireController(QuestionnaireService questionnaireService) {
        this.questionnaireService = questionnaireService;
    }

    @GetMapping
    public QuestionnaireDefinition definition() {
        return questionnaireService.definition();
    }

    @GetMapping("/responses")
    public ResponseDto mine(@AuthenticationPrincipal AuthPrincipal principal) {
        return questionnaireService.get(principal.id());
    }

    @PutMapping("/responses")
    public ResponseDto save(@AuthenticationPrincipal AuthPrincipal principal,
                            @RequestBody SaveRequest request) {
        return questionnaireService.save(principal.id(), request);
    }

    @PostMapping("/complete")
    public ResponseEntity<CompleteResponse> complete(@AuthenticationPrincipal AuthPrincipal principal,
                                                     @Valid @RequestBody CompleteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(questionnaireService.complete(principal.id(), request.payload()));
    }
}
