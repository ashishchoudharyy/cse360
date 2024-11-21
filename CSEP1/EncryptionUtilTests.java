package com.cse360.helpsystem;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// Ensure you import your EncryptionUtil class
import com.cse360.helpsystem.EncryptionUtil;

public class EncryptionUtilTests {

    @Test
    public void testEncryptionDecryption() {
        String originalText = "Hello, world!";
        try {
            String encryptedText = EncryptionUtil.encrypt(originalText);
            String decryptedText = EncryptionUtil.decrypt(encryptedText);

            // Check that the encrypted text is not the same as the original text
            assertNotEquals(originalText, encryptedText, "Encrypted text should not match the original text.");

            // Check that the decrypted text matches the original text
            assertEquals(originalText, decryptedText, "Decrypted text should match the original text.");
        } catch (Exception e) {
            fail("Encryption or decryption threw an exception: " + e.getMessage());
        }
    }
}
