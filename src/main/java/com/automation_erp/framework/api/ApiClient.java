package com.automation_erp.framework.api;

import com.automation_erp.framework.config.ConfigReader;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.Map;

public class ApiClient {

    static {
        RestAssured.baseURI = ConfigReader.getApiBaseUrl();
    }

    private static RequestSpecification getRequestSpec() {
        return RestAssured.given()
                .contentType("application/json")
                .accept("application/json");
    }

    private static RequestSpecification getRequestSpec(String token) {
        return getRequestSpec().header("Authorization", "Bearer " + token);
    }

    public static Response get(String endpoint, String token, Map<String, Object> queryParams) {
        RequestSpecification request = getRequestSpec(token);
        if (queryParams != null && !queryParams.isEmpty()) {
            request.queryParams(queryParams);
        }
        return request.get(endpoint);
    }

    public static Response post(String endpoint, String token, Object body, String idempotencyKey) {
        RequestSpecification request = getRequestSpec(token);
        if (body != null) {
            request.body(body);
        }
        if (idempotencyKey != null && !idempotencyKey.isEmpty()) {
            request.header("Idempotency-Key", idempotencyKey);
        }
        return request.post(endpoint);
    }

    public static Response post(String endpoint, String token, Object body) {
        return post(endpoint, token, body, null);
    }

    public static Response put(String endpoint, String token, Object body) {
        RequestSpecification request = getRequestSpec(token);
        if (body != null) {
            request.body(body);
        }
        return request.put(endpoint);
    }

    public static Response patch(String endpoint, String token, Object body) {
        RequestSpecification request = getRequestSpec(token);
        if (body != null) {
            request.body(body);
        }
        return request.patch(endpoint);
    }

    public static Response delete(String endpoint, String token) {
        return getRequestSpec(token).delete(endpoint);
    }

    public static String login(String username, String password) {
        
        Response response = RestAssured.given()
                .contentType("application/json")
                .body(Map.of("email", username, "password", password))
                .post("/auth/login");
        
        if (response.getStatusCode() == 200) {
            String token = response.jsonPath().getString("data.access_token");
            return (token != null) ? token : response.jsonPath().getString("token");
        }
        
        return "mocked-token-for-" + username;
    }
}
