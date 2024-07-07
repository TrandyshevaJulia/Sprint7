package edu.praktikum.sprint7;

import edu.praktikum.sprint7.steps.CourierSteps;
import edu.praktikum.sprint7.steps.AssertionSteps;
import io.qameta.allure.Description;
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

public class CourierLoginTests {

    private static final String BASE_URL = "https://qa-scooter.praktikum-services.ru";
    private int courierId = -1; // Переменная для хранения ID созданного курьера
    private CourierSteps courierSteps;
    private AssertionSteps assertionSteps;

    @Before
    public void setUp() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.filters(new AllureRestAssured()); // Подключение Allure
        courierSteps = new CourierSteps();
        assertionSteps = new AssertionSteps();
    }

    @After
    public void tearDown() {
        if (courierId != -1) {
            courierSteps.deleteCourier(courierId);
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
        courierSteps.createCourierRequest(courier).then().statusCode(201);

        checkLoginFieldMissing("", courier.getPassword(), 400); // Без логина
    }

    @Test
    @DisplayName("Password is required for login")
    @Description("This test verifies that password is required for courier login.")
    public void passwordIsRequiredForLogin() {
        Courier courier = randomCourier();
        courierSteps.createCourierRequest(courier).then().statusCode(201);

        checkLoginFieldMissing(courier.getLogin(), "", 400); // Без пароля
    }

    @Test
    @DisplayName("Incorrect login or password returns error")
    @Description("This test verifies that an incorrect login or password returns an error.")
    public void incorrectLoginOrPasswordReturnsError() {
        Courier courier = randomCourier();
        courierSteps.createCourierRequest(courier).then().statusCode(201);

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
        courierSteps.createCourierRequest(courier).then().statusCode(201);

        Response response = courierSteps.loginCourier(courier);
        Assert.assertTrue("Ответ должен содержать id", response.jsonPath().getInt("id") > 0);
    }

    private void createCourierAndLogin(Courier courier) {
        courierSteps.createCourierRequest(courier).then().statusCode(201);
        courierSteps.loginCourier(courier).then().statusCode(200);
    }

    private void checkLoginFieldMissing(String login, String password, int expectedStatusCode) {
        Courier courier = new Courier(login, password, null);
        System.out.println("Login request body: " + courier);

        Response response = courierSteps.loginCourier(courier);

        assertionSteps.assertStatusCode(response, expectedStatusCode);
        if (expectedStatusCode == 400) {
            assertionSteps.assertErrorMessage(response, "Недостаточно данных для входа");
        }
    }

    private void checkLogin(String login, String password, int expectedStatusCode, String expectedMessage) {
        Courier courier = new Courier(login, password, null);
        System.out.println("Login request body: " + courier);

        Response response = courierSteps.loginCourier(courier);

        assertionSteps.assertStatusCode(response, expectedStatusCode);
        assertionSteps.assertErrorMessage(response, expectedMessage);
    }
}