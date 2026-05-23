package com.nuvemite.cms.licenses.web.dto;

import com.nuvemite.cms.licenses.domain.LicenseApplicationStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record LicenseApplicationResponse(
        UUID id,
        String licenseNumber,
        UUID companyId,
        UUID premiseId,
        String premiseName,
        String licenseType,
        UUID chemicalId,
        String chemicalName,
        LicenseApplicationStatus status,
        UUID applicationTemplateId,
        UUID inspectionTemplateId,
        List<LocalDate> proposedInspectionDates,
        LocalDate inspectionDate,
        String inspectorName,
        String inspectionNotes,
        String inspectionOutcome,
        String applicationAnswers,
        String checklistAnswers,
        Instant approvedAt,
        LocalDate expiresAt,
        Instant rejectedAt,
        String rejectionReason,
        Instant createdAt,
        Instant updatedAt,
        long version) {}
