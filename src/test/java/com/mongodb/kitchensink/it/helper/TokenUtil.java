package com.mongodb.kitchensink.it.helper;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;


public class TokenUtil {
    public static String getAuthorizationToken(String username, String password) {
        Response loginResponse = RestAssured.given()
                .baseUri(Constants.BASE_URI)
                .request()
                .contentType(ContentType.fromContentType("application/x-www-form-urlencoded"))
                .formParam("username", username)
                .formParam("password", password)
                .post("/login");

        String accessToken = loginResponse.getBody().path("access_token");
        String tokenType = loginResponse.getBody().path("token_type");

        return "%s %s".formatted(tokenType, accessToken);
    }
}
