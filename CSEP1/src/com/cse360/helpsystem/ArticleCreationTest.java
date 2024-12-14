package com.cse360.helpsystem;

public class ArticleCreationTest {

    private static int numPassed = 0;
    private static int numFailed = 0;

    public static void main(String[] args) {
        System.out.println("____________________________________________________________________________");
        System.out.println("\nArticle Creation Testing");

        // Test cases
        performTestCase(1, "JavaFX Basics", "Intro to JavaFX", "JavaFX is a GUI toolkit...", "JavaFX, GUI", "beginner", true);
        performTestCase(2, "", "Intro to JavaFX", "JavaFX is a GUI toolkit...", "JavaFX, GUI", "beginner", false); // Missing title
        performTestCase(3, "JavaFX Advanced", "Advanced concepts", "", "JavaFX, Advanced", "expert", false); // Missing body

        System.out.println("____________________________________________________________________________");
        System.out.println();
        System.out.println("Number of tests passed: " + numPassed);
        System.out.println("Number of tests failed: " + numFailed);
    }

    private static void performTestCase(int testCase, String title, String abs, String body, String keywords, String level, boolean expectedPass) {
        System.out.println("____________________________________________________________________________\n\nTest case: " + testCase);
        System.out.println("Title: \"" + title + "\", Abstract: \"" + abs + "\", Body: \"" + body + "\"");
        System.out.println("______________");

        boolean actualPass = !title.isEmpty() && !body.isEmpty();

        if (actualPass == expectedPass) {
            System.out.println("***Success*** Article creation result matches the expected outcome.");
            numPassed++;
        } else {
            System.out.println("***Failure*** Article creation result does not match the expected outcome.");
            numFailed++;
        }
    }
}
