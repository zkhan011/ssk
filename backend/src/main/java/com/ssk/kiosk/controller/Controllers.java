package com.ssk.kiosk.controller;

import com.ssk.kiosk.dto.ApplicationRequest;
import com.ssk.kiosk.dto.ApplicationResponse;
import com.ssk.kiosk.dto.CategoryDto;
import com.ssk.kiosk.dto.CheckRequest;
import com.ssk.kiosk.dto.HostDto;
import com.ssk.kiosk.dto.ValidateRequest;
import com.ssk.kiosk.dto.ValidationResponse;
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
import org.springframework.data.domain.PageRequest;
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
        "authorised", gatePassId.matches("\\d{8}"),
        "reference", gatePassId,
        "provider", "TASREEH",
        "checkedAt", Instant.now().toString());
  }

  @GetMapping("/admin/dashboard")
  Object dashboard() {
    return Map.of(
        "expectedToday", apps.count(),
        "pendingApprovals", 0,
        "visitorsInside", 0,
        "recentActivities", audits.findAll(PageRequest.of(0, 10)).getContent());
  }

  @GetMapping("/admin/applications")
  Object list() {
    return apps.findAll().map(GatePassService::map);
  }

  @GetMapping("/applications/{id}")
  ApplicationResponse get(@PathVariable UUID id) {
    return apps.findById(id).map(GatePassService::map).orElseThrow();
  }
}
