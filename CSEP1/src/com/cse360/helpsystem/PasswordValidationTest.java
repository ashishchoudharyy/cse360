/*******
 * <p> HelpSystemApp Class </p>
 * 
 * <p> Description: This class is designed for the testing for our program. </p>
 * 
 * <p> Copyright: CSE360 Team(Ashish Kumar, Kamaal Alag, Grace Mower, Ishaan Kurmi, Anshuman Yadav) © 2024 </p>
 * 
 * @author CSE360 Team(Ashish Kumar, Kamaal Alag, Grace Mower, Ishaan Kurmi, Anshuman Yadav)
 * 
 * @version 1.00    2024-03-01 Initial implementation of the CSE360 Help System
 * 
 */

package com.cse360.helpsystem;

public class PasswordValidationTest {

    private static int numPassed = 0;
    private static int numFailed = 0;

    public static void main(String[] args) {
        System.out.println("____________________________________________________________________________");
        System.out.println("\nPassword Validation Testing");

        // Test cases
        performTestCase(1, "Aa!15678", true); // Valid password
        performTestCase(2, "A!", false); // Too short, lacks digits and lower case
        performTestCase(3, "password", false); // No upper case, special char, or digit
        performTestCase(4, "PASSWORD123", false); // No lower case or special char
        performTestCase(5, "Password123", false); // No special char
        performTestCase(6, "Password!", false); // No digit
        performTestCase(7, "Pa$$w0rd", true); // Valid password
        performTestCase(8, "12345678", false); // No upper case, lower case, or special char
        performTestCase(9, "!@#$%^&*", false); // No upper case, lower case, or digit
        performTestCase(10, "", false); // Empty password

        System.out.println("____________________________________________________________________________");
        System.out.println();
        System.out.println("Number of tests passed: " + numPassed);
        System.out.println("Number of tests failed: " + numFailed);
    }

    private static void performTestCase(int testCase, String password, boolean expectedPass) {
        System.out.println("____________________________________________________________________________\n\nTest case: " + testCase);
        System.out.println("Input: \"" + password + "\"");
        System.out.println("______________");

        String validationResult = HelpSystemApp.validatePassword(password);
        boolean actualPass = (validationResult == null);

        if (actualPass == expectedPass) {
            System.out.println("***Success*** The password <" + password + "> validation result matches the expected outcome.");
            numPassed++;
        } else {
            System.out.println("***Failure*** The password <" + password + "> validation result does not match the expected outcome.");
            numFailed++;
        }

        if (validationResult != null) {
            System.out.println("Validation message: " + validationResult);
        }

        displayEvaluation(password);
    }

    private static void displayEvaluation(String password) {
        System.out.println("At least one upper case letter - " + (password.matches(".*[A-Z].*") ? "Satisfied" : "Not Satisfied"));
        System.out.println("At least one lower case letter - " + (password.matches(".*[a-z].*") ? "Satisfied" : "Not Satisfied"));
        System.out.println("At least one digit - " + (password.matches(".*\\d.*") ? "Satisfied" : "Not Satisfied"));
        System.out.println("At least one special character - " + (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*") ? "Satisfied" : "Not Satisfied"));
        System.out.println("At least 8 characters - " + (password.length() >= 8 ? "Satisfied" : "Not Satisfied"));
    }
}