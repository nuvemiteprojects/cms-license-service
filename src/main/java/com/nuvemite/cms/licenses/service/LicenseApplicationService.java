package com.nuvemite.cms.licenses.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuvemite.cms.licenses.config.LicensesProperties;
import com.nuvemite.cms.licenses.domain.LicenseApplication;
import com.nuvemite.cms.licenses.domain.LicenseApplicationStatus;
import com.nuvemite.cms.licenses.domain.LicenseGrant;
import com.nuvemite.cms.licenses.domain.LicenseTimelineEvent;
import com.nuvemite.cms.licenses.exception.ResourceNotFoundException;
import com.nuvemite.cms.licenses.messaging.EventTypes;
import com.nuvemite.cms.licenses.messaging.events.InspectionDateConfirmedEvent;
import com.nuvemite.cms.licenses.messaging.events.LicenseApplicationSubmittedEvent;
import com.nuvemite.cms.licenses.messaging.events.LicenseGrantedEvent;
import com.nuvemite.cms.licenses.messaging.events.LicenseInspectionProposedEvent;
import com.nuvemite.cms.licenses.messaging.events.LicenseInspectionScheduledEvent;
import com.nuvemite.cms.licenses.repository.LicenseApplicationRepository;
import com.nuvemite.cms.licenses.repository.LicenseGrantRepository;
import com.nuvemite.cms.licenses.repository.LicenseTimelineEventRepository;
import com.nuvemite.cms.licenses.web.dto.CompleteInspectionRequest;
import com.nuvemite.cms.licenses.web.dto.CreateLicenseRequest;
import com.nuvemite.cms.licenses.web.dto.ProposeInspectionRequest;
import com.nuvemite.cms.licenses.web.dto.RejectLicenseRequest;
import com.nuvemite.cms.licenses.web.dto.ScheduleInspectionRequest;
import com.nuvemite.cms.licenses.web.dto.SubmitLicenseRequest;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LicenseApplicationService {

    private final LicenseApplicationRepository applicationRepository;
    private final LicenseGrantRepository grantRepository;
    private final LicenseTimelineEventRepository timelineRepository;
    private final OutboxService outboxService;
    private final LicenseNumberGenerator licenseNumberGenerator;
    private final LicensesProperties properties;
    private final ObjectMapper objectMapper;

    public LicenseApplicationService(
            LicenseApplicationRepository applicationRepository,
            LicenseGrantRepository grantRepository,
            LicenseTimelineEventRepository timelineRepository,
            OutboxService outboxService,
            LicenseNumberGenerator licenseNumberGenerator,
            LicensesProperties properties,
            ObjectMapper objectMapper) {
        this.applicationRepository = applicationRepository;
        this.grantRepository = grantRepository;
        this.timelineRepository = timelineRepository;
        this.outboxService = outboxService;
        this.licenseNumberGenerator = licenseNumberGenerator;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public Page<LicenseApplication> list(UUID companyId, UUID premiseId, LicenseApplicationStatus status, Pageable pageable) {
        return applicationRepository.search(companyId, premiseId, status, pageable);
    }

    @Transactional(readOnly = true)
    public LicenseApplication get(UUID id) {
        return findApplication(id);
    }

    @Transactional
    public LicenseApplication createDraft(CreateLicenseRequest request, String actor) {
        String answers = serializeAnswers(request.applicationAnswers());
        LicenseApplication app = LicenseApplication.createDraft(
                licenseNumberGenerator.next(),
                request.companyId(),
                request.premiseId(),
                request.premiseName(),
                request.licenseType(),
                request.chemicalId(),
                request.chemicalName(),
                request.applicationTemplateId(),
                answers);
        applicationRepository.save(app);
        recordTimeline(app.getId(), "DRAFT_CREATED", actor, null);
        return app;
    }

    @Transactional
    public LicenseApplication submit(UUID id, SubmitLicenseRequest request, String actor) {
        LicenseApplication app = findApplication(id);
        LocalDate[] preferred = request.preferredInspectionDates().toArray(LocalDate[]::new);
        app.submit(preferred);
        applicationRepository.save(app);
        recordTimeline(app.getId(), "APPLICATION_SUBMITTED", actor, null);

        UUID eventId = UUID.randomUUID();
        outboxService.enqueue(
                "license_application",
                app.getId(),
                EventTypes.LICENSE_APPLICATION_SUBMITTED,
                new LicenseApplicationSubmittedEvent(
                        eventId,
                        app.getId(),
                        app.getCompanyId(),
                        app.getPremiseId(),
                        app.getLicenseType(),
                        request.preferredInspectionDates()));
        return app;
    }

    @Transactional
    public void agreeInspectionDateFromPlanner(UUID applicationId, LocalDate confirmedDate) {
        LicenseApplication app = findApplication(applicationId);
        if (app.getStatus() == LicenseApplicationStatus.INSPECTION_PROPOSED
                && confirmedDate.equals(app.getInspectionDate())) {
            return;
        }
        if (app.getStatus() != LicenseApplicationStatus.INSPECTION_DATE_PENDING) {
            throw new IllegalStateException("Cannot agree inspection date in status " + app.getStatus());
        }
        app.agreeInspectionDate(confirmedDate);
        applicationRepository.save(app);
        recordTimeline(app.getId(), "INSPECTION_DATE_AGREED", "resource-planner", null);
    }

    @Transactional
    public LicenseApplication proposeInspection(UUID id, ProposeInspectionRequest request, String actor) {
        LicenseApplication app = findApplication(id);
        LocalDate[] dates = request.proposedDates().toArray(LocalDate[]::new);
        app.proposeInspection(dates);
        applicationRepository.save(app);
        recordTimeline(app.getId(), "INSPECTION_PROPOSED", actor, null);

        UUID eventId = UUID.randomUUID();
        outboxService.enqueue(
                "license_application",
                app.getId(),
                EventTypes.LICENSE_INSPECTION_PROPOSED,
                new LicenseInspectionProposedEvent(
                        eventId,
                        app.getId(),
                        app.getLicenseNumber(),
                        app.getCompanyId(),
                        app.getPremiseId(),
                        request.proposedDates()));
        return app;
    }

    @Transactional
    public LicenseApplication scheduleInspection(UUID id, ScheduleInspectionRequest request, String actor) {
        LicenseApplication app = findApplication(id);
        app.scheduleInspection(request.inspectionDate(), request.inspectorName());
        applicationRepository.save(app);
        recordTimeline(app.getId(), "INSPECTION_SCHEDULED", actor, null);
        publishInspectionScheduled(app);
        return app;
    }

    @Transactional
    public LicenseApplication scheduleInspectionFromVisit(UUID applicationId, LocalDate date, String inspectorName) {
        LicenseApplication app = findApplication(applicationId);
        if (app.getStatus() == LicenseApplicationStatus.INSPECTION_SCHEDULED) {
            return app;
        }
        if (app.getStatus() != LicenseApplicationStatus.INSPECTION_PROPOSED) {
            throw new IllegalStateException("Cannot schedule visit for application in status " + app.getStatus());
        }
        app.scheduleInspection(date, inspectorName);
        applicationRepository.save(app);
        recordTimeline(app.getId(), "INSPECTION_SCHEDULED", "resource-planner", "Visit scheduled via Kafka");
        publishInspectionScheduled(app);
        return app;
    }

    @Transactional
    public LicenseApplication completeInspection(UUID id, CompleteInspectionRequest request, String actor) {
        LicenseApplication app = findApplication(id);
        String checklist = serializeAnswers(request.checklistAnswers());
        app.completeInspection(request.outcome(), checklist, request.notes());
        applicationRepository.save(app);
        recordTimeline(app.getId(), "INSPECTION_COMPLETE", actor, request.outcome());
        return app;
    }

    @Transactional
    public LicenseApplication approve(UUID id, String actor) {
        LicenseApplication app = findApplication(id);
        LocalDate validFrom = LocalDate.now();
        LocalDate validUntil = validFrom.plusYears(properties.grantValidityYears());
        app.approve(validFrom, validUntil);
        applicationRepository.save(app);

        LicenseGrant grant = LicenseGrant.fromApplication(app, validFrom, validUntil, actor);
        grantRepository.save(grant);
        recordTimeline(app.getId(), "LICENSE_GRANTED", actor, null);

        UUID eventId = UUID.randomUUID();
        outboxService.enqueue(
                "license_application",
                app.getId(),
                EventTypes.LICENSE_GRANTED,
                new LicenseGrantedEvent(
                        eventId,
                        app.getId(),
                        app.getLicenseNumber(),
                        app.getCompanyId(),
                        app.getPremiseId(),
                        app.getLicenseType(),
                        app.getChemicalId(),
                        app.getChemicalName(),
                        validFrom,
                        validUntil));
        return app;
    }

    @Transactional
    public LicenseApplication reject(UUID id, RejectLicenseRequest request, String actor) {
        LicenseApplication app = findApplication(id);
        app.reject(request.reason());
        applicationRepository.save(app);
        recordTimeline(app.getId(), "APPLICATION_REJECTED", actor, request.reason());
        return app;
    }

    @Transactional
    public void recordPaymentCompleted(UUID applicationId, String notes) {
        findApplication(applicationId);
        recordTimeline(applicationId, "PAYMENT_COMPLETED", "payments", notes);
    }

    private void publishInspectionScheduled(LicenseApplication app) {
        UUID eventId = UUID.randomUUID();
        outboxService.enqueue(
                "license_application",
                app.getId(),
                EventTypes.LICENSE_INSPECTION_SCHEDULED,
                new LicenseInspectionScheduledEvent(
                        eventId,
                        app.getId(),
                        app.getLicenseNumber(),
                        app.getCompanyId(),
                        app.getPremiseId(),
                        app.getInspectionDate(),
                        app.getInspectorName()));
    }

    private void recordTimeline(UUID applicationId, String eventType, String actor, String notes) {
        timelineRepository.save(LicenseTimelineEvent.create(applicationId, eventType, actor, notes));
    }

    private LicenseApplication findApplication(UUID id) {
        return applicationRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("License application not found: " + id));
    }

    private String serializeAnswers(Object answers) {
        if (answers == null) {
            return "{}";
        }
        if (answers instanceof String s) {
            return s.isBlank() ? "{}" : s;
        }
        try {
            return objectMapper.writeValueAsString(answers);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid answers payload", e);
        }
    }

    public List<LicenseTimelineEvent> timeline(UUID applicationId) {
        findApplication(applicationId);
        return timelineRepository.findByApplicationIdOrderByOccurredAtAsc(applicationId);
    }

    public static List<LocalDate> toDateList(LocalDate[] dates) {
        if (dates == null) {
            return List.of();
        }
        return Arrays.asList(dates);
    }
}
