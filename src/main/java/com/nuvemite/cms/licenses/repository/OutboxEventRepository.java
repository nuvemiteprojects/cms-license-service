package com.nuvemite.cms.licenses.repository;

import com.nuvemite.cms.licenses.domain.OutboxEvent;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    @Query(
            value = """
                    SELECT * FROM outbox_event
                    WHERE published_at IS NULL
                    ORDER BY created_at
                    LIMIT :limit
                    """,
            nativeQuery = true)
    List<OutboxEvent> findUnpublished(int limit);
}
