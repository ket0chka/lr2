package org.example;

public class FunctionHelper {
    // Функция для агента 1: y = -0.5x^2
    public static double calculateFunction1(double x) {
        return -0.5 * x * x;
    }

    // Функция для агента 2: y = 2 ^(-0.1x)
    public static double calculateFunction2(double x) {
        return Math.pow(2, -0.1 * x);
    }

    // Функция для агента 3: y = cos(x)
    public static double calculateFunction3(double x) {
        return Math.cos(x);
    }
}

