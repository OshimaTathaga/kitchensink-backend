package util;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.Response;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;

import org.springframework.http.HttpHeaders;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;


public class OAuthUtil {
    public static final String CLIENT_ID = "members-client";
    public static final String REDIRECT_URL = "http://localhost:8080/callback";
    public static final String AUTH_URL = "http://localhost:8080/oauth2/authorize";
    public static final String SCOPE = "api:members";


    public static String getToken() {
        return "";
    }


    public static String getAuthorizationCode() throws NoSuchAlgorithmException {
        Response loginResponse = RestAssured
                .given()
                .formParam("username", "admin@kitchensink.com")
                .formParam("password", "password")
                .post("http://localhost:8080/login");



        String codeVerifier = generateCodeVerifier();
        String codeChallenge = generateCodeChallenge(codeVerifier);

        Response response = RestAssured
                .given()
                .queryParams(Map.of(
                        "response_type", "code",
                        "client_id", CLIENT_ID,
                        "redirect_uri", REDIRECT_URL,
                        "scope", SCOPE,
                        "code_challenge", codeChallenge,
                        "code_challenge_method", "S256"
                ))
                .cookies(loginResponse.getCookies())
                .spec(new RequestSpecBuilder().setBaseUri(AUTH_URL).build())
                .get();
        return null;
    }

    public static String generateCodeVerifier() {
        SecureRandom random = new SecureRandom();
        byte[] codeVerifier = new byte[32];
        random.nextBytes(codeVerifier);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifier);
    }

    public static String generateCodeChallenge(String codeVerifier) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }

    private static HttpHeaders createHeaders(final String username, final String password) {
        HttpHeaders headers = new HttpHeaders();
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.US_ASCII));
        String encodedAuth2 = "Basic " + new String(encodedAuth);
        headers.set(AUTHORIZATION, encodedAuth2);
        return headers;
    }


}
