package edu.praktikum.sprint7;

import edu.praktikum.sprint7.steps.CourierSteps;
import edu.praktikum.sprint7.steps.AssertionSteps;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static edu.praktikum.sprint7.CourierGenerator.randomCourier;
import static edu.praktikum.sprint7.Utils.randomString;

public class CourierTests {

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
    @DisplayName("Create Courier")
    @Description("This test verifies that a new courier can be successfully created.")
    public void createCourier() {
        Courier courier = randomCourier();
        Response response = courierSteps.createCourierRequest(courier);

        assertionSteps.assertStatusCode(response, 201);
        assertionSteps.assertOkResponse(response);

        courierId = courierSteps.loginCourier(courier).jsonPath().getInt("id");
    }

    @Test
    @DisplayName("Cannot Create Duplicate Courier")
    @Description("This test verifies that attempting to create a courier with an existing login results in an error.")
    public void cannotCreateDuplicateCourier() {
        Courier courier = randomCourier();
        courierSteps.createCourierRequest(courier).then().statusCode(201);

        // Attempt to create duplicate
        Response response = courierSteps.createCourierRequest(courier);

        assertionSteps.assertStatusCode(response, 409);
        assertionSteps.assertErrorMessage(response, "Этот логин уже используется. Попробуйте другой.");

        courierId = courierSteps.loginCourier(courier).jsonPath().getInt("id");
    }

    @Test
    @DisplayName("All fields are required")
    @Description("This test verifies that all fields (login and password) are required for courier creation.")
    public void allFieldsAreRequired() {
        checkFieldMissing(null, randomString(10), randomString(10), 400); // Без логина
        checkFieldMissing(randomString(10), null, randomString(10), 400); // Без пароля
        checkFieldMissing(randomString(10), randomString(10), "", 201);   // Без имени
    }

    @Test
    @DisplayName("Login is required")
    @Description("This test verifies that login is required for courier creation.")
    public void loginIsRequired() {
        checkFieldMissing("", randomString(10), randomString(10), 400); // Без логина
    }

    @Test
    @DisplayName("Password is required")
    @Description("This test verifies that password is required for courier creation.")
    public void passwordIsRequired() {
        checkFieldMissing(randomString(10), "", randomString(10), 400); // Без пароля
    }

    @Test
    @DisplayName("First name is optional")
    @Description("This test verifies that first name is optional for courier creation.")
    public void firstNameIsOptional() {
        checkFieldMissing(randomString(10), randomString(10), "", 201);   // Без имени
    }

    @Test
    @DisplayName("Successful request returns ok: true")
    @Description("This test verifies that a successful courier creation request returns ok: true.")
    public void successfulRequestReturnsOkTrue() {
        Courier courier = randomCourier();
        Response response = courierSteps.createCourierRequest(courier);

        assertionSteps.assertStatusCode(response, 201);
        assertionSteps.assertOkResponse(response);

        courierId = courierSteps.loginCourier(courier).jsonPath().getInt("id");
    }

    @Test
    @DisplayName("Login with existing login returns error")
    @Description("This test verifies that attempting to create a courier with an existing login results in an error.")
    public void loginWithExistingLoginReturnsError() {
        Courier courier = randomCourier();
        courierSteps.createCourierRequest(courier).then().statusCode(201);

        // Attempt to create duplicate
        Response response = courierSteps.createCourierRequest(courier);

        assertionSteps.assertStatusCode(response, 409);
        assertionSteps.assertErrorMessage(response, "Этот логин уже используется. Попробуйте другой.");

        courierId = courierSteps.loginCourier(courier).jsonPath().getInt("id");
    }

    private void checkFieldMissing(String login, String password, String firstName, int expectedStatusCode) {
        Courier courier = new Courier(login, password, firstName);
        System.out.println("Request body: " + courier);

        Response response = courierSteps.createCourierRequest(courier);

        assertionSteps.assertStatusCode(response, expectedStatusCode);
        if (expectedStatusCode == 400) {
            assertionSteps.assertErrorMessage(response, "Недостаточно данных для создания учетной записи");
        }
    }
}