package com.nuvemite.cms.licenses.security;

import com.nuvemite.cms.licenses.exception.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static CmsUserPrincipal currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof CmsAuthenticationToken token) {
            return (CmsUserPrincipal) token.getPrincipal();
        }
        if (authentication instanceof JwtAuthenticationToken token) {
            Object principal = token.getPrincipal();
            if (principal instanceof Jwt jwt) {
                return CmsPrincipalParser.fromJwt(jwt);
            }
        }
        if (authentication != null && authentication.getPrincipal() instanceof CmsUserPrincipal principal) {
            return principal;
        }
        throw new AccessDeniedException("Not authenticated");
    }

    public static String currentSubject() {
        return currentUser().subject();
    }
}
