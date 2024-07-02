package edu.praktikum.sprint7;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static edu.praktikum.sprint7.CourierGenerator.randomCourier;
import static edu.praktikum.sprint7.Utils.randomString;
import static io.restassured.RestAssured.given;

public class CourierLoginTests {

    private static final String BASE_URL = "https://qa-scooter.praktikum-services.ru";
    private static final String CREATE_ENDPOINT = "api/v1/courier";
    private static final String LOGIN_ENDPOINT = "api/v1/courier/login";
    private int courierId = -1; // Переменная для хранения ID созданного курьера



    @Before
    public void setUp() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.filters(new AllureRestAssured()); // Подключение Allure

    }

    @After
    public void tearDown() {
        if (courierId != -1) {
            deleteCourier(courierId);
        }
    }

    @Test
    @DisplayName("Courier can login")
    @Description("This test verifies that a courier can successfully login.")
    public void courierCanLogin() {
        Courier courier = randomCourier();
        createCourierAndLogin(courier);
    }

    @Test
    @DisplayName("Login is required for login")
    @Description("This test verifies that login is required for courier login.")
    public void loginIsRequiredForLogin() {
        Courier courier = randomCourier();
        createCourierRequest(courier).then().statusCode(201);

        checkLoginFieldMissing("", courier.getPassword(), 400); // Без логина
    }

    @Test
    @DisplayName("Password is required for login")
    @Description("This test verifies that password is required for courier login.")
    public void passwordIsRequiredForLogin() {
        Courier courier = randomCourier();
        createCourierRequest(courier).then().statusCode(201);

        checkLoginFieldMissing(courier.getLogin(), "", 400); // Без пароля
    }


    @Test
    @DisplayName("Incorrect login or password returns error")
    @Description("This test verifies that an incorrect login or password returns an error.")
    public void incorrectLoginOrPasswordReturnsError() {
        Courier courier = randomCourier();
        createCourierRequest(courier).then().statusCode(201);

        checkLogin(courier.getLogin() + "incorrect", courier.getPassword(), 404, "Учетная запись не найдена"); // Неверный логин
        checkLogin(courier.getLogin(), courier.getPassword() + "incorrect", 404, "Учетная запись не найдена"); // Неверный пароль
    }

    @Test
    @DisplayName("Non-existent user returns error")
    @Description("This test verifies that logging in with a non-existent user returns an error.")
    public void nonExistentUserReturnsError() {
        checkLogin(randomString(10), randomString(10), 404, "Учетная запись не найдена");
    }

    @Test
    @DisplayName("Successful login returns id")
    @Description("This test verifies that a successful login returns an id.")
    public void successfulLoginReturnsId() {
        Courier courier = randomCourier();
        createCourierRequest(courier).then().statusCode(201);

        Response response = loginCourier(courier);
        Assert.assertTrue("Ответ должен содержать id", response.jsonPath().getInt("id") > 0);
    }

    @Step("Создание курьера и логин")
    private void createCourierAndLogin(Courier courier) {
        createCourierRequest(courier).then().statusCode(201);
        loginCourier(courier).then().statusCode(200);
    }

    @Step("Создание курьера")
    private Response createCourierRequest(Courier courier) {
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
    private Response loginCourier(Courier courier) {
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
    private void deleteCourier(int courierId) {
        given()
                .header("Content-type", "application/json")
                .when()
                .delete(CREATE_ENDPOINT + "/" + courierId)
                .then()
                .statusCode(200);
    }

    @Step("Проверка кода ответа")
    private void assertStatusCode(Response response, int expectedStatusCode) {
        Assert.assertEquals("Неверный статус код", expectedStatusCode, response.statusCode());
    }

    @Step("Проверка сообщения об ошибке")
    private void assertErrorMessage(Response response, String expectedMessage) {
        Assert.assertEquals(expectedMessage, response.jsonPath().getString("message"));
    }

    @Step("Проверка: если одного из полей нет, запрос на логин возвращает ожидаемый код состояния")
    private void checkLoginFieldMissing(String login, String password, int expectedStatusCode) {
        Courier courier = new Courier(login, password, null);
        System.out.println("Login request body: " + courier);

        Response response = loginCourier(courier);

        assertStatusCode(response, expectedStatusCode);
        if (expectedStatusCode == 400) {
            assertErrorMessage(response, "Недостаточно данных для входа");
        }
    }

    @Step("Проверка логина с ожидаемым кодом состояния")
    private void checkLogin(String login, String password, int expectedStatusCode, String expectedMessage) {
        Courier courier = new Courier(login, password, null);
        System.out.println("Login request body: " + courier);

        Response response = loginCourier(courier);

        assertStatusCode(response, expectedStatusCode);
        assertErrorMessage(response, expectedMessage);
    }
}
