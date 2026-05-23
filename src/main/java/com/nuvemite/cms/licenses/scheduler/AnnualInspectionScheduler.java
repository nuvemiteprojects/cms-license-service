package com.nuvemite.cms.licenses.scheduler;

import com.nuvemite.cms.licenses.domain.LicenseGrant;
import com.nuvemite.cms.licenses.messaging.EventTypes;
import com.nuvemite.cms.licenses.messaging.events.LicenseAnnualInspectionDueEvent;
import com.nuvemite.cms.licenses.repository.LicenseGrantRepository;
import com.nuvemite.cms.licenses.service.OutboxService;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AnnualInspectionScheduler {

    private static final Logger log = LoggerFactory.getLogger(AnnualInspectionScheduler.class);

    private final LicenseGrantRepository grantRepository;
    private final OutboxService outboxService;

    public AnnualInspectionScheduler(LicenseGrantRepository grantRepository, OutboxService outboxService) {
        this.grantRepository = grantRepository;
        this.outboxService = outboxService;
    }

    @Scheduled(cron = "${cms.licenses.annual-inspection-cron:0 0 2 * * *}")
    @Transactional
    public void emitDueAnnualInspections() {
        LocalDate today = LocalDate.now();
        List<LicenseGrant> due = grantRepository.findAllActive().stream()
                .filter(g -> g.isDueForAnnualNotification(today))
                .toList();
        for (LicenseGrant grant : due) {
            UUID eventId = UUID.randomUUID();
            outboxService.enqueue(
                    "license_grant",
                    grant.getId(),
                    EventTypes.LICENSE_ANNUAL_INSPECTION_DUE,
                    new LicenseAnnualInspectionDueEvent(
                            eventId,
                            grant.getId(),
                            grant.getCompanyId(),
                            grant.getPremiseId(),
                            grant.getLicenseType()));
            grant.markAnnualDueEmitted(today);
            grantRepository.save(grant);
            log.info("Emitted annual inspection due for grant {}", grant.getId());
        }
    }
}
