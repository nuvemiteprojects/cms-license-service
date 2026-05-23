package com.nuvemite.cms.licenses.messaging.events;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record LicenseInspectionProposedEvent(
        UUID eventId,
        UUID applicationId,
        String licenseNumber,
        UUID companyId,
        UUID premiseId,
        List<LocalDate> proposedDates) {}
