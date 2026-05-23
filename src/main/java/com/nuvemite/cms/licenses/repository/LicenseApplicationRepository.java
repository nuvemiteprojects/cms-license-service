package com.nuvemite.cms.licenses.repository;

import com.nuvemite.cms.licenses.domain.LicenseApplication;
import com.nuvemite.cms.licenses.domain.LicenseApplicationStatus;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LicenseApplicationRepository extends JpaRepository<LicenseApplication, UUID> {

    @Query("""
            SELECT a FROM LicenseApplication a
            WHERE (:companyId IS NULL OR a.companyId = :companyId)
              AND (:premiseId IS NULL OR a.premiseId = :premiseId)
              AND (:status IS NULL OR a.status = :status)
            ORDER BY a.createdAt DESC
            """)
    Page<LicenseApplication> search(UUID companyId, UUID premiseId, LicenseApplicationStatus status, Pageable pageable);
}
