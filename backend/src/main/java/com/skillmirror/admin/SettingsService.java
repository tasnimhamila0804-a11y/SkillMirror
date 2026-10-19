package com.skillmirror.admin;

import com.skillmirror.common.ApiException;
import com.skillmirror.config.SkillMirrorProperties;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Runtime, admin-editable settings stored in app_settings. Values fall back to the
 * configuration properties when no override exists.
 */
@Service
public class SettingsService {

    public static final String KEY_AI_DAILY_LIMIT = "ai_daily_user_limit";
    public static final String KEY_AI_MONTHLY_BUDGET = "ai_monthly_token_budget";
    public static final String KEY_REGISTRATION_ENABLED = "registration_enabled";

    private final AppSettingRepository repository;
    private final SkillMirrorProperties properties;

    public SettingsService(AppSettingRepository repository, SkillMirrorProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    public record SettingDto(String key, String value, String description) {}

    public List<SettingDto> all() {
        Map<String, String> defaults = defaults();
        java.util.LinkedHashMap<String, SettingDto> merged = new java.util.LinkedHashMap<>();
        defaults.forEach((key, value) -> merged.put(key, new SettingDto(key, value, descriptionOf(key))));
        for (AppSetting setting : repository.findAll()) {
            merged.put(setting.getKey(),
                    new SettingDto(setting.getKey(), setting.getValue(), setting.getDescription()));
        }
        return List.copyOf(merged.values());
    }

    public String get(String key, String fallback) {
        return repository.findByKey(key).map(AppSetting::getValue).filter(v -> !v.isBlank()).orElse(fallback);
    }

    public int getInt(String key, int fallback) {
        String raw = get(key, null);
        if (raw == null) return fallback;
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    public long getLong(String key, long fallback) {
        String raw = get(key, null);
        if (raw == null) return fallback;
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    public boolean isRegistrationEnabled() {
        return Boolean.parseBoolean(get(KEY_REGISTRATION_ENABLED, "true"));
    }

    @Transactional
    public List<SettingDto> update(List<SettingDto> updates) {
        for (SettingDto update : updates) {
            if (!defaults().containsKey(update.key())) {
                throw ApiException.badRequest("UNKNOWN_SETTING", "Paramètre inconnu : " + update.key());
            }
            if (update.value() == null || update.value().length() > 500) {
                throw ApiException.badRequest("INVALID_SETTING_VALUE", "Valeur invalide pour " + update.key() + ".");
            }
            validate(update.key(), update.value());
            AppSetting setting = repository.findByKey(update.key()).orElseGet(() -> {
                AppSetting created = new AppSetting();
                created.setKey(update.key());
                created.setDescription(descriptionOf(update.key()));
                return created;
            });
            setting.setValue(update.value().trim());
            setting.touch();
            repository.save(setting);
        }
        return all();
    }

    private void validate(String key, String value) {
        try {
            switch (key) {
                case KEY_AI_DAILY_LIMIT -> {
                    int limit = Integer.parseInt(value);
                    if (limit < 0 || limit > 10_000) throw new NumberFormatException();
                }
                case KEY_AI_MONTHLY_BUDGET -> {
                    long budget = Long.parseLong(value);
                    if (budget < 0) throw new NumberFormatException();
                }
                case KEY_REGISTRATION_ENABLED -> {
                    if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
                        throw new NumberFormatException();
                    }
                }
                default -> { }
            }
        } catch (NumberFormatException ex) {
            throw ApiException.badRequest("INVALID_SETTING_VALUE",
                    "Valeur invalide pour " + key + " (attendu : "
                            + (key.equals(KEY_REGISTRATION_ENABLED) ? "true/false" : "nombre") + ").");
        }
    }

    private Map<String, String> defaults() {
        return Map.of(
                KEY_AI_DAILY_LIMIT, String.valueOf(properties.getAi().getDailyUserLimit()),
                KEY_AI_MONTHLY_BUDGET, String.valueOf(properties.getAi().getMonthlyTokenBudget()),
                KEY_REGISTRATION_ENABLED, "true");
    }

    private String descriptionOf(String key) {
        return switch (key) {
            case KEY_AI_DAILY_LIMIT -> "Nombre maximal d'appels IA par utilisateur et par jour.";
            case KEY_AI_MONTHLY_BUDGET -> "Budget mensuel de tokens IA (mode live) avant bascule en démo.";
            case KEY_REGISTRATION_ENABLED -> "Autoriser la création de comptes (true/false).";
            default -> "";
        };
    }
}
