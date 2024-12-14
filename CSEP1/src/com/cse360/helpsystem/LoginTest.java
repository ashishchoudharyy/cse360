package com.cse360.helpsystem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Set;

public class LoginTest {

    private HelpSystemApp app;

    @BeforeEach
    public void setup() {
        app = new HelpSystemApp();
        app.users.put("testUser", new User("testUser", "Test@1234", Set.of(Role.STUDENT)));
    }

    @Test
    public void testSuccessfulLogin() {
        app.login("testUser", "Test@1234");
        assertNotNull(app.currentUser, "User should be logged in successfully.");
        assertEquals("testUser", app.currentUser.getUsername());
    }

    @Test
    public void testLoginWithInvalidPassword() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            app.login("testUser", "WrongPassword");
        });
        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    public void testLoginWithNonExistentUser() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            app.login("unknownUser", "Test@1234");
        });
        assertEquals("User not found", exception.getMessage());
    }
}
