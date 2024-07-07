package edu.praktikum.sprint7.steps;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.Assert;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;

public class AssertionSteps {

    @Step("Проверка кода ответа")
    public void assertStatusCode(Response response, int expectedStatusCode) {
        Assert.assertEquals("Неверный статус код", expectedStatusCode, response.statusCode());
    }

    @Step("Проверка сообщения об ошибке")
    public void assertErrorMessage(Response response, String expectedMessage) {
        Assert.assertEquals(expectedMessage, response.jsonPath().getString("message"));
    }

    @Step("Проверка ответа на ok: true")
    public void assertOkResponse(Response response) {
        Assert.assertTrue("Ответ должен содержать ok: true", response.jsonPath().getBoolean("ok"));
    }

    @Step("Проверка наличия списка заказов в ответе")
    public void assertOrdersListIsPresent(Response response) {
        response.then().body("orders", notNullValue());
        response.then().body("orders.size()", greaterThan(0));
    }

    @Step("Проверка наличия трека в ответе")
    public void assertTrackIsPresent(Response response) {
        Assert.assertTrue("Ответ должен содержать track", response.jsonPath().getInt("track") > 0);
    }
}