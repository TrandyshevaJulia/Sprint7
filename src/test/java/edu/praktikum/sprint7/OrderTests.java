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
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class OrderTests {

    private static final String BASE_URL = "https://qa-scooter.praktikum-services.ru";
    private final String[] colors;
    private OrderSteps orderSteps;
    private AssertionSteps assertionSteps;

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
        orderSteps = new OrderSteps();
        assertionSteps = new AssertionSteps();
    }

    @Test
    @DisplayName("Create Order with colors")
    @Description("This test verifies that an order can be created with specified colors.")
    public void createOrderWithColors() {
        Order order = new Order("Имя", "Фамилия", "Адрес", "Станция",
                "Телефон", 5, "2023-10-10", "Комментарий", colors);

        Response response = orderSteps.createOrder(order);

        assertionSteps.assertStatusCode(response, 201);
        assertionSteps.assertTrackIsPresent(response);
    }
}