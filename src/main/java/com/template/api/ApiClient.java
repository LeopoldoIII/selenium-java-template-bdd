package com.template.api;

import com.template.config.ConfigManager;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

/**
 * Reusable HTTP Client based on REST Assured (Facade / Wrapper Pattern).
 * 
 * 1. "Centralizes and standardizes HTTP request configuration and execution across the framework."
 * 2. "Applies the Facade pattern: hides REST Assured boilerplate (base URL, JSON headers, serialization, logging)."
 * 3. "Implements the Utility class pattern: final class with a private constructor and static helper methods."
 * 4. "Improves maintainability: changes to base URLs, global headers, or auth tokens are made in a single place."
 */
public final class ApiClient {

    /**
     * Private constructor to prevent instantiation (Utility Class pattern).
     */
    private ApiClient() {
    }

    /**
     * Builds and returns the default RequestSpecification.
     * 
     * Key responsibilities:
     * - Reads the base API URL dynamically from ConfigManager (supports config.properties or environment overrides).
     * - Sets Content-Type and Accept headers to application/json by default.
     * - Avoids repetitive header configuration across individual tests.
     */
    public static RequestSpecification getRequestSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(ConfigManager.getApiBaseUrl())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .build();
    }

    /**
     * Entry point for REST Assured with default specifications.
     * 
     * Key responsibilities:
     * - Attaches the pre-configured RequestSpecification.
     * - Enables 'log().ifValidationFails()' to print request/response logs ONLY when
     *   a validation or assertion fails, keeping console output clean during successful runs.
     */
    public static RequestSpecification given() {
        return RestAssured.given()
                .spec(getRequestSpec())
                .log().ifValidationFails();
    }

    /**
     * Performs a simple HTTP GET request to a relative endpoint (e.g., "/users").
     * Returns the Response object so step definitions can validate status code, headers, and body.
     */
    public static Response get(String endpoint) {
        return given()
                .when()
                .get(endpoint)
                .then()
                .extract().response();
    }

    /**
     * Performs an HTTP GET request with query parameters (e.g., "/users?page=2&limit=10").
     * Accepts query parameters as a Map<String, Object>.
     */
    public static Response get(String endpoint, Map<String, ?> queryParams) {
        return given()
                .queryParams(queryParams)
                .when()
                .get(endpoint)
                .then()
                .extract().response();
    }

    /**
     * Performs an HTTP POST request with a request payload (POJO, DTO, Map, or JSON String).
     * Automatically serializes the payload to JSON via Jackson and ContentType.JSON.
     */
    public static Response post(String endpoint, Object body) {
        return given()
                .body(body)
                .when()
                .post(endpoint)
                .then()
                .extract().response();
    }

    /**
     * Performs an authenticated HTTP POST request using a Bearer token (OAuth2 / JWT).
     * Appends the 'Authorization: Bearer <token>' header to the request.
     */
    public static Response post(String endpoint, Object body, String token) {
        return given()
                .header("Authorization", "Bearer " + token)
                .body(body)
                .when()
                .post(endpoint)
                .then()
                .extract().response();
    }

    /**
     * Performs an HTTP PUT request to update an existing resource.
     */
    public static Response put(String endpoint, Object body) {
        return given()
                .body(body)
                .when()
                .put(endpoint)
                .then()
                .extract().response();
    }

    /**
     * Performs an HTTP DELETE request to remove a resource by endpoint (e.g., "/users/123").
     */
    public static Response delete(String endpoint) {
        return given()
                .when()
                .delete(endpoint)
                .then()
                .extract().response();
    }
}
