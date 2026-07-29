package com.ssk.kiosk.controller;

import com.ssk.kiosk.appearance.AppearanceService;
import com.ssk.kiosk.dto.ApplicationRequest;
import com.ssk.kiosk.dto.ApplicationResponse;
import com.ssk.kiosk.dto.CategoryDto;
import com.ssk.kiosk.dto.CheckRequest;
import com.ssk.kiosk.dto.HostDto;
import com.ssk.kiosk.dto.ValidateRequest;
import com.ssk.kiosk.dto.ValidationResponse;
import com.ssk.kiosk.integration.IntegrationConfigurationService;
import com.ssk.kiosk.model.ApplicationStatus;
import com.ssk.kiosk.model.VisitApplication;
import com.ssk.kiosk.repo.AuditLogRepository;
import com.ssk.kiosk.repo.HostRepository;
import com.ssk.kiosk.repo.VisitApplicationRepository;
import com.ssk.kiosk.repo.VisitorCategoryRepository;
import com.ssk.kiosk.service.GatePassService;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
class ApiController {
  private final VisitorCategoryRepository categories;
  private final HostRepository hosts;
  private final VisitApplicationRepository apps;
  private final AuditLogRepository audits;
  private final GatePassService service;
  private final AppearanceService appearanceService;
  private final IntegrationConfigurationService integrationConfigurationService;

  @GetMapping("/admin/integrations")
  Object integrations() { return integrationConfigurationService.all(); }

  @PostMapping("/admin/integrations/{key}")
  Object updateIntegration(@PathVariable String key, @RequestBody Map<String, Object> values) {
    return integrationConfigurationService.update(key, values, "admin");
  }

  @GetMapping("/appearance/published")
  Map<String, Object> publishedAppearance() { return appearanceService.published(); }

  @GetMapping("/appearance/defaults")
  Map<String, Object> defaultAppearance() { return appearanceService.defaults(); }

  @GetMapping("/admin/appearance/draft")
  Map<String, Object> appearanceDraft() { return appearanceService.draft(); }

  @PostMapping("/admin/appearance/draft")
  Map<String, Object> saveAppearanceDraft(@RequestBody Map<String, Object> snapshot) { return appearanceService.saveDraft(snapshot, "admin"); }

  @PostMapping("/admin/appearance/publish")
  Map<String, Object> publishAppearance() { return appearanceService.publish("admin"); }

  @GetMapping("/admin/appearance/history")
  Object appearanceHistory() { return appearanceService.history(); }

  @PostMapping("/kiosk/sessions")
  Map<String, Object> session() {
    return Map.of("sessionId", UUID.randomUUID(), "timeoutSeconds", 180);
  }

  @GetMapping("/kiosk/categories")
  Object categories() {
    return categories.findAll().stream()
        .map(c -> new CategoryDto(c.getId(), c.getCode(), c.getNameEn(), c.getNameAr(), c.isApprovalRequired()))
        .toList();
  }

  @PostMapping("/kiosk/sessions/{sessionId}/application")
  ApplicationResponse app(@PathVariable UUID sessionId, @Valid @RequestBody ApplicationRequest request) {
    return service.create(request);
  }

  @GetMapping("/hosts/search")
  Object hosts(@RequestParam(defaultValue = "") String q) {
    return hosts
        .findTop20ByFullNameContainingIgnoreCaseOrEmployeeIdContainingIgnoreCaseOrDepartmentContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneContainingIgnoreCase(
            q, q, q, q, q)
        .stream()
        .map(h -> new HostDto(h.getId(), h.getEmployeeId(), h.getFullName(), h.getDepartment(), h.getEmail(), h.getPhone()))
        .toList();
  }

  @PostMapping("/passes/validate")
  ValidationResponse validate(@Valid @RequestBody ValidateRequest request) {
    return service.validate(request);
  }

  @PostMapping("/passes/{id}/check-in")
  ApplicationResponse checkIn(@PathVariable UUID id, @RequestBody CheckRequest request) {
    return service.checkIn(id, request);
  }

  @PostMapping("/passes/{id}/check-out")
  ApplicationResponse checkOut(@PathVariable UUID id, @RequestBody CheckRequest request) {
    return service.checkOut(id, request);
  }

