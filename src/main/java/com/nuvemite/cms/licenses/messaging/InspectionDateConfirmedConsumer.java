package com.nuvemite.cms.licenses.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuvemite.cms.licenses.messaging.events.InspectionDateConfirmedEvent;
import com.nuvemite.cms.licenses.service.InboxService;
import com.nuvemite.cms.licenses.service.LicenseApplicationService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class InspectionDateConfirmedConsumer {

    private static final Logger log = LoggerFactory.getLogger(InspectionDateConfirmedConsumer.class);

    private final ObjectMapper objectMapper;
    private final InboxService inboxService;
    private final LicenseApplicationService licenseService;

    public InspectionDateConfirmedConsumer(
            ObjectMapper objectMapper, InboxService inboxService, LicenseApplicationService licenseService) {
        this.objectMapper = objectMapper;
        this.inboxService = inboxService;
        this.licenseService = licenseService;
    }

    @KafkaListener(topics = EventTypes.INSPECTION_DATE_CONFIRMED, groupId = EventTypes.CONSUMER_GROUP)
    @Transactional
    public void onDateConfirmed(ConsumerRecord<String, String> record) throws Exception {
        String eventId = header(record, "eventId");
        if (eventId != null && inboxService.isProcessed(eventId)) {
            return;
        }
        InspectionDateConfirmedEvent event = objectMapper.readValue(record.value(), InspectionDateConfirmedEvent.class);
        if (event.licenseApplicationId() == null) {
            log.warn("Date confirmed event missing licenseApplicationId, skipping");
            return;
        }
        licenseService.agreeInspectionDateFromPlanner(event.licenseApplicationId(), event.confirmedDate());
        String id = eventId != null ? eventId : event.eventId().toString();
        inboxService.markProcessed(id);
        log.info("Agreed inspection date for application {}", event.licenseApplicationId());
    }

    private static String header(ConsumerRecord<String, String> record, String name) {
        var header = record.headers().lastHeader(name);
        return header != null ? new String(header.value()) : null;
    }
}
