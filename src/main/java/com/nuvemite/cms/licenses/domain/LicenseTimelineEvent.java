package com.nuvemite.cms.licenses.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "license_timeline_event")
public class LicenseTimelineEvent {

    @Id
    private UUID id;

    @Column(name = "application_id", nullable = false)
    private UUID applicationId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "actor_ref")
    private String actorRef;

    @Column
    private String notes;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected LicenseTimelineEvent() {}

    public static LicenseTimelineEvent create(UUID applicationId, String eventType, String actorRef, String notes) {
        LicenseTimelineEvent event = new LicenseTimelineEvent();
        event.id = UUID.randomUUID();
        event.applicationId = applicationId;
        event.eventType = eventType;
        event.actorRef = actorRef;
        event.notes = notes;
        event.occurredAt = Instant.now();
        return event;
    }

    public UUID getId() {
        return id;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getActorRef() {
        return actorRef;
    }

    public String getNotes() {
        return notes;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