  @PostMapping("/tasreeh/verify")
  Object tasreeh(@RequestBody Map<String, String> request) {
    String gatePassId = request.getOrDefault("gatePassId", "");
    return Map.of(
        "authorised", !gatePassId.isBlank() && gatePassId.length() <= 64,
        "reference", gatePassId,
        "provider", "TASREEH",
        "checkedAt", Instant.now().toString());
  }

  @PostMapping("/pangu/verify")
  Object panguVerify(@RequestBody Map<String, String> request) {
    String gatePassId = request.getOrDefault("gatePassId", "");
    boolean registered = gatePassId.endsWith("7");
    return Map.of(
        "registered", registered,
        "reference", gatePassId,
        "provider", "PANGU",
        "checkedAt", Instant.now().toString());
  }

  @PostMapping("/pangu/register")
  Object panguRegister(@RequestBody Map<String, Object> request) {
    Object gatePassId = request.getOrDefault("gatePassId", "");
    return Map.of(
        "registered", true,
        "reference", String.valueOf(gatePassId),
        "provider", "PANGU",
        "registeredAt", Instant.now().toString());
  }


  @GetMapping("/admin/dashboard")
  Object dashboard() {
    return Map.of(
        "expectedToday", apps.count(),
        "pendingApprovals", 0,
        "visitorsInside", 0,
        "recentActivities", audits.findAll(PageRequest.of(0, 10)).getContent());
  }

  @GetMapping("/admin/registrations")
  Object registrations(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "createdAt") String sort,
      @RequestParam(defaultValue = "DESC") Sort.Direction direction,
      @RequestParam(defaultValue = "") String search,
      @RequestParam(required = false) ApplicationStatus status) {
    int safeSize = Math.min(Math.max(size, 1), 100);
    String safeSort = switch (sort) { case "applicationNumber", "fullName", "status", "validFrom", "validUntil" -> sort; default -> "createdAt"; };
    PageRequest request = PageRequest.of(Math.max(page, 0), safeSize, Sort.by(direction, safeSort));
    Page<VisitApplication> result = status != null ? apps.findByStatus(status, request)
        : !search.isBlank() ? apps.findByFullNameContainingIgnoreCase(search.trim(), request)
        : apps.findAll(request);
    return result.map(GatePassService::map);
  }

  @GetMapping("/admin/reports/summary")
  Object reportSummary() {
    return Map.of(
        "total", apps.count(),
        "approved", apps.countByStatus(ApplicationStatus.APPROVED),
        "pending", apps.countByStatus(ApplicationStatus.PENDING_HOST_APPROVAL) + apps.countByStatus(ApplicationStatus.PENDING_SECURITY_APPROVAL),
        "checkedIn", apps.countByStatus(ApplicationStatus.CHECKED_IN),
        "rejected", apps.countByStatus(ApplicationStatus.REJECTED));
  }

  @GetMapping(value = "/admin/registrations/export", produces = "text/csv")
  ResponseEntity<String> exportRegistrations() {
    StringBuilder csv = new StringBuilder("applicationNumber,visitorName,status,maskedDocumentNumber,validFrom,validUntil\n");
    apps.findAll(PageRequest.of(0, 1000, Sort.by(Sort.Direction.DESC, "createdAt"))).forEach(a -> csv
        .append(a.getApplicationNumber()).append(',').append(csv(a.getFullName())).append(',')
        .append(a.getStatus()).append(',').append(a.getMaskedDocumentNumber()).append(',')
        .append(a.getValidFrom()).append(',').append(a.getValidUntil()).append('\n'));
    return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=registrations.csv")
        .contentType(new MediaType("text", "csv")).body(csv.toString());
  }

  private String csv(String value) { return "\"" + (value == null ? "" : value.replace("\"", "\"\"")) + "\""; }

  @GetMapping("/admin/applications")
  Object list() {
    return apps.findAll()
        .stream()
        .map(GatePassService::map)
        .toList();
  }

  @GetMapping("/applications/{id}")
  ApplicationResponse get(@PathVariable UUID id) {
    return apps.findById(id)
        .map(GatePassService::map)
        .orElseThrow();
  }
}
