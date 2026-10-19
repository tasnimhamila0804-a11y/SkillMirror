package com.skillmirror.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillmirror.analysis.AnalysisService;
import com.skillmirror.auth.AuthService;
import com.skillmirror.auth.dto.AuthDtos.RegisterRequest;
import com.skillmirror.common.ApiException;
import com.skillmirror.job.JobService;
import com.skillmirror.job.dto.JobDtos.ConfirmSkillsRequest;
import com.skillmirror.job.dto.JobDtos.ReviewedSkill;
import com.skillmirror.job.dto.JobDtos.SaveJobRequest;
import com.skillmirror.learning.EvidenceService;
import com.skillmirror.learning.LearningService;
import com.skillmirror.learning.dto.LearningDtos.GenerateRequest;
import com.skillmirror.learning.dto.LearningDtos.SubmitEvidenceRequest;
import com.skillmirror.learning.dto.LearningDtos.TaskUpdateRequest;
import com.skillmirror.questionnaire.QuestionnaireService;
import com.skillmirror.questionnaire.dto.QuestionnaireDtos.CompleteRequest;
import com.skillmirror.user.User;
import com.skillmirror.user.UserRepository;
import com.skillmirror.user.UserRole;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Clearly identified demonstration data, activated with the Spring profile `demo`.
 * Creates two demo accounts and walks a complete journey through the real services,
 * so the platform can be presented with realistic data.
 *
 * Accounts: demo@skillmirror.local / Demo1234!  and  admin@skillmirror.local / Admin1234!
 */
