package com.skillmirror.learning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillmirror.analysis.AnalysisService;
import com.skillmirror.analysis.dto.AnalysisDtos.AnalysisResultDto;
import com.skillmirror.auth.AuthService;
import com.skillmirror.auth.dto.AuthDtos.RegisterRequest;
import com.skillmirror.common.ApiException;
import com.skillmirror.job.JobService;
import com.skillmirror.job.dto.JobDtos.ConfirmSkillsRequest;
import com.skillmirror.job.dto.JobDtos.ReviewedSkill;
import com.skillmirror.job.dto.JobDtos.SaveJobRequest;
import com.skillmirror.learning.dto.LearningDtos.CompletionCheckDto;
import com.skillmirror.learning.dto.LearningDtos.GenerateRequest;
import com.skillmirror.learning.dto.LearningDtos.LearningPathSummaryDto;
import com.skillmirror.learning.dto.LearningDtos.SubmitEvidenceRequest;
import com.skillmirror.learning.dto.LearningDtos.TaskUpdateRequest;
import com.skillmirror.questionnaire.QuestionnaireService;
import com.skillmirror.questionnaire.dto.QuestionnaireDtos.CompleteRequest;
import com.skillmirror.skill.SkillStatus;
import com.skillmirror.skill.UserSkill;
import com.skillmirror.skill.UserSkillRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Project-to-Proof rules: completing tasks alone NEVER certifies a skill. The backend must
 * verify the explicit criteria (all tasks + a submitted proof) before upgrading a status.
 */
@SpringBootTest
@ActiveProfiles("dev")
class LearningValidationIntegrationTest {

    @Autowired private AuthService authService;
    @Autowired private QuestionnaireService questionnaireService;
    @Autowired private JobService jobService;
    @Autowired private AnalysisService analysisService;
    @Autowired private LearningService learningService;
    @Autowired private EvidenceService evidenceService;
    @Autowired private UserSkillRepository userSkillRepository;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void validatesPathOnlyWhenAllCriteriaAndProofAreMet() throws Exception {
        Long userId = authService.register(
                new RegisterRequest("Learner", "learn-" + System.nanoTime() + "@test.dev", "demo1234"))
                .user().id();

        String payload = """
                {"situation":{"situation":"YOUNG_GRADUATE","domain":"Informatique"},
                 "skills":[{"name":"SQL","category":"LANGUAGES","level":"BEGINNER"},
                           {"name":"Java","category":"LANGUAGES","level":"INTERMEDIATE"}],
                 "objective":{"targetJob":"Data Analyst","careerGoal":"FIRST_JOB","positionType":"JUNIOR"},
                 "preferences":{"hoursPerWeek":6,"learningPreference":"PROJECTS",
                                "preferredDifficulty":"INTERMEDIATE"}}
                """;
        questionnaireService.complete(userId, new CompleteRequest(objectMapper.readTree(payload)).payload());

        var job = jobService.create(userId, new SaveJobRequest("Data Analyst", "Acme",
                "SQL avancé requis pour les analyses.", null, null));
        jobService.confirmSkills(userId, job.id(), new ConfirmSkillsRequest(List.of(
                new ReviewedSkill("SQL", "LANGUAGES", "REQUIRED", 3, "ADVANCED"),
                new ReviewedSkill("Java", "LANGUAGES", "REQUIRED", 3, "INTERMEDIATE"))));

        AnalysisResultDto analysis = analysisService.analyze(userId, job.id());
        assertThat(analysis.counts().partial()).isEqualTo(1); // SQL below required level

        List<LearningPathSummaryDto> generated =
                learningService.generate(userId, new GenerateRequest(analysis.analysis().id(), 3));
        assertThat(generated).isNotEmpty();

        Long pathId = generated.stream()
                .filter(path -> path.skillName().equalsIgnoreCase("SQL"))
                .map(LearningPathSummaryDto::id)
                .findFirst()
                .orElse(generated.get(0).id());

        // 1. no tasks done, no proof -> refused
        CompletionCheckDto initial = learningService.checkCompletion(userId, pathId);
        assertThat(initial.canComplete()).isFalse();
        assertThatThrownBy(() -> learningService.complete(userId, pathId))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Validation impossible");

        // 2. tasks done but no proof -> still refused
        var path = learningService.get(userId, pathId);
        assertThat(path.tasks()).isNotEmpty();
        path.tasks().forEach(task ->
                learningService.updateTask(userId, pathId, task.id(), new TaskUpdateRequest(true, null)));
        CompletionCheckDto tasksOnly = learningService.checkCompletion(userId, pathId);
        assertThat(tasksOnly.doneTasks()).isEqualTo(tasksOnly.totalTasks());
        assertThat(tasksOnly.hasProof()).isFalse();
        assertThatThrownBy(() -> learningService.complete(userId, pathId))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("preuve");

        // 3. proof submitted -> validation succeeds and the skill status improves
        evidenceService.submit(userId, pathId, new SubmitEvidenceRequest(
                "LINK", "https://github.com/learner/sql-proof", "Depot du projet SQL", null));

        var validated = learningService.complete(userId, pathId);
        assertThat(validated.status()).isEqualTo("VALIDATED");
        assertThat(validated.validationNote()).contains("tâches complètes");

        UserSkill sql = userSkillRepository.findByUserIdAndNormalizedName(userId, "sql").orElseThrow();
        assertThat(sql.getStatus()).isEqualTo(SkillStatus.DEMONSTRATED);
        assertThat(sql.getLevel().getRank()).isGreaterThanOrEqualTo(2);
    }
}
