package com.nuvemite.cms.licenses.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuvemite.cms.licenses.messaging.events.PaymentCompletedEvent;
import com.nuvemite.cms.licenses.service.InboxService;
import com.nuvemite.cms.licenses.service.LicenseApplicationService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentCompletedConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentCompletedConsumer.class);
    private static final String REFERENCE_TYPE = "LICENSE_APPLICATION";

    private final ObjectMapper objectMapper;
    private final InboxService inboxService;
    private final LicenseApplicationService licenseService;

    public PaymentCompletedConsumer(
            ObjectMapper objectMapper, InboxService inboxService, LicenseApplicationService licenseService) {
        this.objectMapper = objectMapper;
        this.inboxService = inboxService;
        this.licenseService = licenseService;
    }

    @KafkaListener(topics = EventTypes.PAYMENT_COMPLETED, groupId = EventTypes.CONSUMER_GROUP)
    @Transactional
    public void onPaymentCompleted(ConsumerRecord<String, String> record) throws Exception {
        String eventId = header(record, "eventId");
        if (eventId != null && inboxService.isProcessed(eventId)) {
            return;
        }
        PaymentCompletedEvent event = objectMapper.readValue(record.value(), PaymentCompletedEvent.class);
        if (!REFERENCE_TYPE.equals(event.referenceType())) {
            return;
        }
        licenseService.recordPaymentCompleted(event.referenceId(), "Invoice " + event.invoiceId() + " paid");
        String id = eventId != null ? eventId : event.eventId().toString();
        inboxService.markProcessed(id);
        log.info("Recorded payment for license application {}", event.referenceId());
    }

    private static String header(ConsumerRecord<String, String> record, String name) {
        var header = record.headers().lastHeader(name);
        return header != null ? new String(header.value()) : null;
    }
}
