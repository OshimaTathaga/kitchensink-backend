package com.mongodb.kitchensink.it.helper;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class OAuthUtil {
    private static final String CLIENT_ID = "members-client";
    private static final String REDIRECT_URL = Constants.BASE_URI + "/test-callback";
    private static final String SCOPE = "api:members";
    private static final Pattern CSRF_PATTERN = Pattern.compile("<input\\s+name=\"_csrf\"\\s+type=\"hidden\"\\s+value=\"([^\"]+)\"\\s*/>");

    public static String getAuthorizationToken(String username, String password) {
        Response csrfResponse = RestAssured.given()
                .baseUri(Constants.BASE_URI)
                .request()
                .accept(ContentType.ANY)
                .get("/login");

        String csrfToken = Optional.of(CSRF_PATTERN.matcher(csrfResponse.getBody().asString()))
                .filter(Matcher::find)
                .map(e -> e.group(1))
                .orElseThrow(() -> new RuntimeException("Invalid CSRF response"));

        Response loginResponse = RestAssured.given()
                .baseUri(Constants.BASE_URI)
                .request()
                .contentType(ContentType.fromContentType("application/x-www-form-urlencoded"))
                .formParam("username", username)
                .formParam("password", password)
                .formParam("_csrf", csrfToken)
                .cookies(csrfResponse.getCookies())
                .post("/login");

        String codeVerifier = generateCodeVerifier();
        String codeChallenge = generateCodeChallenge(codeVerifier);

        Response authorizationResponse = RestAssured.given()
                .baseUri(Constants.BASE_URI)
                .request()
                .redirects()
                .follow(false)
                .queryParams(Map.of(
                        "response_type", "code",
                        "client_id", CLIENT_ID,
                        "redirect_uri", REDIRECT_URL,
                        "scope", SCOPE,
                        "code_challenge", codeChallenge,
                        "code_challenge_method", "S256"
                ))
                .cookies(loginResponse.getCookies())
                .get("/oauth2/authorize");

        String code = URI.create(authorizationResponse.getHeader("Location")).getQuery().replace("code=", "");

        Response tokenResponse = RestAssured.given()
                .baseUri(Constants.BASE_URI)
                .request()
                .contentType(ContentType.fromContentType("application/x-www-form-urlencoded"))
                .formParam("grant_type", "authorization_code")
                .formParam("code", code)
                .formParam("redirect_uri", REDIRECT_URL)
                .formParam("code_verifier", codeVerifier)
                .formParam("client_id", CLIENT_ID)
                .post("/oauth2/token");

        String accessToken = tokenResponse.getBody().path("access_token");
        String tokenType = tokenResponse.getBody().path("token_type");

        return "%s %s".formatted(tokenType, accessToken);
    }

    private static String generateCodeVerifier() {
        SecureRandom random = new SecureRandom();
        byte[] codeVerifier = new byte[32];
        random.nextBytes(codeVerifier);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifier);
    }

    private static String generateCodeChallenge(String codeVerifier) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }
}
