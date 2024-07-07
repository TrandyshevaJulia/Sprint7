package edu.praktikum.sprint7.steps;

import edu.praktikum.sprint7.Order;
import edu.praktikum.sprint7.CancelOrderRequest;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class OrderSteps {

    private static final String GET_ORDERS_ENDPOINT = "api/v1/orders";
    private static final String DELETE_ORDER_ENDPOINT = "api/v1/orders/cancel";
    private static final String CREATE_ORDER_ENDPOINT = "api/v1/orders";

    @Step("Получение списка заказов")
    public Response getOrdersRequest() {
        Response response = given()
                .header("Content-type", "application/json")
                .when()
                .get(GET_ORDERS_ENDPOINT);
        System.out.println("Response: " + response.getBody().asString());
        return response;
    }

    @Step("Удаление заказа")
    public void deleteOrder(int track) {
        CancelOrderRequest cancelOrderRequest = new CancelOrderRequest(track);
        given()
                .header("Content-type", "application/json")
                .and()
                .body(cancelOrderRequest)
                .when()
                .put(DELETE_ORDER_ENDPOINT)
                .then()
                .statusCode(200);
    }

    @Step("Создание заказа")
    public Response createOrder(Order order) {
        Response response = given()
                .header("Content-type", "application/json")
                .and()
                .body(order)
                .when()
                .post(CREATE_ORDER_ENDPOINT);
        System.out.println("Request body: " + order);
        System.out.println("Response: " + response.getBody().asString());
        return response;
    }
}