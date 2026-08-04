package com.ssk.kiosk.flow;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ScreenFlowConfigurationRepository extends JpaRepository<ScreenFlowConfiguration,UUID>{Optional<ScreenFlowConfiguration> findFirstByStatusOrderByVersionNumberDesc(ScreenFlowConfiguration.Status status);}
