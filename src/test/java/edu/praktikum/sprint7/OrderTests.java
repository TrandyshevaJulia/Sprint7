package edu.praktikum.sprint7;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static io.restassured.RestAssured.given;

@RunWith(Parameterized.class)
public class OrderTests {

    private static final String BASE_URL = "https://qa-scooter.praktikum-services.ru";
    private static final String CREATE_ORDER_ENDPOINT = "api/v1/orders";
    private static final String DELETE_ORDER_ENDPOINT = "api/v1/orders/cancel";


    private final String[] colors;

    public OrderTests(String[] colors) {
        this.colors = colors;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {new String[]{"BLACK"}}, // Один цвет
                {new String[]{"GREY"}},  // Один цвет
                {new String[]{"BLACK", "GREY"}}, // Оба цвета
                {new String[]{}} // Без цвета
        });
    }

    @Before
    public void setUp() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.filters(new AllureRestAssured()); // Подключение Allure
    }

    @Test
    @DisplayName("Create Order with colors")
    @Description("This test verifies that an order can be created with specified colors.")
    public void createOrderWithColors() {
        Order order = new Order("Имя", "Фамилия", "Адрес", "Станция",
                "Телефон", 5, "2023-10-10", "Комментарий", colors);

        Response response = createOrderRequest(order);

        assertStatusCode(response, 201);
        assertTrackIsPresent(response);
    }

    @Step("Создание заказа")
    private Response createOrderRequest(Order order) {
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
        Assert.assertEquals("Неверный статус код", expectedStatusCode, response.statusCode());
    }

    @Step("Проверка наличия track в ответе")
    private void assertTrackIsPresent(Response response) {
        Assert.assertTrue("Ответ должен содержать track", response.jsonPath().getInt("track") > 0);
    }
}
