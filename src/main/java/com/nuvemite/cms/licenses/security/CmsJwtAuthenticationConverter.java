package com.nuvemite.cms.licenses.security;

import java.util.Collection;
import java.util.List;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class CmsJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        CmsUserPrincipal principal = CmsPrincipalParser.fromJwt(jwt);
        Collection<GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_" + (principal.platformRole() != null
                        ? principal.platformRole()
                        : "ANONYMOUS")));
        return new CmsAuthenticationToken(jwt, authorities, principal);
    }
}
