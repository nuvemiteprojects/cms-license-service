package com.nuvemite.cms.licenses.web.mapper;

import com.nuvemite.cms.licenses.domain.LicenseApplication;
import com.nuvemite.cms.licenses.domain.LicenseTimelineEvent;
import com.nuvemite.cms.licenses.service.LicenseApplicationService;
import com.nuvemite.cms.licenses.web.dto.LicenseApplicationResponse;
import com.nuvemite.cms.licenses.web.dto.TimelineEventResponse;
import org.springframework.stereotype.Component;

@Component
public class LicenseMapper {

    public LicenseApplicationResponse toResponse(LicenseApplication app) {
        return new LicenseApplicationResponse(
                app.getId(),
                app.getLicenseNumber(),
                app.getCompanyId(),
                app.getPremiseId(),
                app.getPremiseName(),
                app.getLicenseType(),
                app.getChemicalId(),
                app.getChemicalName(),
                app.getStatus(),
                app.getApplicationTemplateId(),
                app.getInspectionTemplateId(),
                LicenseApplicationService.toDateList(app.getProposedInspectionDates()),
                app.getInspectionDate(),
                app.getInspectorName(),
                app.getInspectionNotes(),
                app.getInspectionOutcome(),
                app.getApplicationAnswers(),
                app.getChecklistAnswers(),
                app.getApprovedAt(),
                app.getExpiresAt(),
                app.getRejectedAt(),
                app.getRejectionReason(),
                app.getCreatedAt(),
                app.getUpdatedAt(),
                app.getVersion());
    }

    public TimelineEventResponse toTimelineResponse(LicenseTimelineEvent event) {
        return new TimelineEventResponse(
                event.getId(),
                event.getEventType(),
                event.getActorRef(),
                event.getNotes(),
                event.getOccurredAt());
    }
}
