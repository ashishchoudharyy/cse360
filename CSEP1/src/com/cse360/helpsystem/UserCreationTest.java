package com.cse360.helpsystem;

public class UserCreationTest {

    private static int numPassed = 0;
    private static int numFailed = 0;

    public static void main(String[] args) {
        System.out.println("____________________________________________________________________________");
        System.out.println("\nUser Creation Testing");

        // Test cases
        performTestCase(1, "adminUser", "Admin@1234", true); // Valid user creation
        performTestCase(2, "", "Admin@1234", false); // Empty username
        performTestCase(3, "user123", "password", false); // Invalid password (no uppercase, special char)
        performTestCase(4, "userTest", "Password123", false); // Invalid password (no special char)
        performTestCase(5, "validUser", "Valid@123", true); // Valid user creation

        System.out.println("____________________________________________________________________________");
        System.out.println();
        System.out.println("Number of tests passed: " + numPassed);
        System.out.println("Number of tests failed: " + numFailed);
    }

    private static void performTestCase(int testCase, String username, String password, boolean expectedPass) {
        System.out.println("____________________________________________________________________________\n\nTest case: " + testCase);
        System.out.println("Username: \"" + username + "\", Password: \"" + password + "\"");
        System.out.println("______________");

        String validationResult = HelpSystemApp.validatePassword(password);
        boolean actualPass = (validationResult == null) && !username.isEmpty();

        if (actualPass == expectedPass) {
            System.out.println("***Success*** User creation validation result matches the expected outcome.");
            numPassed++;
        } else {
            System.out.println("***Failure*** User creation validation result does not match the expected outcome.");
            numFailed++;
        }

        if (validationResult != null) {
            System.out.println("Validation message: " + validationResult);
        }
    }
}
