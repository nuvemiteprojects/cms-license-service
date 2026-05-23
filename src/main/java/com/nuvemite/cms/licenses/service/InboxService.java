package com.nuvemite.cms.licenses.service;

import com.nuvemite.cms.licenses.domain.InboxProcessedEvent;
import com.nuvemite.cms.licenses.messaging.EventTypes;
import com.nuvemite.cms.licenses.repository.InboxProcessedEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InboxService {

    private final InboxProcessedEventRepository repository;

    public InboxService(InboxProcessedEventRepository repository) {
        this.repository = repository;
    }

    public boolean isProcessed(String eventId) {
        return repository.existsByEventIdAndConsumerGroup(eventId, EventTypes.CONSUMER_GROUP);
    }

    @Transactional
    public void markProcessed(String eventId) {
        if (!repository.existsByEventIdAndConsumerGroup(eventId, EventTypes.CONSUMER_GROUP)) {
            repository.save(InboxProcessedEvent.create(eventId, EventTypes.CONSUMER_GROUP));
        }
    }
}
