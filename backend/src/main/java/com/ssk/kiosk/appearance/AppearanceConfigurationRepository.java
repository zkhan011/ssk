package com.ssk.kiosk.appearance;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AppearanceConfigurationRepository extends JpaRepository<AppearanceConfiguration,UUID>{
 Optional<AppearanceConfiguration> findFirstByStatusOrderByVersionNumberDesc(AppearanceConfiguration.Status status);
 List<AppearanceConfiguration> findAllByOrderByVersionNumberDesc();
}
