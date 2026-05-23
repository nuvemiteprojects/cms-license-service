package com.nuvemite.cms.licenses.repository;

import com.nuvemite.cms.licenses.domain.LicenseGrant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LicenseGrantRepository extends JpaRepository<LicenseGrant, UUID> {

    Optional<LicenseGrant> findByApplicationId(UUID applicationId);

    @Query("SELECT g FROM LicenseGrant g WHERE g.status = 'ACTIVE'")
    List<LicenseGrant> findAllActive();
}
