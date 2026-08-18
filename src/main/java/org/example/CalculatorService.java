
package org.example;

public class CalculatorService {

    public void calculate(String expression) {

        try {

            String cleanExpression = expression
                    .replace(" ", "");

            double result;

            if (cleanExpression.contains("+")) {

                String[] numbers = cleanExpression.split("\\+");

                result = Double.parseDouble(numbers[0])
                        + Double.parseDouble(numbers[1]);

            } else if (cleanExpression.contains("-")) {

                String[] numbers = cleanExpression.split("-");

                result = Double.parseDouble(numbers[0])
                        - Double.parseDouble(numbers[1]);

            } else if (cleanExpression.contains("*")) {

                String[] numbers = cleanExpression.split("\\*");

                result = Double.parseDouble(numbers[0])
                        * Double.parseDouble(numbers[1]);

            } else if (cleanExpression.contains("/")) {

                String[] numbers = cleanExpression.split("/");

                double divisor = Double.parseDouble(numbers[1]);

                if (divisor == 0) {
                    System.out.println("Jarvis: Cannot divide by zero.");
                    return;
                }

                result = Double.parseDouble(numbers[0])
                        / divisor;

            } else {

                System.out.println("Jarvis: I don't understand that calculation.");
                return;
            }

            System.out.println("Jarvis: Result = " + result);

        } catch (NumberFormatException e) {

            System.out.println("Jarvis: Invalid calculation.");

        }
    }
}
