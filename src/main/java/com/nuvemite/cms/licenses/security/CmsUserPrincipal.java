package com.nuvemite.cms.licenses.security;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record CmsUserPrincipal(
        String subject,
        String platformRole,
        Set<UUID> companyIds,
        Set<UUID> premiseIds,
        Map<UUID, String> companyMembershipRoles) {

    public boolean isRegulator() {
        return "REGULATOR".equals(platformRole);
    }

    public boolean isCompanyMember(UUID companyId) {
        return isRegulator() || companyIds.contains(companyId);
    }

    public boolean canAccessPremise(UUID companyId, UUID premiseId) {
        if (isRegulator()) {
            return true;
        }
        return isCompanyMember(companyId) && premiseIds.contains(premiseId);
    }

    public static CmsUserPrincipal fromClaims(
            String subject,
            String platformRole,
            List<String> companyIdClaims,
            List<String> premiseIdClaims,
            List<Map<String, Object>> membershipClaims) {
        Set<UUID> companyIds = CmsPrincipalParser.parseUuids(companyIdClaims);
        Set<UUID> premiseIds = CmsPrincipalParser.parseUuids(premiseIdClaims);
        Map<UUID, String> roles = parseMemberships(membershipClaims);
        companyIds.addAll(roles.keySet());
        return new CmsUserPrincipal(subject, platformRole, companyIds, premiseIds, roles);
    }

    @SuppressWarnings("unchecked")
    private static Map<UUID, String> parseMemberships(List<Map<String, Object>> memberships) {
        if (memberships == null || memberships.isEmpty()) {
            return Collections.emptyMap();
        }
        return memberships.stream()
                .filter(m -> m.get("companyId") != null)
                .collect(Collectors.toMap(
                        m -> UUID.fromString(String.valueOf(m.get("companyId"))),
                        m -> String.valueOf(m.get("role")),
                        (a, b) -> a));
    }
}
