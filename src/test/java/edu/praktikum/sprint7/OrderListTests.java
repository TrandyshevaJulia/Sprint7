package edu.praktikum.sprint7;

import edu.praktikum.sprint7.steps.OrderSteps;
import edu.praktikum.sprint7.steps.AssertionSteps;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;

public class OrderListTests {

    private static final String BASE_URL = "https://qa-scooter.praktikum-services.ru";
    private OrderSteps orderSteps;
    private AssertionSteps assertionSteps;

    @Before
    public void setUp() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.filters(new AllureRestAssured()); // Подключение Allure
        orderSteps = new OrderSteps();
        assertionSteps = new AssertionSteps();
    }

    @Test
    @DisplayName("Get list of orders")
    @Description("This test verifies that the response body contains a list of orders.")
    public void getListOfOrders() {
        Response response = orderSteps.getOrdersRequest();

        assertionSteps.assertStatusCode(response, 200);
        assertionSteps.assertOrdersListIsPresent(response);
    }
}