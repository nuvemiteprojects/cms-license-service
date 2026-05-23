package com.nuvemite.cms.licenses.security;

import com.nuvemite.cms.licenses.domain.LicenseApplication;
import com.nuvemite.cms.licenses.exception.AccessDeniedException;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class LicenseAccessService {

    public void requireRegulator() {
        if (!SecurityUtils.currentUser().isRegulator()) {
            throw new AccessDeniedException("Regulator role required");
        }
    }

    public void requireReadAccess(LicenseApplication app) {
        CmsUserPrincipal user = SecurityUtils.currentUser();
        if (user.isRegulator()) {
            return;
        }
        if (!user.canAccessPremise(app.getCompanyId(), app.getPremiseId())) {
            throw new AccessDeniedException("No access to this license application");
        }
    }

    public void requireCompanyWrite(UUID companyId, UUID premiseId) {
        CmsUserPrincipal user = SecurityUtils.currentUser();
        if (user.isRegulator()) {
            return;
        }
        if (!user.canAccessPremise(companyId, premiseId)) {
            throw new AccessDeniedException("No access to premise");
        }
    }

    public void requireCompanySubmit(LicenseApplication app) {
        requireReadAccess(app);
        if (SecurityUtils.currentUser().isRegulator()) {
            throw new AccessDeniedException("Company users submit applications");
        }
    }
}
