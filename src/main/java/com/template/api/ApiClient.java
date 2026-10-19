package com.template.api;

import com.template.config.ConfigManager;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

/**
 * Reusable REST Assured API Client.
 * Provides pre-configured RequestSpecification and clean HTTP helpers.
 */
public final class ApiClient {

    private ApiClient() {
    }

    public static RequestSpecification getRequestSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(ConfigManager.getApiBaseUrl())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .build();
    }

    public static RequestSpecification given() {
        return RestAssured.given()
                .spec(getRequestSpec())
                .log().ifValidationFails();
    }

    public static Response get(String endpoint) {
        return given()
                .when()
                .get(endpoint)
                .then()
                .extract().response();
    }

    public static Response get(String endpoint, Map<String, ?> queryParams) {
        return given()
                .queryParams(queryParams)
                .when()
                .get(endpoint)
                .then()
                .extract().response();
    }

    public static Response post(String endpoint, Object body) {
        return given()
                .body(body)
                .when()
                .post(endpoint)
                .then()
                .extract().response();
    }

    public static Response post(String endpoint, Object body, String token) {
        return given()
                .header("Authorization", "Bearer " + token)
                .body(body)
                .when()
                .post(endpoint)
                .then()
                .extract().response();
    }

    public static Response put(String endpoint, Object body) {
        return given()
                .body(body)
                .when()
                .put(endpoint)
                .then()
                .extract().response();
    }

    public static Response delete(String endpoint) {
        return given()
                .when()
                .delete(endpoint)
                .then()
                .extract().response();
    }
}
