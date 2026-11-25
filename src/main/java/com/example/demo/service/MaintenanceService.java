package com.example.demo.service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.PlatformSetting;
import com.example.demo.repository.PlatformSettingRepository;

import org.springframework.scheduling.annotation.Scheduled;

@Service
public class MaintenanceService {
	
	private final PlatformSettingRepository settingsRepository;

	private final AdministratorSessionService sessionService;

	public MaintenanceService(
	        PlatformSettingRepository settingsRepository,
	        AdministratorSessionService sessionService) {

	    this.settingsRepository = settingsRepository;
	    this.sessionService = sessionService;
	}
	
	@Transactional(readOnly = true)
	public Map<String, Object> getMaintenanceStatus() {

	    Map<String, Object> status = new HashMap<>();

	    String enabled = settingsRepository
	            .findBySettingKey("maintenanceModeEnabled")
	            .orElseThrow(() -> new IllegalStateException(
	                    "Maintenance setting is missing."
	            ))
	            .getSettingValue();

	    String startsAt = settingsRepository
	            .findBySettingKey("maintenanceStartsAt")
	            .orElseThrow(() -> new IllegalStateException(
	                    "Maintenance start time setting is missing."
	            ))
	            .getSettingValue();

	    String gracePeriod = settingsRepository
	            .findBySettingKey("maintenanceGracePeriodSeconds")
	            .orElseThrow(() -> new IllegalStateException(
	                    "Maintenance grace period setting is missing."
	            ))
	            .getSettingValue();

	    status.put("maintenanceEnabled", Boolean.parseBoolean(enabled));
	    status.put("maintenanceStartsAt",
	            startsAt == null || startsAt.isBlank() ? null : startsAt);
	    status.put("gracePeriodSeconds", Integer.parseInt(gracePeriod));

	    return status;
	}
	
	@Transactional
	public Map<String, Object> scheduleMaintenance(Long adminId) {

	    PlatformSetting enabledSetting = settingsRepository
	            .findBySettingKey("maintenanceModeEnabled")
	            .orElseThrow(() -> new IllegalStateException(
	                    "Maintenance setting is missing."
	            ));

	    if (Boolean.parseBoolean(enabledSetting.getSettingValue())) {
	        throw new IllegalStateException(
	                "Maintenance Mode is already active."
	        );
	    }

	    PlatformSetting startsAtSetting = settingsRepository
	            .findBySettingKey("maintenanceStartsAt")
	            .orElseThrow(() -> new IllegalStateException(
	                    "Maintenance start time setting is missing."
	            ));

	    if (startsAtSetting.getSettingValue() != null
	            && !startsAtSetting.getSettingValue().isBlank()) {
	        throw new IllegalStateException(
	                "A maintenance countdown is already scheduled."
	        );
	    }

	    PlatformSetting graceSetting = settingsRepository
	            .findBySettingKey("maintenanceGracePeriodSeconds")
	            .orElseThrow(() -> new IllegalStateException(
	                    "Maintenance grace period setting is missing."
	            ));

	    int gracePeriodSeconds = Integer.parseInt(
	            graceSetting.getSettingValue()
	    );

	    if (gracePeriodSeconds < 1 || gracePeriodSeconds > 86400) {
	        throw new IllegalStateException(
	                "Grace period must be between 1 and 86400 seconds."
	        );
	    }

	    LocalDateTime activationTime = LocalDateTime.now()
	            .plusSeconds(gracePeriodSeconds);

	    startsAtSetting.setSettingValue(activationTime.toString());
	    startsAtSetting.setUpdatedBy(adminId);

	    settingsRepository.save(startsAtSetting);

	    return getMaintenanceStatus();
	}
	
	@Transactional
	public void cancelScheduledMaintenance(Long adminId) {

	    PlatformSetting startsAtSetting = settingsRepository
	            .findBySettingKey("maintenanceStartsAt")
	            .orElseThrow(() -> new IllegalStateException(
	                    "Maintenance start time setting is missing."
	            ));

	    PlatformSetting enabledSetting = settingsRepository
	            .findBySettingKey("maintenanceModeEnabled")
	            .orElseThrow(() -> new IllegalStateException(
	                    "Maintenance setting is missing."
	            ));

	    if (Boolean.parseBoolean(enabledSetting.getSettingValue())) {
	        throw new IllegalStateException(
	                "Maintenance is already active. Disable it instead."
	        );
	    }

	    if (startsAtSetting.getSettingValue() == null
	            || startsAtSetting.getSettingValue().isBlank()) {
	        throw new IllegalStateException(
	                "There is no scheduled maintenance to cancel."
	        );
	    }

	    startsAtSetting.setSettingValue("");
	    startsAtSetting.setUpdatedBy(adminId);

	    settingsRepository.save(startsAtSetting);
	}
	@Transactional
	public void disableMaintenance(Long adminId) {

	    PlatformSetting enabledSetting = settingsRepository
	            .findBySettingKey("maintenanceModeEnabled")
	            .orElseThrow(() -> new IllegalStateException(
	                    "Maintenance setting is missing."
	            ));

	    PlatformSetting startsAtSetting = settingsRepository
	            .findBySettingKey("maintenanceStartsAt")
	            .orElseThrow(() -> new IllegalStateException(
	                    "Maintenance start time setting is missing."
	            ));

	    if (!Boolean.parseBoolean(enabledSetting.getSettingValue())) {
	        throw new IllegalStateException(
	                "Maintenance Mode is not currently active."
	        );
	    }

	    enabledSetting.setSettingValue("false");
	    enabledSetting.setUpdatedBy(adminId);

	    startsAtSetting.setSettingValue("");
	    startsAtSetting.setUpdatedBy(adminId);

	    settingsRepository.save(enabledSetting);
	    settingsRepository.save(startsAtSetting);
	}
	@Scheduled(fixedDelay = 3000)
	@Transactional
	public void activateMaintenanceIfDue() {

	    PlatformSetting enabledSetting = settingsRepository
	            .findBySettingKey("maintenanceModeEnabled")
	            .orElseThrow(() -> new IllegalStateException(
	                    "Maintenance setting is missing."
	            ));

	    PlatformSetting startsAtSetting = settingsRepository
	            .findBySettingKey("maintenanceStartsAt")
	            .orElseThrow(() -> new IllegalStateException(
	                    "Maintenance start time setting is missing."
	            ));

	    if (Boolean.parseBoolean(enabledSetting.getSettingValue())) {
	        return;
	    }

	    String startsAt = startsAtSetting.getSettingValue();

	    if (startsAt == null || startsAt.isBlank()) {
	        return;
	    }

	    LocalDateTime activationTime = LocalDateTime.parse(startsAt);

	    if (LocalDateTime.now().isBefore(activationTime)) {
	        return;
	    }

	    enabledSetting.setSettingValue("true");
	    startsAtSetting.setSettingValue("");

	    settingsRepository.save(enabledSetting);
	    settingsRepository.save(startsAtSetting);

	    sessionService.revokeAllOrganizationAdminSessions(
	            "ThreatShare maintenance mode activated."
	    );
	}

}
