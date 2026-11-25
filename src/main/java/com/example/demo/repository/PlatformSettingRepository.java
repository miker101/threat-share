
package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.PlatformSetting;

@Repository
public interface PlatformSettingRepository
        extends JpaRepository<PlatformSetting, Long> {

    Optional<PlatformSetting> findBySettingKey(String settingKey);

    List<PlatformSetting> findByCategory(String category);

    boolean existsBySettingKey(String settingKey);
}
