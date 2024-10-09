package application;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class ProjectPhase1 extends Application {

    private List<User> userList = new ArrayList<>();
    private List<Content> contentList = new ArrayList<>();
    private User adminUser;
    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("CSE 360 Help System");

        VBox layout = new VBox(10);
        layout.setPadding(new javafx.geometry.Insets(20));

        // Login and Registration UI Components
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        Button loginButton = new Button("Login");
        Button registerButton = new Button("Register");
        Button logoutButton = new Button("Logout");
        Label messageLabel = new Label();

        // Admin Dashboard Components
        Button inviteUserButton = new Button("Invite User");
        Button manageRolesButton = new Button("Manage Roles");
        Button deleteUserButton = new Button("Delete User");
        Button resetPasswordButton = new Button("Reset Password");

        // Setup initial scene
        layout.getChildren().addAll(new Label("Login or Register:"), usernameField, passwordField, loginButton, registerButton, messageLabel);
        Scene scene = new Scene(layout, 400, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Action Handlers
        loginButton.setOnAction(event -> handleLogin(usernameField, passwordField, layout, messageLabel));
        registerButton.setOnAction(event -> handleRegister(usernameField, passwordField, messageLabel));
        inviteUserButton.setOnAction(event -> handleInviteUser(layout, messageLabel));
        manageRolesButton.setOnAction(event -> handleManageRoles(layout, messageLabel));
        deleteUserButton.setOnAction(event -> handleDeleteUser(layout, messageLabel));
        resetPasswordButton.setOnAction(event -> handleResetPassword(layout, messageLabel));
        logoutButton.setOnAction(event -> handleLogout(layout, usernameField, passwordField, messageLabel));
    }

    // Method to handle login functionality
    private void handleLogin(TextField usernameField, PasswordField passwordField, VBox layout, Label messageLabel) {
        // Implementation for user login
    }

    // Method to handle user registration
    private void handleRegister(TextField usernameField, PasswordField passwordField, Label messageLabel) {
        // Implementation for user registration
    }

    // Method to handle inviting a new user
    private void handleInviteUser(VBox layout, Label messageLabel) {
        String inviteCode = generateRandomCode();
        // Implementation for inviting a new user with an invite code
    }

    // Method to handle role management
    private void handleManageRoles(VBox layout, Label messageLabel) {
        // Implementation for managing roles
    }

    // Method to handle deleting a user
    private void handleDeleteUser(VBox layout, Label messageLabel) {
        // Implementation for deleting a user
    }

    // Method to handle password reset
    private void handleResetPassword(VBox layout, Label messageLabel) {
        // Implementation for resetting a password
    }

    // Method to handle user logout
    private void handleLogout(VBox layout, TextField usernameField, PasswordField passwordField, Label messageLabel) {
        // Implementation for logging out a user
    }

    // Helper method to generate a random alphanumeric code
    private String generateRandomCode() {
        int count = 10;
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int)(Math.random() * ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

    public static void main(String[] args) {
        launch(args);
    }

    // Inner classes for User, Role, Content, and SessionManager...
    
    // Inner class: User
    public class User {
        private int id;
        private String username;
        private String password;
        private Role role;
        private String email;
        private String firstName;
        private String middleName;
        private String lastName;
        private String preferredName;

        public User(int id, String username, String password, Role role) {
            this.id = id;
            this.username = username;
            this.password = password;
            this.role = role;
        }

        // Getters and setters
        public int getId() { return id; }
        public String getUsername() { return username; }
        public String getPassword() { return password; }
        public Role getRole() { return role; }
        public void setRole(Role role) { this.role = role; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getMiddleName() { return middleName; }
        public void setMiddleName(String middleName) { this.middleName = middleName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getPreferredName() { return preferredName; }
        public void setPreferredName(String preferredName) { this.preferredName = preferredName; }
    }

    // Inner class: Role
    public class Role {
        private String name;

        public Role(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    // Inner class: Content
    public class Content {
        private int id;
        private String title;
        private String description;
        private String body;

        public Content(int id, String title, String description, String body) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.body = body;
        }

        public int getId() { return id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getBody() { return body; }
        public void setBody(String body) { this.body = body; }
    }

    // Inner class: SessionManager
    public static class SessionManager {
        private static User currentUser;

        public static User getCurrentUser() { return currentUser; }
        public static void setCurrentUser(User user) { currentUser = user; }
        public static void clearCurrentUser() { currentUser = null; }
    }
}
