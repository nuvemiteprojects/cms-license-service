package com.nuvemite.cms.licenses.repository;

import com.nuvemite.cms.licenses.domain.InboxProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InboxProcessedEventRepository extends JpaRepository<InboxProcessedEvent, InboxProcessedEvent.InboxId> {

    boolean existsByEventIdAndConsumerGroup(String eventId, String consumerGroup);
}
