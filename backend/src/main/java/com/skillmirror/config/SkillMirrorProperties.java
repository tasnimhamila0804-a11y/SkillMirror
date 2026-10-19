package com.skillmirror.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** All configurable settings of the application, bound from the `skillmirror.*` properties. */
@ConfigurationProperties(prefix = "skillmirror")
public class SkillMirrorProperties {

    private Security security = new Security();
    private Cors cors = new Cors();
    private Ai ai = new Ai();
    private Upload upload = new Upload();

    public Security getSecurity() { return security; }
    public void setSecurity(Security security) { this.security = security; }
    public Cors getCors() { return cors; }
    public void setCors(Cors cors) { this.cors = cors; }
    public Ai getAi() { return ai; }
    public void setAi(Ai ai) { this.ai = ai; }
    public Upload getUpload() { return upload; }
    public void setUpload(Upload upload) { this.upload = upload; }

    public static class Security {
        private Jwt jwt = new Jwt();
        public Jwt getJwt() { return jwt; }
        public void setJwt(Jwt jwt) { this.jwt = jwt; }
    }

    public static class Jwt {
        private String secret;
        private long expirationSeconds = 86400;
        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }
        public long getExpirationSeconds() { return expirationSeconds; }
        public void setExpirationSeconds(long expirationSeconds) { this.expirationSeconds = expirationSeconds; }
    }

    public static class Cors {
        private String allowedOrigins = "http://localhost:4200";
        public String getAllowedOrigins() { return allowedOrigins; }
        public void setAllowedOrigins(String allowedOrigins) { this.allowedOrigins = allowedOrigins; }
    }

    public static class Ai {
        /** auto | live | demo */
        private String mode = "auto";
        private String baseUrl = "https://api.openai.com/v1";
        private String apiKey = "";
        private String model = "gpt-4o-mini";
        private int timeoutSeconds = 45;
        private int maxTokens = 2500;
        private int dailyUserLimit = 40;
        private long monthlyTokenBudget = 2_000_000;

        public String getMode() { return mode; }
        public void setMode(String mode) { this.mode = mode; }
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public int getTimeoutSeconds() { return timeoutSeconds; }
        public void setTimeoutSeconds(int timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
        public int getMaxTokens() { return maxTokens; }
        public void setMaxTokens(int maxTokens) { this.maxTokens = maxTokens; }
        public int getDailyUserLimit() { return dailyUserLimit; }
        public void setDailyUserLimit(int dailyUserLimit) { this.dailyUserLimit = dailyUserLimit; }
        public long getMonthlyTokenBudget() { return monthlyTokenBudget; }
        public void setMonthlyTokenBudget(long monthlyTokenBudget) { this.monthlyTokenBudget = monthlyTokenBudget; }

        public boolean hasKey() {
            return apiKey != null && !apiKey.isBlank();
        }

        /** Effective mode: `live` only when a key is configured and mode allows it. */
        public String effectiveMode() {
            if ("demo".equalsIgnoreCase(mode)) return "DEMO";
            if ("live".equalsIgnoreCase(mode)) return hasKey() ? "LIVE" : "DEMO";
            return hasKey() ? "LIVE" : "DEMO";
        }
    }

    public static class Upload {
        private String directory = "./uploads";
        private long maxBytes = 5_242_880L;
        private String allowedExtensions = "pdf,txt,md,png,jpg,jpeg,zip,json,csv,docx";

        public String getDirectory() { return directory; }
        public void setDirectory(String directory) { this.directory = directory; }
        public long getMaxBytes() { return maxBytes; }
        public void setMaxBytes(long maxBytes) { this.maxBytes = maxBytes; }
        public String getAllowedExtensions() { return allowedExtensions; }
        public void setAllowedExtensions(String allowedExtensions) { this.allowedExtensions = allowedExtensions; }
    }
}
