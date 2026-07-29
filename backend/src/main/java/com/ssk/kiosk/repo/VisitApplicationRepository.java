package com.ssk.kiosk.repo;
import com.ssk.kiosk.model.ApplicationStatus;
import com.ssk.kiosk.model.VisitApplication;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
public interface VisitApplicationRepository extends JpaRepository<VisitApplication,UUID>{
 Optional<VisitApplication> findByApplicationNumber(String number);
 Optional<VisitApplication> findByQrToken(String token);
 Page<VisitApplication> findByFullNameContainingIgnoreCase(String name, Pageable pageable);
 Page<VisitApplication> findByStatus(ApplicationStatus status, Pageable pageable);
 long countByStatus(ApplicationStatus status);
}
