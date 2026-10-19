package com.skillmirror.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillmirror.ai.dto.AiDtos.AnalysisText;
import com.skillmirror.ai.dto.AiDtos.ExtractionResult;
import com.skillmirror.ai.dto.AiDtos.ExtractedSkill;
import com.skillmirror.ai.dto.AiDtos.GeneratedProject;
import com.skillmirror.admin.SettingsService;
import com.skillmirror.common.ApiException;
import com.skillmirror.config.SkillMirrorProperties;
import com.skillmirror.skill.SkillCategory;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * AI facade: chooses between the live OpenAI-compatible provider and the deterministic demo
 * engine, enforces quotas, records usage, and guarantees a graceful (and clearly flagged)
 * fallback whenever the provider fails.
 */
@Service
public class AiService {

    public static final String PURPOSE_EXTRACTION = "SKILL_EXTRACTION";
    public static final String PURPOSE_PROJECT = "PROJECT_GENERATION";
    public static final String PURPOSE_SUMMARY = "ANALYSIS_SUMMARY";

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    private static final String SYSTEM_EXTRACTION = """
            You extract the skill requirements from a job posting.
            Reply ONLY with minified JSON, no markdown:
            {"technical":[{"name":"...","category":"LANGUAGES|WEB|DATABASES|DATA|AI|TOOLS|SOFT_SKILLS|OTHER","requirement":"REQUIRED|DESIRED","weight":1|2|3,"requiredLevel":"BEGINNER|INTERMEDIATE|ADVANCED|EXPERT"}],
             "softSkills":[same shape as technical],
             "experienceLevel":"JUNIOR|MIDDLE|SENIOR",
             "educationRequirement":"..."}
            Rules: only skills actually present in the text; canonical short names ("Spring Boot", "MySQL");
            REQUIRED skills get weight 3, nice-to-have get weight 2 or 1; DESIRED only when the text says so;
            requiredLevel only when the text states an expected level; free-text fields in the language of the text.
            """;

    private static final String SYSTEM_PROJECT = """
            You are a senior career coach designing ONE realistic practice project that proves a missing skill.
            Reply ONLY with minified JSON, no markdown:
            {"title":"...","description":"...","difficulty":"BEGINNER|INTERMEDIATE|ADVANCED",
             "objectives":["..."],"tools":["..."],"steps":["..."],"tasks":["..."],
             "evaluationCriteria":["..."],"expectedResult":"...","proofRequired":"..."}
            The project must be doable alone, finishable in the announced weekly hours, and end with a public proof
            (repository or report). Write everything in the same language as the user's request.
            """;

    private final SkillMirrorProperties properties;
    private final OpenAiCompatibleClient liveClient;
    private final DemoIntelligence demo;
    private final AiUsageRepository usageRepository;
    private final ObjectMapper objectMapper;
    private final SettingsService settingsService;

    private enum Mode { LIVE, DEMO }

    public AiService(SkillMirrorProperties properties, OpenAiCompatibleClient liveClient,
                     DemoIntelligence demo, AiUsageRepository usageRepository, ObjectMapper objectMapper,
                     SettingsService settingsService) {
        this.properties = properties;
        this.liveClient = liveClient;
        this.demo = demo;
        this.usageRepository = usageRepository;
        this.objectMapper = objectMapper;
        this.settingsService = settingsService;
    }

    public boolean isLiveMode() {
        return "LIVE".equals(properties.getAi().effectiveMode()) && liveClient.supportsLiveCalls();
    }

    // --- skill extraction ---------------------------------------------------

    public ExtractionResult extractJobSkills(String jobText, Long userId) {
        if (resolveMode(userId, PURPOSE_EXTRACTION) == Mode.LIVE) {
            try {
                AiClient.AiCompletion completion = liveClient.complete(SYSTEM_EXTRACTION, truncate(jobText, 7000));
                ExtractionResult result = parseExtraction(completion.text());
                record(userId, PURPOSE_EXTRACTION, "LIVE", completion, "SUCCESS", null);
                return result;
            } catch (Exception ex) {
                log.warn("Live extraction failed, falling back to demo engine: {}", ex.getMessage());
                record(userId, PURPOSE_EXTRACTION, "LIVE", null, "ERROR", ex.getMessage());
            }
        }
        ExtractionResult demoResult = demo.extractJobSkills(jobText);
        record(userId, PURPOSE_EXTRACTION, "SIMULATED", null, "SUCCESS", null);
        return demoResult;
    }

    // --- project generation ---------------------------------------------------

