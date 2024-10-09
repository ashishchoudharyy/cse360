package passwordEvaluationTestbed;

/**
 * <p> PasswordEvaluationTestingAutomation Class </p>
 *
 * <p> Description: This class is to validate the functionality of the PasswordEvaluator.
 * This runs various password inputs to make sure that the PasswordEvaluator class 
 * effectively assesses password validity. </p>
 *
 * <p> Copyright: Lynn Robert Carter © 2022 </p>
 *
 * @author Ashish Kumar
 * @version 1.00 2022-09-30 Initial version for testing PasswordEvaluator functionality
 */
public class PasswordEvaluationTestingAutomation {

    static int numPassed = 0;
    static int numFailed = 0;

    /**
     * The main method that runs test cases.
     */
    public static void main(String[] args) {
        System.out.println("____________________________________________________________________________");
        System.out.println("\nTesting Automation");

        // Existing test cases
        performTestCase(1, "Aa!15678", true); // Valid password
        performTestCase(2, "A!", false); // Too short, lacks digits and lower case
        performTestCase(3, "Aa!15678", false); // Duplicate test, supposed to fail
        performTestCase(4, "A!", true); // Incorrect expectation, supposed to fail
        performTestCase(5, "", true); // Empty password, incorrect expectation

        // Additional test cases to cover other scenarios
        performTestCase(6, "Testcase@6", true); // Valid: Upper, lower, digit, special
        performTestCase(7, "testcase@7", false); // Invalid: no upper-case
        performTestCase(8, "TESTCASE@8", false); // Invalid: no lower-case
        performTestCase(9, "Testcase@", false); // Invalid: no digit
        performTestCase(10, "Testcase10", false); // Invalid: no special character
        performTestCase(11, "testcase", false); // Invalid: no upper, no digit, no special

        System.out.println("____________________________________________________________________________");
        System.out.println();
        System.out.println("Number of tests passed: " + numPassed);
        System.out.println("Number of tests failed: " + numFailed);
    }

    /**
     * This method performs a single test case and checks the validity of a password against the expected outcomes.
     * @param testCase The test case number.
     * @param inputText The password to test.
     * @param expectedPass The expected outcome.
     */
    private static void performTestCase(int testCase, String inputText, boolean expectedPass) {
        System.out.println("____________________________________________________________________________\n\nTest case: " + testCase);
        System.out.println("Input: \"" + inputText + "\"");
        System.out.println("______________");
        System.out.println("\nFinite state machine execution trace:");

        String resultText = PasswordEvaluator.evaluatePassword(inputText);

        System.out.println();

        if (resultText != "") {
            if (expectedPass) {
                System.out.println("***Failure*** The password <" + inputText + "> is invalid." +
                        "\nBut it was supposed to be valid, so this is a failure!\n");
                System.out.println("Error message: " + resultText);
                numFailed++;
            } else {
                System.out.println("***Success*** The password <" + inputText + "> is invalid." +
                        "\nBut it was supposed to be invalid, so this is a pass!\n");
                System.out.println("Error message: " + resultText);
                numPassed++;
            }
        } else {
            if (expectedPass) {
                System.out.println("***Success*** The password <" + inputText +
                        "> is valid, so this is a pass!");
                numPassed++;
            } else {
                System.out.println("***Failure*** The password <" + inputText +
                        "> was judged as valid" +
                        "\nBut it was supposed to be invalid, so this is a failure!");
                numFailed++;
            }
        }
        displayEvaluation();
    }

    /**
     * This displays the evaluation results for the tested passwords.
     */
    private static void displayEvaluation() {
        if (PasswordEvaluator.foundUpperCase)
            System.out.println("At least one upper case letter - Satisfied");
        else
            System.out.println("At least one upper case letter - Not Satisfied");

        if (PasswordEvaluator.foundLowerCase)
            System.out.println("At least one lower case letter - Satisfied");
        else
            System.out.println("At least one lower case letter - Not Satisfied");

        if (PasswordEvaluator.foundNumericDigit)
            System.out.println("At least one digit - Satisfied");
        else
            System.out.println("At least one digit - Not Satisfied");

        if (PasswordEvaluator.foundSpecialChar)
            System.out.println("At least one special character - Satisfied");
        else
            System.out.println("At least one special character - Not Satisfied");

        if (PasswordEvaluator.foundLongEnough)
            System.out.println("At least 8 characters - Satisfied");
        else
            System.out.println("At least 8 characters - Not Satisfied");
    }
}
