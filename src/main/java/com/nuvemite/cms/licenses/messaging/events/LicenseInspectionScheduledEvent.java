package com.nuvemite.cms.licenses.messaging.events;

import java.time.LocalDate;
import java.util.UUID;

public record LicenseInspectionScheduledEvent(
        UUID eventId,
        UUID applicationId,
        String licenseNumber,
        UUID companyId,
        UUID premiseId,
        LocalDate inspectionDate,
        String inspectorName) {}
