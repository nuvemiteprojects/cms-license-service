package com.nuvemite.cms.licenses.web;

import com.nuvemite.cms.licenses.domain.LicenseApplication;
import com.nuvemite.cms.licenses.domain.LicenseApplicationStatus;
import com.nuvemite.cms.licenses.security.LicenseAccessService;
import com.nuvemite.cms.licenses.security.SecurityUtils;
import com.nuvemite.cms.licenses.service.LicenseApplicationService;
import com.nuvemite.cms.licenses.web.dto.CompleteInspectionRequest;
import com.nuvemite.cms.licenses.web.dto.CreateLicenseRequest;
import com.nuvemite.cms.licenses.web.dto.LicenseApplicationResponse;
import com.nuvemite.cms.licenses.web.dto.ProposeInspectionRequest;
import com.nuvemite.cms.licenses.web.dto.RejectLicenseRequest;
import com.nuvemite.cms.licenses.web.dto.ScheduleInspectionRequest;
import com.nuvemite.cms.licenses.web.dto.SubmitLicenseRequest;
import com.nuvemite.cms.licenses.web.dto.TimelineEventResponse;
import com.nuvemite.cms.licenses.web.mapper.LicenseMapper;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/licenses")
public class LicenseController {

    private final LicenseApplicationService licenseService;
    private final LicenseAccessService access;
    private final LicenseMapper mapper;

    public LicenseController(
            LicenseApplicationService licenseService, LicenseAccessService access, LicenseMapper mapper) {
        this.licenseService = licenseService;
        this.access = access;
        this.mapper = mapper;
    }

    @GetMapping
    public Page<LicenseApplicationResponse> list(
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) UUID premiseId,
            @RequestParam(required = false) LicenseApplicationStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        var user = SecurityUtils.currentUser();
        UUID filterCompany = companyId;
        if (!user.isRegulator()) {
            filterCompany = companyId != null ? companyId : user.companyIds().stream().findFirst().orElse(null);
        }
        return licenseService.list(filterCompany, premiseId, status, pageable).map(mapper::toResponse);
    }

    @GetMapping("/{id}")
    public LicenseApplicationResponse get(@PathVariable UUID id) {
        LicenseApplication app = licenseService.get(id);
        access.requireReadAccess(app);
        return mapper.toResponse(app);
    }

    @GetMapping("/{id}/timeline")
    public List<TimelineEventResponse> timeline(@PathVariable UUID id) {
        LicenseApplication app = licenseService.get(id);
        access.requireReadAccess(app);
        return licenseService.timeline(id).stream().map(mapper::toTimelineResponse).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LicenseApplicationResponse create(@Valid @RequestBody CreateLicenseRequest request) {
        access.requireCompanyWrite(request.companyId(), request.premiseId());
        return mapper.toResponse(licenseService.createDraft(request, SecurityUtils.currentSubject()));
    }

    @PostMapping("/{id}/submit")
    public LicenseApplicationResponse submit(
            @PathVariable UUID id, @Valid @RequestBody SubmitLicenseRequest request) {
        LicenseApplication app = licenseService.get(id);
        access.requireCompanySubmit(app);
        return mapper.toResponse(licenseService.submit(id, request, SecurityUtils.currentSubject()));
    }

    @PostMapping("/{id}/propose-inspection")
    public LicenseApplicationResponse proposeInspection(
            @PathVariable UUID id, @Valid @RequestBody ProposeInspectionRequest request) {
        access.requireRegulator();
        return mapper.toResponse(
                licenseService.proposeInspection(id, request, SecurityUtils.currentSubject()));
    }

    @PostMapping("/{id}/schedule-inspection")
    public LicenseApplicationResponse scheduleInspection(
            @PathVariable UUID id, @Valid @RequestBody ScheduleInspectionRequest request) {
        access.requireRegulator();
        return mapper.toResponse(
                licenseService.scheduleInspection(id, request, SecurityUtils.currentSubject()));
    }

    @PostMapping("/{id}/complete-inspection")
    public LicenseApplicationResponse completeInspection(
            @PathVariable UUID id, @Valid @RequestBody CompleteInspectionRequest request) {
        access.requireRegulator();
        return mapper.toResponse(
                licenseService.completeInspection(id, request, SecurityUtils.currentSubject()));
    }

    @PostMapping("/{id}/approve")
    public LicenseApplicationResponse approve(@PathVariable UUID id) {
        access.requireRegulator();
        return mapper.toResponse(licenseService.approve(id, SecurityUtils.currentSubject()));
    }

    @PostMapping("/{id}/reject")
    public LicenseApplicationResponse reject(@PathVariable UUID id, @Valid @RequestBody RejectLicenseRequest request) {
        access.requireRegulator();
        return mapper.toResponse(licenseService.reject(id, request, SecurityUtils.currentSubject()));
    }
}
