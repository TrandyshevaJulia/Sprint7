package edu.praktikum.sprint7;

import static edu.praktikum.sprint7.Utils.randomString;

public class CourierGenerator {

    public static Courier randomCourier() {
        return new Courier()
                .withLogin(randomString(10))
                .withPassword(randomString(12))
                .withFirstName(randomString(20));
    }
}
