package com.mongodb.kitchensink.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    private final JwtGrantedAuthoritiesConverter defaultGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt source) {
        Collection<GrantedAuthority> authorities =
                Stream.concat(defaultGrantedAuthoritiesConverter.convert(source).stream(), extractResourceRoles(source))
                        .collect(Collectors.toSet());
        return new JwtAuthenticationToken(source, authorities);
    }

    private static Stream<? extends GrantedAuthority> extractResourceRoles(Jwt jwt) {
        List<String> resourceAccess = jwt.getClaim("roles");

        return Optional.ofNullable(resourceAccess)
                .stream()
                .flatMap(Collection::stream)
                .map(x -> new SimpleGrantedAuthority("ROLE_" + x));
    }
}
