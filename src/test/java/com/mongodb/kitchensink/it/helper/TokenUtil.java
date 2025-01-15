package com.mongodb.kitchensink.it.helper;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

//import java.util.Optional;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;


public class TokenUtil {
//    private static final Pattern CSRF_PATTERN = Pattern.compile("<input\\s+name=\"_csrf\"\\s+type=\"hidden\"\\s+value=\"([^\"]+)\"\\s*/>");

    public static String getAuthorizationToken(String username, String password) {
//        Response csrfResponse = RestAssured.given()
//                .baseUri(Constants.BASE_URI)
//                .request()
//                .accept(ContentType.ANY)
//                .get("/login");
//
//        String csrfToken = Optional.of(CSRF_PATTERN.matcher(csrfResponse.getBody().asString()))
//                .filter(Matcher::find)
//                .map(e -> e.group(1))
//                .orElseThrow(() -> new RuntimeException("Invalid CSRF response"));

        Response loginResponse = RestAssured.given()
                .baseUri(Constants.BASE_URI)
                .request()
                .contentType(ContentType.fromContentType("application/x-www-form-urlencoded"))
                .formParam("username", username)
                .formParam("password", password)
//                .formParam("_csrf", csrfToken)
//                .cookies(csrfResponse.getCookies())
                .post("/login");

        String accessToken = loginResponse.getBody().path("access_token");
        String tokenType = loginResponse.getBody().path("token_type");

        return "%s %s".formatted(tokenType, accessToken);
    }
}