    public GeneratedProject generateProject(String skillName, SkillCategory category, String level,
                                            String difficulty, String targetJob, int hoursPerWeek,
                                            String learningPreference, Long userId) {
        String prompt = "Skill: " + skillName + "\nCategory: " + category + "\nUser self-assessed level: "
                + nullToUnknown(level) + "\nTarget difficulty: " + difficulty
                + "\nTarget job: " + nullToUnknown(targetJob)
                + "\nWeekly hours available: " + hoursPerWeek
                + "\nLearning preference: " + nullToUnknown(learningPreference);

        if (resolveMode(userId, PURPOSE_PROJECT) == Mode.LIVE) {
            try {
                AiClient.AiCompletion completion = liveClient.complete(SYSTEM_PROJECT, prompt);
                GeneratedProject project = parseProject(completion.text(), skillName, category, difficulty);
                record(userId, PURPOSE_PROJECT, "LIVE", completion, "SUCCESS", null);
                return project;
            } catch (Exception ex) {
                log.warn("Live project generation failed, falling back to demo engine: {}", ex.getMessage());
                record(userId, PURPOSE_PROJECT, "LIVE", null, "ERROR", ex.getMessage());
            }
        }
        record(userId, PURPOSE_PROJECT, "SIMULATED", null, "SUCCESS", null);
        return demo.generateProject(skillName, category, difficulty, targetJob, hoursPerWeek);
    }

    // --- analysis summary ---------------------------------------------------

    public AnalysisText summarizeAnalysis(String jobTitle, int score, int demonstrated, int declared,
                                          int partial, int missing, List<String> topGaps, Long userId) {
        List<String> demoLines = demo.analysisSummary(score, demonstrated, declared, partial, missing);
        if (resolveMode(userId, PURPOSE_SUMMARY) == Mode.LIVE) {
            try {
                String prompt = "Job: " + jobTitle + "\nCompatibility score: " + score + "/100\n"
                        + "Demonstrated: " + demonstrated + ", declared: " + declared + ", partial: " + partial
                        + ", missing: " + missing + "\nTop gaps: " + String.join(", ", topGaps)
                        + "\nWrite a short encouraging paragraph (3 sentences max) explaining the score, "
                        + "then one line starting with 'Next step:' with the single highest-leverage action.";
                AiClient.AiCompletion completion = liveClient.complete(
                        "You are a supportive career coach. Be precise and honest; never promise a job.",
                        prompt);
                record(userId, PURPOSE_SUMMARY, "LIVE", completion, "SUCCESS", null);
                return new AnalysisText(String.join("\n", demoLines), completion.text().trim(), false);
            } catch (Exception ex) {
                log.warn("Live summary failed, falling back to demo text: {}", ex.getMessage());
                record(userId, PURPOSE_SUMMARY, "LIVE", null, "ERROR", ex.getMessage());
            }
        }
        record(userId, PURPOSE_SUMMARY, "SIMULATED", null, "SUCCESS", null);
        return new AnalysisText(String.join("\n", demoLines), String.join("\n", demoLines), true);
    }

    // --- mode + quotas -------------------------------------------------------

