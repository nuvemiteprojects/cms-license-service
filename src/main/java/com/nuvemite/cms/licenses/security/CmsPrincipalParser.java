package com.nuvemite.cms.licenses.security;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.oauth2.jwt.Jwt;

public final class CmsPrincipalParser {

    private CmsPrincipalParser() {}

    public static CmsUserPrincipal fromJwt(Jwt jwt) {
        String platformRole = jwt.getClaimAsString("platform_role");
        List<String> companyIds = jwt.getClaimAsStringList("company_ids");
        List<String> premiseIds = jwt.getClaimAsStringList("premise_ids");
        List<Map<String, Object>> memberships = jwt.getClaim("company_memberships");
        return CmsUserPrincipal.fromClaims(
                jwt.getSubject(), platformRole, companyIds, premiseIds, memberships);
    }

    static Set<UUID> parseUuids(List<String> values) {
        if (values == null || values.isEmpty()) {
            return new HashSet<>();
        }
        return values.stream().map(UUID::fromString).collect(Collectors.toCollection(HashSet::new));
    }
}
