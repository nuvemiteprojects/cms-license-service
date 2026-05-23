package com.nuvemite.cms.licenses.repository;

import com.nuvemite.cms.licenses.domain.LicenseTimelineEvent;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LicenseTimelineEventRepository extends JpaRepository<LicenseTimelineEvent, UUID> {

    List<LicenseTimelineEvent> findByApplicationIdOrderByOccurredAtAsc(UUID applicationId);
}
