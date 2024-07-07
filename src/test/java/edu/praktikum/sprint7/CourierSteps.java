package edu.praktikum.sprint7.steps;

import edu.praktikum.sprint7.Courier;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class CourierSteps {

    private static final String CREATE_ENDPOINT = "api/v1/courier";
    private static final String LOGIN_ENDPOINT = "api/v1/courier/login";

    @Step("Создание курьера")
    public Response createCourierRequest(Courier courier) {
        Response response = given()
                .header("Content-type", "application/json")
                .and()
                .body(courier)
                .when()
                .post(CREATE_ENDPOINT);
        System.out.println("Request body: " + courier);
        System.out.println("Response: " + response.getBody().asString());
        return response;
    }

    @Step("Логин курьера")
    public Response loginCourier(Courier courier) {
        Response response = given()
                .header("Content-type", "application/json")
                .and()
                .body(courier)
                .when()
                .post(LOGIN_ENDPOINT);
        System.out.println("Login response: " + response.getBody().asString());
        return response;
    }

    @Step("Удаление курьера")
    public void deleteCourier(int courierId) {
        given()
                .header("Content-type", "application/json")
                .when()
                .delete(CREATE_ENDPOINT + "/" + courierId)
                .then()
                .statusCode(200);
    }
}
