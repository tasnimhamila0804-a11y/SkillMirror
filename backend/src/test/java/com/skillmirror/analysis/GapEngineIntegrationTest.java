package com.skillmirror.analysis;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillmirror.analysis.dto.AnalysisDtos.AnalysisResultDto;
import com.skillmirror.auth.AuthService;
import com.skillmirror.auth.dto.AuthDtos.RegisterRequest;
import com.skillmirror.job.JobService;
import com.skillmirror.job.dto.JobDtos.ConfirmSkillsRequest;
import com.skillmirror.job.dto.JobDtos.ReviewedSkill;
import com.skillmirror.job.dto.JobDtos.SaveJobRequest;
import com.skillmirror.questionnaire.QuestionnaireService;
import com.skillmirror.questionnaire.dto.QuestionnaireDtos.CompleteRequest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Career Gap Engine rules, exercised end-to-end through the real services on H2:
 * weighted score, category classification, and "unknown is never acquired".
 */
@SpringBootTest
@ActiveProfiles("dev")
class GapEngineIntegrationTest {

    @Autowired private AuthService authService;
    @Autowired private QuestionnaireService questionnaireService;
    @Autowired private JobService jobService;
    @Autowired private AnalysisService analysisService;
    @Autowired private ObjectMapper objectMapper;

    private Long newUser(String email) {
        var response = authService.register(new RegisterRequest("Test User", email, "demo1234"));
        return response.user().id();
    }

    private void completeQuestionnaire(Long userId, String skillsJson) throws Exception {
        String payload = """
                {"situation":{"situation":"STUDENT","domain":"Informatique","educationLevel":"Licence"},
                 "skills":%s,
                 "objective":{"targetJob":"Developpeur","careerGoal":"FIRST_JOB","positionType":"JUNIOR"},
                 "preferences":{"hoursPerWeek":8,"learningPreference":"PROJECTS",
                                "preferredDifficulty":"INTERMEDIATE"}}
                """.formatted(skillsJson);
        questionnaireService.complete(userId, new CompleteRequest(objectMapper.readTree(payload)).payload());
    }

    private Long createConfirmedJob(Long userId, String title, String description,
                                    List<ReviewedSkill> skills) {
        var job = jobService.create(userId, new SaveJobRequest(title, "Acme", description, null, null));
        jobService.confirmSkills(userId, job.id(), new ConfirmSkillsRequest(skills));
        return job.id();
    }

    @Test
    void computesWeightedScoreFromRealDataAndExplainsIt() throws Exception {
        Long userId = newUser("gap-engine-" + System.nanoTime() + "@test.dev");
        completeQuestionnaire(userId,
                "[{\"name\":\"Java\",\"category\":\"LANGUAGES\",\"level\":\"INTERMEDIATE\"}]");

        Long jobId = createConfirmedJob(userId, "Dev Full Stack",
                "Poste demandant Java, MySQL et Kubernetes, Docker un plus.",
                List.of(
                        new ReviewedSkill("Java", "LANGUAGES", "REQUIRED", 3, "INTERMEDIATE"),
                        new ReviewedSkill("MySQL", "DATABASES", "REQUIRED", 3, null),
                        new ReviewedSkill("Kubernetes", "TOOLS", "REQUIRED", 3, null),
                        new ReviewedSkill("Docker", "TOOLS", "DESIRED", 1, null)));

        AnalysisResultDto result = analysisService.analyze(userId, jobId);

        // declared (unverified) Java at the required level = 0.75 mastery, everything else 0
        // score = (3 * 0.75) / (3 + 3 + 3 + 1) * 100 = 22.5 -> 23
        assertThat(result.analysis().score()).isEqualTo(23);
        assertThat(result.counts().total()).isEqualTo(4);
        assertThat(result.counts().declared()).isEqualTo(1);
        assertThat(result.counts().missing()).isEqualTo(3);
        assertThat(result.explanation()).contains("indicateur interne de compatibilité");
        assertThat(result.explanation()).contains("somme(poids");

        var java = result.details().stream().filter(d -> d.skillName().equals("Java")).findFirst().orElseThrow();
        assertThat(java.matchCategory()).isEqualTo("DECLARED");
        assertThat(java.masteryScore().doubleValue()).isEqualTo(0.75);

        var mysql = result.details().stream().filter(d -> d.skillName().equals("MySQL")).findFirst().orElseThrow();
        assertThat(mysql.matchCategory()).isEqualTo("MISSING");
        assertThat(mysql.masteryScore().doubleValue()).isZero();
        assertThat(mysql.priority()).isPositive();
    }

    @Test
    void neverTreatsUnknownLevelAsAcquired() throws Exception {
        Long userId = newUser("gap-unknown-" + System.nanoTime() + "@test.dev");
        completeQuestionnaire(userId,
                "[{\"name\":\"Docker\",\"category\":\"TOOLS\",\"level\":\"UNKNOWN\"}]");

        Long jobId = createConfirmedJob(userId, "DevOps",
                "Besoin de Docker en production.",
                List.of(new ReviewedSkill("Docker", "TOOLS", "REQUIRED", 3, null)));

        AnalysisResultDto result = analysisService.analyze(userId, jobId);

        var docker = result.details().get(0);
        assertThat(docker.matchCategory()).isEqualTo("MISSING");
        assertThat(docker.masteryScore().doubleValue()).isEqualTo(0.15);
        assertThat(docker.note()).contains("insuffisamment documentée");
        assertThat(result.analysis().score()).isEqualTo(15); // 0.15 * 3 / 3 * 100
    }

    @Test
    void partialLevelBelowRequirementIsPartialWithLowerMastery() throws Exception {
        Long userId = newUser("gap-partial-" + System.nanoTime() + "@test.dev");
        completeQuestionnaire(userId,
                "[{\"name\":\"SQL\",\"category\":\"LANGUAGES\",\"level\":\"BEGINNER\"}]");

        Long jobId = createConfirmedJob(userId, "Data Engineer",
                "SQL niveau avancé requis.",
                List.of(new ReviewedSkill("SQL", "LANGUAGES", "REQUIRED", 3, "INTERMEDIATE")));

        AnalysisResultDto result = analysisService.analyze(userId, jobId);

        var sql = result.details().get(0);
        assertThat(sql.matchCategory()).isEqualTo("PARTIAL");
        assertThat(sql.masteryScore().doubleValue()).isEqualTo(0.50);
        assertThat(result.analysis().score()).isEqualTo(50);
    }
}
