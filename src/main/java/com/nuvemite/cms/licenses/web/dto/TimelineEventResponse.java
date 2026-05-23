package com.nuvemite.cms.licenses.web.dto;

import java.time.Instant;
import java.util.UUID;

public record TimelineEventResponse(
        UUID id, String eventType, String actorRef, String notes, Instant occurredAt) {}
