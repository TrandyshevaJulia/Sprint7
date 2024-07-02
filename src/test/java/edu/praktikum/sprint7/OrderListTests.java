package edu.praktikum.sprint7;

import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;

public class OrderListTests {

    private static final String BASE_URL = "https://qa-scooter.praktikum-services.ru";
    private static final String GET_ORDERS_ENDPOINT = "api/v1/orders";
    private static final String DELETE_ORDER_ENDPOINT = "api/v1/orders/cancel";

    @Before
    public void setUp() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.filters(new AllureRestAssured()); // Подключение Allure
    }

    @Test
    @DisplayName("Get list of orders")
    @Description("This test verifies that the response body contains a list of orders.")
    public void getListOfOrders() {
        Response response = getOrdersRequest();

        assertStatusCode(response, 200);
        assertOrdersListIsPresent(response);
    }

    @Step("Получение списка заказов")
    private Response getOrdersRequest() {
        Response response = given()
                .header("Content-type", "application/json")
                .when()
                .get(GET_ORDERS_ENDPOINT);
        System.out.println("Response: " + response.getBody().asString());
        return response;
    }

    @Step("Удаление заказа")
    private void deleteOrder(int track) {
        given()
                .header("Content-type", "application/json")
                .and()
                .body("{\"track\": \"" + track + "\"}")
                .when()
                .put(DELETE_ORDER_ENDPOINT)
                .then()
                .statusCode(200);
    }

    @Step("Проверка кода ответа")
    private void assertStatusCode(Response response, int expectedStatusCode) {
        response.then().statusCode(expectedStatusCode);
    }

    @Step("Проверка наличия списка заказов в ответе")
    private void assertOrdersListIsPresent(Response response) {
        response.then().body("orders", notNullValue());
        response.then().body("orders.size()", greaterThan(0));
    }
}
