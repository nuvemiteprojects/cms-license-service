package com.nuvemite.cms.licenses.messaging.events;

import java.time.LocalDate;
import java.util.UUID;

public record LicenseGrantedEvent(
        UUID eventId,
        UUID licenseId,
        String licenseNumber,
        UUID companyId,
        UUID premiseId,
        String licenseType,
        UUID chemicalId,
        String chemicalName,
        LocalDate validFrom,
        LocalDate validUntil) {}