    private Mode resolveMode(Long userId, String purpose) {
        SkillMirrorProperties.Ai ai = properties.getAi();
        if (!"LIVE".equals(ai.effectiveMode()) || !liveClient.supportsLiveCalls()) {
            return Mode.DEMO;
        }
        if (userId != null) {
            int dailyLimit = settingsService.getInt(SettingsService.KEY_AI_DAILY_LIMIT, ai.getDailyUserLimit());
            if (dailyLimit > 0) {
                Instant dayStart = LocalDate.now(ZoneOffset.UTC).atStartOfDay().toInstant(ZoneOffset.UTC);
                long calls = usageRepository.countByUserIdAndCreatedAtAfter(userId, dayStart);
                if (calls >= dailyLimit) {
                    throw ApiException.tooManyRequests("AI_DAILY_QUOTA",
                            "Quota IA quotidien atteint (" + dailyLimit
                                    + " appels par jour). Réessayez demain.");
                }
            }
        }
        Instant monthStart = LocalDate.now(ZoneOffset.UTC).withDayOfMonth(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        long tokens = usageRepository.totalTokensSince(monthStart);
        long budget = settingsService.getLong(SettingsService.KEY_AI_MONTHLY_BUDGET, ai.getMonthlyTokenBudget());
        if (tokens >= budget) {
            log.warn("Monthly AI token budget exhausted ({} tokens); switching to simulated mode", tokens);
            return Mode.DEMO;
        }
        return Mode.LIVE;
    }

    private void record(Long userId, String purpose, String mode, AiClient.AiCompletion completion,
                        String status, String errorMessage) {
        try {
            AiUsageLog entry = new AiUsageLog();
            entry.setUserId(userId);
            entry.setPurpose(purpose);
            entry.setMode(mode);
            entry.setStatus(status);
            if (completion != null) {
                entry.setProvider("openai-compatible");
                entry.setModel(completion.model());
                entry.setPromptTokens(completion.promptTokens());
                entry.setCompletionTokens(completion.completionTokens());
                entry.setLatencyMs(completion.latencyMs());
            }
            if (errorMessage != null) {
                entry.setErrorMessage(errorMessage.length() > 450 ? errorMessage.substring(0, 450) : errorMessage);
            }
            usageRepository.save(entry);
        } catch (Exception ex) {
            log.warn("Unable to record AI usage: {}", ex.getMessage());
        }
    }

    // --- parsing -------------------------------------------------------------

    private ExtractionResult parseExtraction(String raw) throws Exception {
        JsonNode root = objectMapper.readTree(OpenAiCompatibleClient.extractJsonObject(raw));
        List<ExtractedSkill> technical = parseSkills(root.path("technical"), 30);
        List<ExtractedSkill> soft = parseSkills(root.path("softSkills"), 12);
        String experienceLevel = normalizeEnum(root.path("experienceLevel").asText(null),
                List.of("JUNIOR", "MIDDLE", "SENIOR"));
        String education = emptyToNull(root.path("educationRequirement").asText(null));
        return new ExtractionResult(technical, soft, experienceLevel, education, false);
    }

    private List<ExtractedSkill> parseSkills(JsonNode array, int limit) {
        List<ExtractedSkill> result = new ArrayList<>();
        if (!array.isArray()) return result;
        for (JsonNode node : array) {
            String name = node.path("name").asText("").trim();
            if (name.isEmpty() || result.size() >= limit) continue;
            String requirement = node.path("requirement").asText("REQUIRED").toUpperCase(Locale.ROOT);
            if (!requirement.equals("DESIRED")) requirement = "REQUIRED";
            int weight = node.path("weight").asInt(requirement.equals("REQUIRED") ? 3 : 1);
            weight = Math.max(1, Math.min(3, weight));
            String category = normalizeEnum(node.path("category").asText("OTHER"),
                    List.of("LANGUAGES", "WEB", "DATABASES", "DATA", "AI", "TOOLS", "SOFT_SKILLS", "OTHER"));
            String requiredLevel = normalizeEnum(node.path("requiredLevel").asText(null),
                    List.of("BEGINNER", "INTERMEDIATE", "ADVANCED", "EXPERT"));
            result.add(new ExtractedSkill(name.length() > 120 ? name.substring(0, 120) : name, category,
                    requirement, weight, requiredLevel));
        }
        return result;
    }

    private GeneratedProject parseProject(String raw, String skillName, SkillCategory category,
                                          String difficulty) throws Exception {
        JsonNode root = objectMapper.readTree(OpenAiCompatibleClient.extractJsonObject(raw));
        GeneratedProject fallback = demo.generateProject(skillName, category, difficulty, null, 8);
        return new GeneratedProject(
                emptyToNull(root.path("title").asText(null)) != null ? root.path("title").asText() : fallback.title(),
                emptyToNull(root.path("description").asText(null)) != null ? root.path("description").asText() : fallback.description(),
                normalizeEnum(root.path("difficulty").asText(null), List.of("BEGINNER", "INTERMEDIATE", "ADVANCED")) != null
                        ? root.path("difficulty").asText() : fallback.difficulty(),
                strings(root.path("objectives"), fallback.objectives()),
                strings(root.path("tools"), fallback.tools()),
                strings(root.path("steps"), fallback.steps()),
                strings(root.path("tasks"), fallback.tasks()),
                strings(root.path("evaluationCriteria"), fallback.evaluationCriteria()),
                emptyToNull(root.path("expectedResult").asText(null)) != null ? root.path("expectedResult").asText() : fallback.expectedResult(),
                emptyToNull(root.path("proofRequired").asText(null)) != null ? root.path("proofRequired").asText() : fallback.proofRequired(),
                false);
    }

    private List<String> strings(JsonNode array, List<String> fallback) {
        if (!array.isArray() || array.isEmpty()) return fallback;
        List<String> values = new ArrayList<>();
        for (JsonNode node : array) {
            String value = node.asText("").trim();
            if (!value.isEmpty()) values.add(value.length() > 500 ? value.substring(0, 500) : value);
        }
        return values.isEmpty() ? fallback : values;
    }

    private String normalizeEnum(String raw, List<String> allowed) {
        if (raw == null || raw.isBlank()) return null;
        String upper = raw.trim().toUpperCase(Locale.ROOT);
        return allowed.contains(upper) ? upper : null;
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String nullToUnknown(String value) {
        return value == null || value.isBlank() ? "unknown" : value;
    }

    private String truncate(String value, int max) {
        if (value == null) return "";
        return value.length() > max ? value.substring(0, max) : value;
    }
}
