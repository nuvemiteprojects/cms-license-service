package com.nuvemite.cms.licenses.messaging.events;

import java.util.UUID;

public record PaymentCompletedEvent(
        UUID eventId,
        String referenceType,
        UUID referenceId,
        UUID companyId,
        UUID invoiceId) {}
