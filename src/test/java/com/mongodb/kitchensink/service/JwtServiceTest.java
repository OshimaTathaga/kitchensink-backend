package com.mongodb.kitchensink.service;

import com.mongodb.kitchensink.config.AppConfigProperties;
import com.mongodb.kitchensink.config.AppConfigProperties.JwtConfigProperties;
import com.mongodb.kitchensink.service.JwtService.JwtTokenResponse;
import com.nimbusds.jose.JOSEException;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {
    private static final String JWT_PRIVATE_KEY = """
            -----BEGIN PRIVATE KEY-----
            MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQDPKORMX4B47pn6
            V7PujdUWnuQBNHcFph4kB2yPaD1Uw+V59CqkfMulctMT0bXTdaH+JX5frx3u4UPs
            ebFqtUTpchYigL0ouYHcbKNeJEDTVbHm4R4Ys2UzA7VIlzA/ion9gsvqfD8qYYVV
            TrJrk5MoMV7ru4wMWmr3VbKH6cMbN5cgIOozC/YQEH1UHZH1GvdiKqHtRdEEg3lj
            ypVrFbHMBeFfJDzKuYo6N9GQtUSjO4VLHQhmZHb1d9nWtO5LpJfCaW+DF+TxstGC
            +ocM4UK/Vuhu4uSEepOFRy2DKFAWzaQebv3qq9+Pk11GxQ/vAjLNaUWM2QNkf6Xa
            QUWjM76nAgMBAAECggEAEwQt8NTTdkZw3Aow+jUK2oi7ZWDrjBkzqxsDXCa1epKA
            /jDruS27g/SGa8tUZZTmye2nLCFnHgaPtaV2I9V//e37Gi+3LYXZ5eITHRE24i97
            pTYFDT9qisRMX92BEFQnVp4rGdtN8RYCp1ISDs39NDNUA0+0C25BY3L6cH+E6XqG
            fljY1ZLkz3lnTz8B6vvRHnHL6uvyCl6OqL3cWh2gjI7n3F0BmVtbA8V3U/4Wiao4
            15A020pIEilcoZCTLt8g7sxOeFXZxcIzfl3xXtg42q3I7jWK6pGgvJzKo1rg7FJC
            tjkERhb8IjmMr/42lqBAxwctwf/vzZGv7ZP+syZgAQKBgQD4ZHVHYGcMojmUq9sE
            On5Lw6eWxLNfKB0rWP7nxZSMKnYlPR2V/OWhBdHZ7jJ6YZuWGnyiAM94dNfXeIuC
            MsbDfNTyBbkvmDeQM/w+a/rI/yRrTLBN6v3mDT+G793LhiKtoYr+Q0VufPQCBnW2
            PQX069UvM+VOPp2xs5HZjHE04QKBgQDVgSUz6UtHSSZHm7xgXhzWE36EfVgFVj8c
            tdZa7JG25lnCdj6RbUbJ7DUnkp7wDdkE5rWooYiYhrEU93jhOiE95FX32GuODAko
            xhTDUYNssO+cn4oHnbgchcCInJaXUpMsmE5yEy0n1GxWwpHLMQNqgeu2o0NKXEmM
            sWlz9IBchwKBgQDFZIfy1gBugMjDhiMB5D4TlYOsA9sLxsQPe6TuqQxYqzHCfCPz
            2srdQNO4nVuN99191sSutXgqoKgaephbOmDMX3qWbsfws8WMGlPdg1ADxQa7InGF
            KjblSCfw4tPedlU+qH7derZFjJUYuut2jF4cegGrhlE28fm9ixdfzAT24QKBgQCY
            EcdExTsICDMKuAMhhIb3d6UaiwihrH+BXIbT9kIHSuTN3FjOTk4oTmTBze4Ev2g+
            1HC5LmzTbsQ8UdO4YCYDJlqREIXzA+GWOBfb0nhQd2oDXaL7r7vOHPo4k4ezOm53
            Sfg9yijUgLm1xhn/5uYu+2C/kmPAWCFO46Jfo9u1iwKBgQDjCE7zaJFH9w14B4wo
            7hDmg8FAoOPTPyRYJh8Ci7f068cEVzezApquVzIIoeRZPtMdJUnQePHFKo1R2by1
            wX7/ASnDdC8fi6W5gsTy62sIOxYJvCbkjx5bdowQpl01gVDbbsLam9IgvP+ZvSt3
            IGpsMHhX41yi6ViHM3RHaRQ4Gg==
            -----END PRIVATE KEY-----
            """;


    private final JwtService jwtService;

    public JwtServiceTest() throws JOSEException {
        JwtConfigProperties jwtConfigProperties = new JwtConfigProperties("test-aud", "http://test-issuer", JWT_PRIVATE_KEY, 10);
        AppConfigProperties appConfigProperties = new AppConfigProperties(null, null, jwtConfigProperties);
        jwtService = new JwtService(appConfigProperties);
    }

    @Test
    void shouldGenerateAToken() {
        JwtTokenResponse token = jwtService.generateJwtToken(new TestingAuthenticationToken("Test User", "Test Password", "ADMIN"));

        assertNotNull(token.accessToken());
    }

    @Test
    void shouldValidateToken() {
        JwtTokenResponse token = jwtService.generateJwtToken(new TestingAuthenticationToken("Test User", "Test Password", "ADMIN"));
        Authentication authentication = jwtService.getAuthenticatedUser(token.accessToken());

        assertTrue(authentication.isAuthenticated());
    }
}