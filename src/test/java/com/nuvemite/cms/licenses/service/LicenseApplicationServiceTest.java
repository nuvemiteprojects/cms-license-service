package com.nuvemite.cms.licenses.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuvemite.cms.licenses.config.LicensesProperties;
import com.nuvemite.cms.licenses.domain.LicenseApplication;
import com.nuvemite.cms.licenses.domain.LicenseApplicationStatus;
import com.nuvemite.cms.licenses.repository.LicenseApplicationRepository;
import com.nuvemite.cms.licenses.repository.LicenseGrantRepository;
import com.nuvemite.cms.licenses.repository.LicenseTimelineEventRepository;
import com.nuvemite.cms.licenses.web.dto.CompleteInspectionRequest;
import com.nuvemite.cms.licenses.web.dto.CreateLicenseRequest;
import com.nuvemite.cms.licenses.web.dto.ProposeInspectionRequest;
import com.nuvemite.cms.licenses.web.dto.ScheduleInspectionRequest;
import com.nuvemite.cms.licenses.web.dto.SubmitLicenseRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LicenseApplicationServiceTest {

    @Mock
    private LicenseApplicationRepository applicationRepository;

    @Mock
    private LicenseGrantRepository grantRepository;

    @Mock
    private LicenseTimelineEventRepository timelineRepository;

    @Mock
    private OutboxService outboxService;

    private LicenseApplicationService service;

    @BeforeEach
    void setUp() {
        service = new LicenseApplicationService(
                applicationRepository,
                grantRepository,
                timelineRepository,
                outboxService,
                new LicenseNumberGenerator(),
                new LicensesProperties(new LicensesProperties.Outbox(5000, 50), 1),
                new ObjectMapper());
    }

    @Test
    void workflowAdvancesThroughStates() {
        LicenseApplication app = LicenseApplication.createDraft(
                "LIC-2026-000001",
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Plant A",
                "Importer",
                UUID.randomUUID(),
                "Acetone",
                null,
                "{}");
        UUID id = app.getId();
        when(applicationRepository.findById(id)).thenReturn(Optional.of(app));
        when(applicationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.submit(id, new SubmitLicenseRequest(List.of(LocalDate.now().plusDays(14))), "user-1");
        assertThat(app.getStatus()).isEqualTo(LicenseApplicationStatus.INSPECTION_DATE_PENDING);

        service.proposeInspection(id, new ProposeInspectionRequest(List.of(LocalDate.now().plusDays(7))), "reg-1");
        assertThat(app.getStatus()).isEqualTo(LicenseApplicationStatus.INSPECTION_PROPOSED);
        verify(outboxService, org.mockito.Mockito.atLeastOnce()).enqueue(any(), any(), any(), any());

        service.scheduleInspection(id, new ScheduleInspectionRequest(LocalDate.now().plusDays(10), "Inspector Lee"), "reg-1");
        assertThat(app.getStatus()).isEqualTo(LicenseApplicationStatus.INSPECTION_SCHEDULED);

        service.completeInspection(
                id, new CompleteInspectionRequest("PASS", "ok", java.util.Map.of("item1", true)), "reg-1");
        assertThat(app.getStatus()).isEqualTo(LicenseApplicationStatus.INSPECTION_COMPLETE);

        service.approve(id, "reg-1");
        assertThat(app.getStatus()).isEqualTo(LicenseApplicationStatus.ACTIVE);
        verify(grantRepository).save(any());
    }

    @Test
    void rejectFromSubmitted() {
        LicenseApplication app = LicenseApplication.createDraft(
                "LIC-2026-000002",
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "Importer",
                UUID.randomUUID(),
                "Acetone",
                null,
                "{}");
        app.submit(new LocalDate[] {LocalDate.now().plusDays(7)});
        UUID id = app.getId();
        when(applicationRepository.findById(id)).thenReturn(Optional.of(app));
        when(applicationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.reject(id, new com.nuvemite.cms.licenses.web.dto.RejectLicenseRequest("Incomplete docs"), "reg-1");
        assertThat(app.getStatus()).isEqualTo(LicenseApplicationStatus.REJECTED);
    }

    @Test
    void cannotSubmitTwice() {
        LicenseApplication app = LicenseApplication.createDraft(
                "LIC-2026-000003",
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "Importer",
                UUID.randomUUID(),
                "Acetone",
                null,
                "{}");
        app.submit(new LocalDate[] {LocalDate.now().plusDays(7)});
        UUID id = app.getId();
        when(applicationRepository.findById(id)).thenReturn(Optional.of(app));

        assertThatThrownBy(() -> service.submit(id, new SubmitLicenseRequest(List.of(LocalDate.now().plusDays(21))), "user-1"))
                .isInstanceOf(IllegalStateException.class);
    }
}
