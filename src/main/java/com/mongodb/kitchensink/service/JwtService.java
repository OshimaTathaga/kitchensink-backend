package com.mongodb.kitchensink.service;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.mongodb.kitchensink.config.AppConfigProperties;
import com.mongodb.kitchensink.error.ErrorCode;
import com.mongodb.kitchensink.error.KitchenSinkException;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class JwtService {
    private final JWSSigner jwsSigner;
    private final JWSVerifier jwsVerifier;
    private final JWSHeader jwsHeader;
    private final AppConfigProperties.JwtConfigProperties jwtConfigProperties;

    public JwtService(AppConfigProperties appConfigProperties) throws JOSEException {
        this.jwtConfigProperties = appConfigProperties.jwt();

        JWK jwk = JWK.parseFromPEMEncodedObjects(this.jwtConfigProperties.privateKey());
        this.jwsSigner = new RSASSASigner(jwk.toRSAKey());
        this.jwsVerifier = new RSASSAVerifier(jwk.toRSAKey().toRSAPublicKey());
        this.jwsHeader = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .type(JOSEObjectType.JWT)
                .build();
    }

    public JwtTokenResponse generateJwtToken(Authentication authentication) {
        Date currentDate = new Date();
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(authentication.getName())
                .audience(this.jwtConfigProperties.audience())
                .issuer(this.jwtConfigProperties.issuer())
                .jwtID(UUID.randomUUID().toString())
                .issueTime(currentDate)
                .notBeforeTime(currentDate)
                .expirationTime(Date.from(currentDate.toInstant().plus(this.jwtConfigProperties.expirationInSeconds(), ChronoUnit.SECONDS)))
                .claim("roles", authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                .build();

        SignedJWT signedJWT = new SignedJWT(jwsHeader, claimsSet);
        try {
            signedJWT.sign(jwsSigner);
        } catch (JOSEException e) {
            throw KitchenSinkException.builder().errorCode(ErrorCode.SERVER_ERROR).message(e.getMessage()).build();
        }

        return new JwtTokenResponse("Bearer", signedJWT.serialize(), this.jwtConfigProperties.expirationInSeconds());
    }

    public Authentication getAuthenticatedUser(String jwtToken) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(jwtToken);

            if (!signedJWT.verify(jwsVerifier)) {
                throw new BadCredentialsException("Invalid JWT");
            }

            if (signedJWT.getJWTClaimsSet().getExpirationTime().before(new Date())) {
                throw new CredentialsExpiredException("Expired JWT");
            }

            JWTClaimsSet jwtClaimsSet = signedJWT.getJWTClaimsSet();
            List<SimpleGrantedAuthority> roles = jwtClaimsSet.getListClaim("roles")
                    .stream()
                    .map("ROLE_%s"::formatted)
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            return new UsernamePasswordAuthenticationToken(jwtClaimsSet.getSubject(), null, roles);
        } catch (JOSEException | ParseException e) {
            throw new BadCredentialsException("Unable to parse JWT");
        }
    }

    public record JwtTokenResponse(@JsonProperty("token_type") String tokenType,
                            @JsonProperty("access_token") String accessToken,
                            @JsonProperty("expires_in") long expiresIn) {

    }


}
