package passwordEvaluationTestbed;

/**
 * <p> PasswordEvaluator Class </p>
 *
 * <p> Description: This class reviews a submitted password to meet specific security requirements.
 * It investigates whether upper case and lower case letters along with numbers and special characters
 * meet a specific minimum length. </p>
 *
 * <p> Copyright: Lynn Robert Carter © 2022 </p>
 *
 * @author Ashish Kumar
 * @version 1.00 2022-09-30 Initial version that implements password validation rules
 */
public class PasswordEvaluator {
    public static String passwordErrorMessage = "";
    public static String passwordInput = "";
    public static int passwordIndexofError = -1;
    public static boolean foundUpperCase = false;
    public static boolean foundLowerCase = false;
    public static boolean foundNumericDigit = false;
    public static boolean foundSpecialChar = false;
    public static boolean foundLongEnough = false;
    private static String inputLine = "";
    private static char currentChar;
    private static int currentCharNdx;
    private static boolean running;

    /**
     * Displays the current state of the input being evaluated. This helps in debugging by showing
     * where in the input string the evaluation is currently focused and what character is being processed.
     */
    private static void displayInputState() {
        System.out.println(inputLine);
        System.out.println(inputLine.substring(0, currentCharNdx) + "?");
        System.out.println("The password size: " + inputLine.length() + "  |  The currentCharNdx: " + 
                currentCharNdx + "  |  The currentChar: \"" + currentChar + "\"");
    }

    /**
     * Evaluates a password based on multiple criteria.
     * - Must contain at least one upper case letter
     * - Must contain at least one lower case letter
     * - Must contain at least one numeric digit
     * - Must contain at least one special character
     * - Must be at least 8 characters long
     *
     * @param input The password string to be evaluated.
     * @return A string describing which conditions were not satisfied, if there are any.
     */
    public static String evaluatePassword(String input) {
        passwordErrorMessage = "";
        passwordIndexofError = 0;
        inputLine = input;
        currentCharNdx = 0;
        
        if (input.length() <= 0) return "*** Error *** The password is empty!";

        currentChar = input.charAt(0);        // The current character from the above indexed position

        passwordInput = input;
        foundUpperCase = false;
        foundLowerCase = false;    
        foundNumericDigit = false;
        foundSpecialChar = false;
        foundNumericDigit = false;
        foundLongEnough = false;
        running = true;

        while (running) {
            displayInputState();
            if (currentChar >= 'A' && currentChar <= 'Z') {
                System.out.println("Upper case letter found");
                foundUpperCase = true;
            } else if (currentChar >= 'a' && currentChar <= 'z') {
                System.out.println("Lower case letter found");
                foundLowerCase = true;
            } else if (currentChar >= '0' && currentChar <= '9') {
                System.out.println("Digit found");
                foundNumericDigit = true;
            } else if ("~`!@#$%^&*()_-+={}[]|\\:;\"'<>,.?/".indexOf(currentChar) >= 0) {
                System.out.println("Special character found");
                foundSpecialChar = true;
            } else {
                passwordIndexofError = currentCharNdx;
                return "*** Error *** An invalid character has been found!";
            }
            if (currentCharNdx >= 7) {
                System.out.println("At least 8 characters found");
                foundLongEnough = true;
            }
            currentCharNdx++;
            if (currentCharNdx >= inputLine.length())
                running = false;
            else
                currentChar = input.charAt(currentCharNdx);
            
            System.out.println();
        }
        
        String errMessage = "";
        if (!foundUpperCase)
            errMessage += "Upper case; ";
        
        if (!foundLowerCase)
            errMessage += "Lower case; ";
        
        if (!foundNumericDigit)
            errMessage += "Numeric digits; ";
            
        if (!foundSpecialChar)
            errMessage += "Special character; ";
            
        if (!foundLongEnough)
            errMessage += "Long Enough; ";
        
        if (errMessage == "")
            return "";
        
        passwordIndexofError = currentCharNdx;
        return errMessage + "conditions were not satisfied";
    }
}