@Component
@Profile("demo")
public class DemoDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);

    private static final String DEMO_EMAIL = "demo@skillmirror.local";
    private static final String ADMIN_EMAIL = "admin@skillmirror.local";

    private static final String DEMO_QUESTIONNAIRE = """
            {
              "situation": {
                "situation": "YOUNG_GRADUATE",
                "domain": "Informatique",
                "educationLevel": "Master 1 Informatique",
                "experienceSummary": "Stage de 3 mois en développement web + projets universitaires"
              },
              "skills": [
                {"name": "Java", "category": "LANGUAGES", "level": "INTERMEDIATE"},
                {"name": "SQL", "category": "LANGUAGES", "level": "BEGINNER"},
                {"name": "Git", "category": "TOOLS", "level": "INTERMEDIATE"},
                {"name": "Angular", "category": "WEB", "level": "BEGINNER"},
                {"name": "Communication", "category": "SOFT_SKILLS", "level": "INTERMEDIATE"}
              ],
              "experience": {
                "projects": [
                  {"name": "Gestion de bibliothèque", "description": "Application web de gestion de prêts",
                   "repositoryUrl": "https://github.com/demo/bibliotheque", "technologies": "Java, MySQL, Angular"}
                ],
                "experiences": [
                  {"title": "Stagiaire développeur web", "organization": "Startup Locale",
                   "type": "INTERNSHIP", "description": "Développement d'une API REST et d'un front Angular",
                   "startDate": "2025-04", "endDate": "2025-07", "current": false}
                ],
                "certificates": [
                  {"name": "SQL avancé", "issuer": "OpenClassrooms", "issuedDate": "2025-02"}
                ],
                "githubUrl": "https://github.com/demo",
                "portfolioUrl": null
              },
              "objective": {
                "targetJob": "Développeur Full Stack",
                "careerGoal": "FIRST_JOB",
                "targetCompany": "",
                "positionType": "JUNIOR"
              },
              "preferences": {
                "hoursPerWeek": 8,
                "learningPreference": "PROJECTS",
                "preferredDifficulty": "INTERMEDIATE",
                "deadline": "6 mois"
              }
            }
            """;

    private static final String DEMO_JOB = """
            Nous recherchons un(e) Développeur Full Stack Junior pour rejoindre notre équipe produit.

            Missions :
            - Développer et maintenir des fonctionnalités côté back-end (Java, Spring Boot)
            - Concevoir des API REST documentées et consommer un front Angular
            - Modéliser et interroger une base MySQL
            - Participer aux revues de code et au travail en équipe agile

            Profil recherché :
            - Bac+3/5 en informatique ou équivalent
            - Bonnes compétences en Java et SQL (requis)
            - Notions de Docker (apprécié)
            - Anglais technique lu (apprécié)
            - Rigueur, communication et esprit d'équipe
            """;

    private final UserRepository userRepository;
    private final AuthService authService;
    private final QuestionnaireService questionnaireService;
    private final JobService jobService;
    private final AnalysisService analysisService;
    private final LearningService learningService;
    private final EvidenceService evidenceService;
    private final ObjectMapper objectMapper;

    public DemoDataInitializer(UserRepository userRepository, AuthService authService,
                               QuestionnaireService questionnaireService, JobService jobService,
                               AnalysisService analysisService, LearningService learningService,
                               EvidenceService evidenceService, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.authService = authService;
        this.questionnaireService = questionnaireService;
        this.jobService = jobService;
        this.analysisService = analysisService;
        this.learningService = learningService;
        this.evidenceService = evidenceService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByEmailIgnoreCase(ADMIN_EMAIL).isPresent()) {
            log.info("Demo data already present — skipping initialization.");
            return;
        }
        try {
            buildAdmin();
            Long demoUserId = buildDemoUser();
            buildJourney(demoUserId);
            log.info("===========================================================");
            log.info("DEMO DATA READY (clearly identified demo accounts):");
            log.info("  user : {} / Demo1234!", DEMO_EMAIL);
            log.info("  admin: {} / Admin1234!", ADMIN_EMAIL);
            log.info("===========================================================");
        } catch (Exception ex) {
            log.warn("Demo data initialization failed: {}", ex.getMessage());
        }
    }

    private void buildAdmin() {
        var response = authService.register(new RegisterRequest("Admin SkillMirror", ADMIN_EMAIL, "Admin1234!"));
        User admin = userRepository.findByEmailIgnoreCase(ADMIN_EMAIL).orElseThrow();
        admin.setRole(UserRole.ADMIN);
        admin.setProfileCompleted(true);
        userRepository.save(admin);
        log.info("Demo admin created (id={})", response.user().id());
    }

    private Long buildDemoUser() throws Exception {
        var response = authService.register(new RegisterRequest("Amina Ben Salah", DEMO_EMAIL, "Demo1234!"));
        Long userId = response.user().id();
        questionnaireService.complete(userId,
                new CompleteRequest(objectMapper.readTree(DEMO_QUESTIONNAIRE)).payload());
        return userId;
    }

    private void buildJourney(Long userId) throws Exception {
        var job = jobService.create(userId, new SaveJobRequest(
                "Développeur Full Stack Junior", "NovaTech", DEMO_JOB, null, null));
        jobService.extract(userId, job.id());

        var jobSkills = jobService.rawJobSkills(job.id());
        List<ReviewedSkill> reviewed = jobSkills.stream()
                .map(s -> new ReviewedSkill(s.getSkillName(), s.getCategory(), s.getRequirement(),
                        s.getWeight(), s.getRequiredLevel()))
                .toList();
        jobService.confirmSkills(userId, job.id(), new ConfirmSkillsRequest(reviewed));

        var analysis = analysisService.analyze(userId, job.id());
        log.info("Demo analysis computed: score {}/100", analysis.analysis().score());

        var paths = learningService.generate(userId, new GenerateRequest(analysis.analysis().id(), 2));
        if (!paths.isEmpty()) {
            Long pathId = paths.get(0).id();
            var full = learningService.get(userId, pathId);
            // complete every task so the demo path reaches a validatable state
            full.tasks().forEach(task ->
                    learningService.updateTask(userId, pathId, task.id(), new TaskUpdateRequest(true, null)));
            evidenceService.submit(userId, pathId, new SubmitEvidenceRequest(
                    "LINK", "https://github.com/demo/skillmirror-proof", "Dépôt public du projet réalisé.", null));
            try {
                learningService.complete(userId, pathId);
                log.info("Demo learning path validated (skill demonstrated).");
            } catch (ApiException ex) {
                log.info("Demo path not validated yet: {}", ex.getMessage());
            }
        }
    }
}
