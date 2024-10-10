package com.cse360.helpsystem;

import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import java.util.stream.Collectors;

import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import java.util.UUID;
import java.util.HashSet;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.*;
import java.util.stream.Collectors;

public class HelpSystemApp extends Application {

    private Map<String, User> users = new HashMap<>();
    private User currentUser;
    private Stage primaryStage;
    private Map<String, Set<Role>> inviteCodes = new HashMap<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("CSE360 Help System");
        showLoginPage();
    }
    
    private void showLoginPage() {
        GridPane grid = createGrid();

        Label titleLabel = new Label("CSE360 Help System");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        Button loginButton = new Button("Login");
        Label inviteCodeLabel = new Label("Invite Code:");
        TextField inviteCodeField = new TextField();
        Button registerButton = new Button("Register with Invite Code");

        grid.add(titleLabel, 0, 0, 2, 1);
        grid.add(usernameLabel, 0, 1);
        grid.add(usernameField, 1, 1);
        grid.add(passwordLabel, 0, 2);
        grid.add(passwordField, 1, 2);
        grid.add(loginButton, 1, 3);
        grid.add(inviteCodeLabel, 0, 4);
        grid.add(inviteCodeField, 1, 4);
        grid.add(registerButton, 1, 5);

        loginButton.setOnAction(e -> login(usernameField.getText(), passwordField.getText()));
        registerButton.setOnAction(e -> {
            String inviteCode = inviteCodeField.getText().trim();
            if (inviteCode.isEmpty()) {
                showAlert("Please enter an invite code.");
            } else if (inviteCodes.containsKey(inviteCode)) {
                showRegistrationPage(inviteCode);
            } else {
                showAlert("Invalid invite code.");
            }
        });

        Scene scene = new Scene(grid, 300, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
  
    
    private void login(String username, String password) {
        if (users.isEmpty()) {
            String passwordValidationResult = validatePassword(password);
            if (passwordValidationResult != null) {
                showAlert(passwordValidationResult);
                return;
            }
            
            User admin = new User(username, password, new HashSet<>(Arrays.asList(Role.ADMIN)));
            users.put(username, admin);
            currentUser = admin;
            showSetupAccountPage();
        } else if (users.containsKey(username)) {
            User user = users.get(username);
            if (user.isPasswordResetRequired()) {
                if (user.getOneTimePassword() != null && user.getOneTimePassword().equals(password)) {
                    if (user.getOneTimePasswordExpiration().after(new Date())) {
                        currentUser = user;
                        showResetPasswordPage();
                    } else {
                        showAlert("One-time password has expired. Please contact an administrator.");
                    }
                } else {
                    showAlert("Invalid credentials. Please use the one-time password provided.");
                }
            } else if (user.getPassword().equals(password)) {
                currentUser = user;
                if (!user.isSetupComplete()) {
                    showSetupAccountPage();
                } else if (user.getRoles().size() > 1) {
                    showRoleSelectionPage();
                } else {
                    showHomePage(user.getRoles().iterator().next());
                }
            } else {
                showAlert("Invalid credentials");
            }
        } else {
            showAlert("User not found");
        }
    }
        
    private String validatePassword(String password) {
        StringBuilder missingCriteria = new StringBuilder("Password must contain:\n");
        boolean isValid = true;

        if (!password.matches(".*[A-Z].*")) {
            missingCriteria.append("- An uppercase letter\n");
            isValid = false;
        }
        if (!password.matches(".*[a-z].*")) {
            missingCriteria.append("- A lowercase letter\n");
            isValid = false;
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            missingCriteria.append("- A special character\n");
            isValid = false;
        }
        if (!password.matches(".*\\d.*")) {
            missingCriteria.append("- A digit\n");
            isValid = false;
        }
        if (password.length() < 8) {
            missingCriteria.append("- At least 8 characters long\n");
            isValid = false;
        }

        return isValid ? null : missingCriteria.toString();
    }
    
    
    private void showRegistrationPage(String inviteCode) {
        GridPane grid = createGrid();

        Label titleLabel = new Label("Register New User");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        Label confirmPasswordLabel = new Label("Confirm Password:");
        PasswordField confirmPasswordField = new PasswordField();
        Button registerButton = new Button("Register");

        grid.add(titleLabel, 0, 0, 2, 1);
        grid.add(usernameLabel, 0, 1);
        grid.add(usernameField, 1, 1);
        grid.add(passwordLabel, 0, 2);
        grid.add(passwordField, 1, 2);
        grid.add(confirmPasswordLabel, 0, 3);
        grid.add(confirmPasswordField, 1, 3);
        grid.add(registerButton, 1, 4);

        registerButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            if (username.isEmpty()) {
                showAlert("Please enter a username.");
                return;
            }

            String passwordValidationResult = validatePassword(password);
            if (passwordValidationResult != null) {
                showAlert(passwordValidationResult);
                return;
            }

            if (!password.equals(confirmPassword)) {
                showAlert("Passwords do not match");
                return;
            }

            register(username, password, inviteCode);
        });

        Scene scene = new Scene(grid, 300, 250);
        primaryStage.setScene(scene);
    }


    private void register(String username, String password, String inviteCode) {
        if (inviteCodes.containsKey(inviteCode)) {
            Set<Role> roles = inviteCodes.get(inviteCode);
            User newUser = new User(username, password, roles);
            newUser.setSetupComplete(false);
            users.put(username, newUser);
            currentUser = newUser;
            inviteCodes.remove(inviteCode);
            showSetupAccountPage();
        } else {
            showAlert("Invalid invite code");
        }
    }

    
    private void showSetupAccountPage() {
        GridPane grid = createGrid();

        Label titleLabel = new Label("Complete Your Account Setup");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField();
        Label firstNameLabel = new Label("First Name:");
        TextField firstNameField = new TextField();
        Label middleNameLabel = new Label("Middle Name (optional):");
        TextField middleNameField = new TextField();
        Label lastNameLabel = new Label("Last Name:");
        TextField lastNameField = new TextField();
        Label preferredNameLabel = new Label("Preferred Name (optional):");
        TextField preferredNameField = new TextField();
        Button submitButton = new Button("Submit");

        grid.add(titleLabel, 0, 0, 2, 1);
        grid.add(emailLabel, 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(firstNameLabel, 0, 2);
        grid.add(firstNameField, 1, 2);
        grid.add(middleNameLabel, 0, 3);
        grid.add(middleNameField, 1, 3);
        grid.add(lastNameLabel, 0, 4);
        grid.add(lastNameField, 1, 4);
        grid.add(preferredNameLabel, 0, 5);
        grid.add(preferredNameField, 1, 5);
        grid.add(submitButton, 1, 6);

        submitButton.setOnAction(e -> {
            String email = emailField.getText().trim();
            String firstName = firstNameField.getText().trim();
            String middleName = middleNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String preferredName = preferredNameField.getText().trim();

            if (email.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
                showAlert("Please fill in all required fields.");
            } else {
                currentUser.setEmail(email);
                currentUser.setFirstName(firstName);
                currentUser.setMiddleName(middleName);
                currentUser.setLastName(lastName);
                currentUser.setPreferredName(preferredName);
                currentUser.setSetupComplete(true);
                showLoginPage();
            }
        });

        Scene scene = new Scene(grid, 400, 350);
        primaryStage.setScene(scene);
    }
    

    private void showRoleSelectionPage() {
        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));

        Label titleLabel = new Label("Select Your Role");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        vbox.getChildren().add(titleLabel);

        for (Role role : currentUser.getRoles()) {
            Button roleButton = new Button(role.toString());
            roleButton.setOnAction(e -> showHomePage(role));
            vbox.getChildren().add(roleButton);
        }

        Scene scene = new Scene(vbox, 300, 200);
        primaryStage.setScene(scene);
    }

    private void showHomePage(Role role) {
        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));

        Label welcomeLabel = new Label("Welcome, " + currentUser.getDisplayName() + "!");
        welcomeLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        vbox.getChildren().add(welcomeLabel);

        if (role == Role.ADMIN) {
            Button inviteButton = new Button("Invite New User");
            Button manageRolesButton = new Button("Manage Roles");
            Button deleteButton = new Button("Delete User");
            Button resetButton = new Button("Reset User Password");
            Button listUsersButton = new Button("List Users");
            Button logoutButton = new Button("Logout");

            inviteButton.setOnAction(e -> showInviteUserPage());
            manageRolesButton.setOnAction(e -> showManageRolesPage());
            deleteButton.setOnAction(e -> showDeleteUserPage());
            resetButton.setOnAction(e -> showResetUserPage());
            listUsersButton.setOnAction(e -> showListUsersPage());
            logoutButton.setOnAction(e -> showLoginPage());

            vbox.getChildren().addAll(inviteButton, manageRolesButton, deleteButton, resetButton, listUsersButton, logoutButton);
        } else {
            Label roleLabel = new Label(role.toString() + " Dashboard");
            Button logoutButton = new Button("Logout");
            logoutButton.setOnAction(e -> showLoginPage());
            vbox.getChildren().addAll(roleLabel, logoutButton);
        }

        Scene scene = new Scene(vbox, 300, 250);
        primaryStage.setScene(scene);
    }

    private void showInviteUserPage() {
        GridPane grid = createGrid();

        Label titleLabel = new Label("Invite New User");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        
        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField();
        
        Label rolesLabel = new Label("Roles:");
        CheckBox adminCheckBox = new CheckBox("Admin");
        CheckBox studentCheckBox = new CheckBox("Student");
        CheckBox instructorCheckBox = new CheckBox("Instructor");
        
        Button generateButton = new Button("Generate Invite Code");
        Button backButton = new Button("Back");

        grid.add(titleLabel, 0, 0, 2, 1);
        grid.add(emailLabel, 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(rolesLabel, 0, 2);
        grid.add(adminCheckBox, 1, 2);
        grid.add(studentCheckBox, 1, 3);
        grid.add(instructorCheckBox, 1, 4);
        grid.add(generateButton, 0, 5);
        grid.add(backButton, 1, 5);

        generateButton.setOnAction(e -> {
            String email = emailField.getText().trim();
            if (email.isEmpty()) {
                showAlert("Please enter an email address.");
                return;
            }
            
            Set<Role> selectedRoles = new HashSet<>();
            if (adminCheckBox.isSelected()) selectedRoles.add(Role.ADMIN);
            if (studentCheckBox.isSelected()) selectedRoles.add(Role.STUDENT);
            if (instructorCheckBox.isSelected()) selectedRoles.add(Role.INSTRUCTOR);
            
            if (selectedRoles.isEmpty()) {
                showAlert("Please select at least one role.");
                return;
            }
            
            String inviteCode = UUID.randomUUID().toString().substring(0, 8);
            inviteCodes.put(inviteCode, selectedRoles);
            
            // In a real application, you would send an email here
            showAlert("Invitation Code: " + inviteCode + "\nFor email: " + email + "\nRoles: " + selectedRoles);
        });

        backButton.setOnAction(e -> showHomePage(Role.ADMIN));

        Scene scene = new Scene(grid, 300, 250);
        primaryStage.setScene(scene);
    }
    

    private void showManageRolesPage() {
        GridPane grid = createGrid();

        Label titleLabel = new Label("Manage User Roles");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        Label roleLabel = new Label("Role:");
        ComboBox<Role> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll(Role.values());
        Button addRoleButton = new Button("Add Role");
        Button removeRoleButton = new Button("Remove Role");
        Button backButton = new Button("Back");

        grid.add(titleLabel, 0, 0, 2, 1);
        grid.add(usernameLabel, 0, 1);
        grid.add(usernameField, 1, 1);
        grid.add(roleLabel, 0, 2);
        grid.add(roleComboBox, 1, 2);
        grid.add(addRoleButton, 0, 3);
        grid.add(removeRoleButton, 1, 3);
        grid.add(backButton, 1, 4);

        addRoleButton.setOnAction(e -> {
            String username = usernameField.getText();
            Role selectedRole = roleComboBox.getValue();
            if (users.containsKey(username) && selectedRole != null) {
                User user = users.get(username);
                user.addRole(selectedRole);
                showAlert("Role added successfully");
            } else {
                showAlert("Invalid input");
            }
        });

        removeRoleButton.setOnAction(e -> {
            String username = usernameField.getText();
            Role selectedRole = roleComboBox.getValue();
            if (users.containsKey(username) && selectedRole != null) {
                User user = users.get(username);
                if (user.getRoles().size() > 1) {
                    user.removeRole(selectedRole);
                    showAlert("Role removed successfully");
                } else {
                    showAlert("Cannot remove the only role");
                }
            } else {
                showAlert("Invalid input");
            }
        });

        backButton.setOnAction(e -> showHomePage(Role.ADMIN));

        Scene scene = new Scene(grid, 300, 250);
        primaryStage.setScene(scene);
    }

    private void showDeleteUserPage() {
        GridPane grid = createGrid();

        Label titleLabel = new Label("Delete User");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        Button deleteButton = new Button("Delete User");
        Button backButton = new Button("Back");

        grid.add(titleLabel, 0, 0, 2, 1);
        grid.add(usernameLabel, 0, 1);
        grid.add(usernameField, 1, 1);
        grid.add(deleteButton, 1, 2);
        grid.add(backButton, 1, 3);

        deleteButton.setOnAction(e -> {
            String username = usernameField.getText();
            if (users.containsKey(username)) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this user?", ButtonType.YES, ButtonType.NO);
                alert.showAndWait();

                if (alert.getResult() == ButtonType.YES) {
                    users.remove(username);
                    showAlert("User deleted successfully");
                }
            } else {
                showAlert("User not found");
            }
        });

        backButton.setOnAction(e -> showHomePage(Role.ADMIN));

        Scene scene = new Scene(grid, 300, 200);
        primaryStage.setScene(scene);
    }


    private void showResetUserPage() {
        GridPane grid = createGrid();

        Label titleLabel = new Label("Reset User Password");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        Button resetButton = new Button("Reset Password");
        Button backButton = new Button("Back");

        grid.add(titleLabel, 0, 0, 2, 1);
        grid.add(usernameLabel, 0, 1);
        grid.add(usernameField, 1, 1);
        grid.add(resetButton, 1, 2);
        grid.add(backButton, 1, 3);

        resetButton.setOnAction(e -> {
            String username = usernameField.getText();
            if (users.containsKey(username)) {
                User user = users.get(username);
                String oneTimePassword = UUID.randomUUID().toString().substring(0, 8);
                Date expirationTime = new Date(System.currentTimeMillis() + 30 * 60 * 1000); // 30 minutes
                user.setOneTimePassword(oneTimePassword);
                user.setOneTimePasswordExpiration(expirationTime);
                user.setPasswordResetRequired(true);
                showAlert("Password reset. One-time password: " + oneTimePassword + "\nExpires at: " + expirationTime);
            } else {
                showAlert("User not found");
            }
        });

        backButton.setOnAction(e -> showHomePage(Role.ADMIN));

        Scene scene = new Scene(grid, 300, 200);
        primaryStage.setScene(scene);
    }
    

    private void showListUsersPage() {
        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));

        Label titleLabel = new Label("User Accounts");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        vbox.getChildren().add(titleLabel);

        // Create header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER);
        Label usernameHeader = new Label("Username");
        Label nameHeader = new Label("Name");
        Label rolesHeader = new Label("Role Codes");
        
        usernameHeader.setMinWidth(100);
        nameHeader.setMinWidth(150);
        rolesHeader.setMinWidth(100);
        
        usernameHeader.setStyle("-fx-font-weight: bold");
        nameHeader.setStyle("-fx-font-weight: bold");
        rolesHeader.setStyle("-fx-font-weight: bold");
        
        header.getChildren().addAll(usernameHeader, nameHeader, rolesHeader);
        vbox.getChildren().add(header);

        // Add a separator
        Separator separator = new Separator();
        separator.setMaxWidth(350);
        vbox.getChildren().add(separator);

        // Create a ScrollPane to contain the user list
        ScrollPane scrollPane = new ScrollPane();
        VBox userList = new VBox(5);
        userList.setAlignment(Pos.CENTER);
        
        for (User user : users.values()) {
            HBox userRow = new HBox(10);
            userRow.setAlignment(Pos.CENTER);
            
            Label usernameLabel = new Label(user.getUsername());
            Label nameLabel = new Label(user.getDisplayName());
            String rolesCodes = user.getRoles().stream()
                                    .map(role -> role.toString().substring(0, 1))
                                    .collect(Collectors.joining(", "));
            Label rolesLabel = new Label(rolesCodes);
            
            usernameLabel.setMinWidth(100);
            nameLabel.setMinWidth(150);
            rolesLabel.setMinWidth(100);
            
            userRow.getChildren().addAll(usernameLabel, nameLabel, rolesLabel);
            userList.getChildren().add(userRow);
        }
        
        scrollPane.setContent(userList);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(200);
        vbox.getChildren().add(scrollPane);

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> showHomePage(Role.ADMIN));
        vbox.getChildren().add(backButton);

        Scene scene = new Scene(vbox, 400, 350);
        primaryStage.setScene(scene);
    }
    

    private void showResetPasswordPage() {
        GridPane grid = createGrid();

        Label titleLabel = new Label("Reset Your Password");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        Label newPasswordLabel = new Label("New Password:");
        PasswordField newPasswordField = new PasswordField();
        Label confirmPasswordLabel = new Label("Confirm New Password:");
        PasswordField confirmPasswordField = new PasswordField();
        Button resetButton = new Button("Reset Password");

        grid.add(titleLabel, 0, 0, 2, 1);
        grid.add(newPasswordLabel, 0, 1);
        grid.add(newPasswordField, 1, 1);
        grid.add(confirmPasswordLabel, 0, 2);
        grid.add(confirmPasswordField, 1, 2);
        grid.add(resetButton, 1, 3);

        resetButton.setOnAction(e -> {
            String newPassword = newPasswordField.getText();
            String confirmPassword = confirmPasswordField.getText();
            if (newPassword.equals(confirmPassword)) {
                currentUser.setPassword(newPassword);
                currentUser.setOneTimePassword(null);
                currentUser.setOneTimePasswordExpiration(null);
                currentUser.setPasswordResetRequired(false);
                showAlert("Password reset successfully");
                showLoginPage();
            } else {
                showAlert("Passwords do not match");
            }
        });

        Scene scene = new Scene(grid, 300, 200);
        primaryStage.setScene(scene);
    }
    

    private GridPane createGrid() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        return grid;
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private class User {
        private String username;
        private String password;
        private Set<Role> roles;
        private String email;
        private String firstName;
        private String middleName;
        private String lastName;
        private String preferredName;
        private boolean setupComplete;
        private String oneTimePassword;
        private Date oneTimePasswordExpiration;
        private boolean passwordResetRequired;

        public User(String username, String password, Set<Role> roles) {
            this.username = username;
            this.password = password;
            this.roles = roles;
            this.setupComplete = false;
            this.oneTimePassword = null;
            this.oneTimePasswordExpiration = null;
            this.passwordResetRequired = false;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public Set<Role> getRoles() {
            return roles;
        }

        public void addRole(Role role) {
            roles.add(role);
        }

        public void removeRole(Role role) {
            roles.remove(role);
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getMiddleName() {
            return middleName;
        }

        public void setMiddleName(String middleName) {
            this.middleName = middleName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getPreferredName() {
            return preferredName;
        }

        public void setPreferredName(String preferredName) {
            this.preferredName = preferredName;
        }

        public boolean isSetupComplete() {
            return setupComplete;
        }

        public void setSetupComplete(boolean setupComplete) {
            this.setupComplete = setupComplete;
        }

        public String getOneTimePassword() {
            return oneTimePassword;
        }

        public void setOneTimePassword(String oneTimePassword) {
            this.oneTimePassword = oneTimePassword;
        }

        public Date getOneTimePasswordExpiration() {
            return oneTimePasswordExpiration;
        }

        public void setOneTimePasswordExpiration(Date oneTimePasswordExpiration) {
            this.oneTimePasswordExpiration = oneTimePasswordExpiration;
        }

        public boolean isPasswordResetRequired() {
            return passwordResetRequired;
        }

        public void setPasswordResetRequired(boolean passwordResetRequired) {
            this.passwordResetRequired = passwordResetRequired;
        }

        public String getDisplayName() {
            return preferredName != null && !preferredName.isEmpty() ? preferredName : firstName;
        }
    }
    

    private enum Role {
        ADMIN, STUDENT, INSTRUCTOR
    }
}