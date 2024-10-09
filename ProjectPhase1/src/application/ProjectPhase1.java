package application;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProjectPhase1 extends Application {

    private List<User> userList = new ArrayList<>();
    private List<Content> contentList = new ArrayList<>();
    private User adminUser;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("CSE 360 Help System");

        VBox layout = new VBox(10);
        layout.setPadding(new javafx.geometry.Insets(20));

        // Initialize login and registration components
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm Password");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First Name");
        TextField middleNameField = new TextField();
        middleNameField.setPromptText("Middle Name");
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");
        TextField preferredNameField = new TextField();
        preferredNameField.setPromptText("Preferred Name");

        Button loginButton = new Button("Login");
        Button registerButton = new Button("Register");
        Button logoutButton = new Button("Logout");
        Label messageLabel = new Label();

        // Initialize content management components
        TableView<Content> contentTable = new TableView<>();
        contentTable.setPrefHeight(200);
        Button addContentButton = new Button("Add Content");
        Button editContentButton = new Button("Edit Content");
        Button deleteContentButton = new Button("Delete Content");

        layout.getChildren().addAll(new Label("Login or Register:"), usernameField, passwordField, confirmPasswordField, emailField, firstNameField, middleNameField, lastNameField, preferredNameField, loginButton, registerButton, messageLabel);

        Scene scene = new Scene(layout, 400, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Actions
        loginButton.setOnAction(event -> handleLogin(usernameField, passwordField, layout, contentTable, messageLabel));
        registerButton.setOnAction(event -> handleRegister(usernameField, passwordField, confirmPasswordField, emailField, firstNameField, middleNameField, lastNameField, preferredNameField, messageLabel));
        logoutButton.setOnAction(event -> handleLogout(layout, usernameField, passwordField, confirmPasswordField, emailField, firstNameField, middleNameField, lastNameField, preferredNameField, messageLabel, contentTable));
    }

    private void handleLogin(TextField usernameField, PasswordField passwordField, VBox layout, TableView<Content> contentTable, Label messageLabel) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        Optional<User> matchingUser = userList.stream()
            .filter(user -> user.getUsername().equals(username) && user.getPassword().equals(password))
            .findFirst();

        if (matchingUser.isPresent()) {
            SessionManager.setCurrentUser(matchingUser.get());
            layout.getChildren().clear();
            layout.getChildren().addAll(new Label("Content Management:"), contentTable, new Button("Add Content"), new Button("Edit Content"), new Button("Delete Content"), new Button("Logout"));
            messageLabel.setText("Login successful!");
        } else {
            messageLabel.setText("Login failed: Incorrect username or password.");
        }
    }

    private void handleRegister(TextField usernameField, PasswordField passwordField, PasswordField confirmPasswordField, TextField emailField, TextField firstNameField, TextField middleNameField, TextField lastNameField, TextField preferredNameField, Label messageLabel) {
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            messageLabel.setText("Passwords do not match.");
            return;
        }

        String username = usernameField.getText();
        String password = passwordField.getText();
        User newUser = new User(userList.size() + 1, username, password, new Role("User"));  // Default role
        userList.add(newUser);
        messageLabel.setText("Registration successful. Please login.");
    }

    private void handleLogout(VBox layout, TextField usernameField, PasswordField passwordField, PasswordField confirmPasswordField, TextField emailField, TextField firstNameField, TextField middleNameField, TextField lastNameField, TextField preferredNameField, Label messageLabel, TableView<Content> contentTable) {
        SessionManager.clearCurrentUser();
        layout.getChildren().clear();
        layout.getChildren().addAll(usernameField, passwordField, confirmPasswordField, emailField, firstNameField, middleNameField, lastNameField, preferredNameField, new Button("Login"), new Button("Register"), messageLabel);
        messageLabel.setText("Logged out successfully.");
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
