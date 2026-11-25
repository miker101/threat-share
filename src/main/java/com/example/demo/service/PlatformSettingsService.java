
package com.example.demo.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.PlatformSetting;
import com.example.demo.repository.PlatformSettingRepository;

@Service
public class PlatformSettingsService {

    private final PlatformSettingRepository settingsRepository;

    public PlatformSettingsService(
            PlatformSettingRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }
    
    @Transactional(readOnly = true)
    public boolean getBooleanSetting(String settingKey) {

        PlatformSetting setting = settingsRepository
                .findBySettingKey(settingKey)
                .orElseThrow(() -> new IllegalStateException(
                        "Required platform setting is missing: " + settingKey
                ));

        if (!"BOOLEAN".equalsIgnoreCase(setting.getValueType())) {
            throw new IllegalStateException(
                    settingKey + " is not a BOOLEAN setting."
            );
        }

        return Boolean.parseBoolean(setting.getSettingValue());
    }
    @Transactional(readOnly = true)
    public int getIntegerSetting(String settingKey) {

        PlatformSetting setting = settingsRepository
                .findBySettingKey(settingKey)
                .orElseThrow(() -> new IllegalStateException(
                        "Required platform setting is missing: " + settingKey
                ));

        if (!"INTEGER".equalsIgnoreCase(setting.getValueType())) {
            throw new IllegalStateException(
                    settingKey + " is not an INTEGER setting."
            );
        }

        try {
            return Integer.parseInt(setting.getSettingValue());
        } catch (NumberFormatException ex) {
            throw new IllegalStateException(
                    "Invalid integer value for setting: " + settingKey,
                    ex
            );
        }
    }

    // Retrieve all platform settings
    @Transactional(readOnly = true)
    public List<PlatformSetting> getAllSettings() {
        return settingsRepository.findAll();
    }

    // Retrieve settings belonging to a category
    @Transactional(readOnly = true)
    public List<PlatformSetting> getSettingsByCategory(String category) {
        return settingsRepository.findByCategory(category.toUpperCase());
    }

    // Update multiple settings
    @Transactional
    public List<PlatformSetting> updateSettings(
            Map<String, String> updates,
            Long updatedBy) {

        if (updates == null || updates.isEmpty()) {
            throw new IllegalArgumentException(
                    "No settings were provided for updating.");
        }

        for (Map.Entry<String, String> entry : updates.entrySet()) {

            String key = entry.getKey();
            String value = entry.getValue();
         // Maintenance Mode must only be controlled through
         // the dedicated maintenance workflow.
            if ("maintenanceModeEnabled".equals(key)
                    || "maintenanceGracePeriodSeconds".equals(key)
                    || "maintenanceStartsAt".equals(key)) {

                throw new IllegalArgumentException(
                        "Maintenance settings must be managed through the maintenance controls."
                );
            }
            PlatformSetting setting = settingsRepository
                    .findBySettingKey(key)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Unknown platform setting: " + key));

            validateSettingValue(setting, value);

            setting.setSettingValue(value);
            setting.setUpdatedBy(updatedBy);

            settingsRepository.save(setting);
        }

        return settingsRepository.findAll();
    }

    // Validate values according to their declared types
    private void validateSettingValue(
            PlatformSetting setting,
            String value) {

        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    setting.getSettingKey() + " cannot be empty.");
        }

        String type = setting.getValueType();

        switch (type.toUpperCase()) {

            case "BOOLEAN":
                if (!value.equalsIgnoreCase("true")
                        && !value.equalsIgnoreCase("false")) {
                    throw new IllegalArgumentException(
                            setting.getSettingKey()
                            + " must be true or false.");
                }
                break;

            case "INTEGER":
                try {
                    int number = Integer.parseInt(value);

                    switch (setting.getSettingKey()) {

                        case "maximumLoginAttempts":
                        case "maximumSessions":
                            if (number < 1 || number > 20) {
                                throw new IllegalArgumentException(
                                        setting.getSettingKey()
                                        + " must be between 1 and 20.");
                            }
                            break;

                        case "sessionTimeoutMinutes":
                            if (number < 1 || number > 1440) {
                                throw new IllegalArgumentException(
                                        "Session timeout must be between "
                                        + "1 and 1440 minutes.");
                            }
                            break;
                    }

                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException(
                            setting.getSettingKey()
                            + " must contain a valid integer.");
                }
                break;

            case "STRING":
                if (value.length() > 2000) {
                    throw new IllegalArgumentException(
                            setting.getSettingKey()
                            + " is too long.");
                }
                break;

            default:
                throw new IllegalArgumentException(
                        "Unsupported setting type: " + type);
        }
    }
}
