package com.nuvemite.cms.licenses.messaging.events;

import java.time.LocalDate;
import java.util.UUID;

public record InspectionDateConfirmedEvent(
        UUID eventId,
        UUID inspectionRequestId,
        UUID licenseApplicationId,
        UUID companyId,
        UUID premiseId,
        LocalDate confirmedDate) {}
