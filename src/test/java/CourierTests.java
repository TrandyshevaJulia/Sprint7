import edu.praktikum.sprint7.Courier;
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

public class CourierTests {

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

    // Проверка, что можно создать курьера
    @Test
    @DisplayName("Create Courier")
    @Description("This test verifies that a new courier can be successfully created.")
    public void createCourier() {
        Courier courier = randomCourier();
        Response response = createCourierRequest(courier);


        assertStatusCode(response, 201);
        assertOkResponse(response);

        courierId = loginCourier(courier);
    }



    // Проверка, что нельзя создать двух одинаковых курьеров
    @Test
    @DisplayName("Cannot Create Duplicate Courier")
    @Description("This test verifies that attempting to create a courier with an existing login results in an error.")
    public void cannotCreateDuplicateCourier() {
        Courier courier = randomCourier();
        createCourierRequest(courier).then().statusCode(201);

        // Attempt to create duplicate
        Response response = createCourierRequest(courier);

        assertStatusCode(response, 409);
        assertErrorMessage(response, "Этот логин уже используется. Попробуйте другой.");

        courierId = loginCourier(courier);
    }

    // Проверка, что все поля обязательны
    @Test
    @DisplayName("All fields are required")
    @Description("This test verifies that all fields (login and password) are required for courier creation.")
    public void allFieldsAreRequired() {
        checkFieldMissing(null, randomString(10), randomString(10), 400); // Без логина
        checkFieldMissing(randomString(10), null, randomString(10), 400); // Без пароля
        checkFieldMissing(randomString(10), randomString(10), "", 201);   // Без имени
    }

    // Проверка, что логин и пароль обязательны
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

    // Проверка, что поле firstName не обязательно
    @Test
    @DisplayName("First name is optional")
    @Description("This test verifies that first name is optional for courier creation.")
    public void firstNameIsOptional() {
        checkFieldMissing(randomString(10), randomString(10), "", 201);   // Без имени
    }

    // Проверка, что успешный запрос возвращает ok: true
    @Test
    @DisplayName("Successful request returns ok: true")
    @Description("This test verifies that a successful courier creation request returns ok: true.")
    public void successfulRequestReturnsOkTrue() {
        Courier courier = randomCourier();
        Response response = createCourierRequest(courier);

        assertStatusCode(response, 201);
        assertOkResponse(response);

        courierId = loginCourier(courier);
    }

    // Проверка, что нельзя создать пользователя с логином, который уже существует
    @Test
    @DisplayName("Login with existing login returns error")
    @Description("This test verifies that attempting to create a courier with an existing login results in an error.")
    public void loginWithExistingLoginReturnsError() {
        Courier courier = randomCourier();
        createCourierRequest(courier).then().statusCode(201);

        // Attempt to create duplicate
        Response response = createCourierRequest(courier);

        assertStatusCode(response, 409);
        assertErrorMessage(response, "Этот логин уже используется. Попробуйте другой.");

        courierId = loginCourier(courier);
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
    private int loginCourier(Courier courier) {
        Response response = given()
                .header("Content-type", "application/json")
                .and()
                .body(courier)
                .when()
                .post(LOGIN_ENDPOINT);

        return response.jsonPath().getInt("id");
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

    @Step("Проверка ответа на ok: true")
    private void assertOkResponse(Response response) {
        Assert.assertTrue("Ответ должен содержать ok: true", response.jsonPath().getBoolean("ok"));
    }

    @Step("Проверка сообщения об ошибке")
    private void assertErrorMessage(Response response, String expectedMessage) {
        Assert.assertEquals(expectedMessage, response.jsonPath().getString("message"));
    }

    @Step("Проверка: если одного из полей нет, запрос возвращает ожидаемый код состояния")
    private void checkFieldMissing(String login, String password, String firstName, int expectedStatusCode) {
        Courier courier = new Courier(login, password, firstName);
        System.out.println("Request body: " + courier);

        Response response = createCourierRequest(courier);

        assertStatusCode(response, expectedStatusCode);
        if (expectedStatusCode == 400) {
            assertErrorMessage(response, "Недостаточно данных для создания учетной записи");
        }
    }
}