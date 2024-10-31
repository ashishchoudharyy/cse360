package com.cse360.helpsystem;

//JavaFX imports needed to support the Graphical User Interface
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

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.io.*;
import java.util.HashMap;
import java.util.Map;


/*******
 * <p> HelpSystemApp Class </p>
 * 
 * <p> Description: A JavaFX-based GUI implementation of a Help System for CSE360 </p>
 * 
 * <p> Copyright: CSE360 Team(Ashish Kumar, Kamaal Alag, Grace Mower, Ishaan Kurmi, Anshuman Yadav) © 2024 </p>
 * 
 * @author CSE360 Team(Ashish Kumar, Kamaal Alag, Grace Mower, Ishaan Kurmi, Anshuman Yadav)
 * 
 * @version 2.00 2024-10-30 Added new features including addition of article, deletion of article etc.
 * 
 */

public class HelpSystemApp extends Application {
	/** Map to store user accounts */
    private Map<String, User> users = new HashMap<>();
    
    /** Reference to the current logged-in user */
    private User currentUser;
    
    /** The primary stage for the application */
    private Stage primaryStage;
    
    /** Map to store invitation codes and associated roles */
    private Map<String, Set<Role>> inviteCodes = new HashMap<>();
    
    static Map<Long, Article> articles = new HashMap<>();
    private static Map<String, Set<Long>> articleGroups = new HashMap<>();
    
    static long nextArticleId = 1;
    
    private Role currentRole;
    
    
    
    
    
    /**
     * This is the main entry point for this application.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
    	restoreNextArticleId();
        launch(args);
    }
    
    /**
     * This method is called when the application should start.
     * It sets up the primary stage and shows the login page.
     * 
     * @param primaryStage The primary stage for this application
     */
    

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("CSE360 Help System");
        showLoginPage();
    }
    
    /**
     * Displays the login page of the application.
     */
    
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

        Scene scene = new Scene(grid, 600, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
  

    /**
     * Handles the login process for users.
     * 
     * @param username The entered username
     * @param password The entered password
     */
    
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
        
    /**
     * Validates the password against the specified criteria.
     * 
     * @param password The password to validate
     * @return A string containing missing criteria, or null if valid
     */
    
    
    public static String validatePassword(String password) {
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
    
    /**
     * Displays the registration page for new users.
     * 
     * @param inviteCode The invitation code used for registration
     */
    
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

        Scene scene = new Scene(grid, 600, 500);
        primaryStage.setScene(scene);
    }

    /**
     * Registers a new user in the system.
     * 
     * @param user-name The chosen user-name
     * @param password The chosen password
     * @param inviteCode The invitation code used
     */

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

    /**
     * Displays the account setup page for new users.
     */
    
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
    
    /**
     * Displays the role selection page for users with multiple roles.
     */

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
    
    /**
     * Displays the home page based on the user's role.
     * 
     * @param role The role of the current user
     */

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
            Button addArticleButton = new Button("Add Article");
            Button updateArticleButton = new Button("Update Article");
            Button deleteArticleButton = new Button("Delete Article");
            Button listArticleButton = new Button("List Articles");
            Button backupButton = new Button("Backup Articles");
            Button restoreButton = new Button("Restore Articles");

            inviteButton.setOnAction(e -> showInviteUserPage());
            manageRolesButton.setOnAction(e -> showManageRolesPage());
            deleteButton.setOnAction(e -> showDeleteUserPage());
            resetButton.setOnAction(e -> showResetUserPage());
            listUsersButton.setOnAction(e -> showListUsersPage());
            logoutButton.setOnAction(e -> showLoginPage());
            addArticleButton.setOnAction(e -> showAddArticlePage(role));
            listArticleButton.setOnAction(e -> showListArticlesPage(role));
            updateArticleButton.setOnAction(e -> showUpdateArticlePage(role));
            deleteArticleButton.setOnAction(e -> showDeleteArticlePage(role));
            backupButton.setOnAction(e -> showBackupPage(role));
            restoreButton.setOnAction(e -> showRestorePage(role));

            vbox.getChildren().addAll(inviteButton, manageRolesButton, deleteButton, resetButton, listUsersButton, logoutButton, addArticleButton,listArticleButton, updateArticleButton, deleteArticleButton, backupButton, restoreButton);
        } 
        
        if (role == Role.INSTRUCTOR) {
            
            Button logoutButton = new Button("Logout");
            Button addArticleButton = new Button("Add Article");
            Button updateArticleButton = new Button("Update Article");
            Button deleteArticleButton = new Button("Delete Article");
            Button listArticleButton = new Button("List Articles");
            Button backupButton = new Button("Backup Articles");
            Button restoreButton = new Button("Restore Articles");


            logoutButton.setOnAction(e -> showLoginPage());
            addArticleButton.setOnAction(e -> showAddArticlePage(role));
            listArticleButton.setOnAction(e -> showListArticlesPage(role));
            updateArticleButton.setOnAction(e -> showUpdateArticlePage(role));
            deleteArticleButton.setOnAction(e -> showDeleteArticlePage(role));
            backupButton.setOnAction(e -> showBackupPage(role));
            restoreButton.setOnAction(e -> showRestorePage(role));

            vbox.getChildren().addAll(logoutButton, addArticleButton,listArticleButton, updateArticleButton, deleteArticleButton, backupButton, restoreButton);
        }
        
        if (role == Role.STUDENT) {
            Label roleLabel = new Label(role.toString() + " Dashboard");
            Button logoutButton = new Button("Logout");
            logoutButton.setOnAction(e -> showLoginPage());
            vbox.getChildren().addAll(roleLabel, logoutButton);
        }

        Scene scene = new Scene(vbox, 600, 500);
        primaryStage.setScene(scene);
    }
    
    /**
     * Displays the page for inviting new users (Admin function).
     */

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
    
    /**
     * Displays the page for managing user roles (Admin function).
     */

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
    

    /**
     * Displays the page for deleting users (Admin function).
     */

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
    

    /**
     * Displays the page for resetting user passwords (Admin function).
     */

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
    
    /**
     * Displays a list of all users in the system (Admin function).
     */

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
    
    /**
     * Displays the page for users to reset their own password.
     */

    
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

            // First, validate the new password
            String passwordValidationResult = validatePassword(newPassword);
            if (passwordValidationResult != null) {
                showAlert(passwordValidationResult);
                return;
            }

            // Then, check if passwords match
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
    
    /**
     * Creates and returns a standard GridPane for consistent UI layout.
     * 
     * @return A configured GridPane
     */

    private GridPane createGrid() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        return grid;
    }

    /**
     * Displays an alert dialog with the given message.
     * 
     * @param message The message to display
     */
    
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
/**
 * Phase 2
 */

/**
 * Add functionality - Displays the GUI for adding a new article and processes the input to add a new article.
 */

/**
 * Shows the page for adding a new article, setting up the GUI components necessary for user input.
 *
 * @param role The role of the user who is currently logged in.
 */

    
    private void showAddArticlePage(Role role) {
        GridPane grid = createGrid();

        Label titleLabel = new Label("Add New Article");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        
        // Fields for article details
        TextField levelField = new TextField();
        TextField titleField = new TextField();
        TextField abstractField = new TextField();
        TextArea bodyField = new TextArea();
        TextField keywordsField = new TextField();
        TextField groupsField = new TextField();
        Button submitButton = new Button("Submit");
        Button backButton = new Button("Back");
        

        // Layout the form
        grid.add(titleLabel, 0, 0, 2, 1);
        grid.add(new Label("Title:"), 0, 1);
        grid.add(titleField, 1, 1);
        grid.add(new Label("Abstract:"), 0, 2);
        grid.add(abstractField, 1, 2);
        grid.add(new Label("Body:"), 0, 3);
        grid.add(bodyField, 1, 3);
        grid.add(new Label("Keywords (comma-separated):"), 0, 4);
        grid.add(keywordsField, 1, 4);
        grid.add(new Label("Level:"), 0, 5);
        grid.add(levelField, 1, 5);
        grid.add(new Label("Groups (comma-separated):"), 0, 6);
        grid.add(groupsField, 1, 6);
        grid.add(submitButton, 0, 7);
        grid.add(backButton, 1, 7);

        // Event handlers for buttons
        submitButton.setOnAction(e -> {
        	
            String title = titleField.getText().trim();
            String abs = abstractField.getText().trim();
            String body = bodyField.getText().trim();
            String keywords = keywordsField.getText().trim();
            String level = levelField.getText().trim();
            String groups = groupsField.getText().trim();
            addArticle(title, abs, body, keywords, level, groups);
            
        });

        backButton.setOnAction(e -> showHomePage(role));


        // Display the scene
        Scene scene = new Scene(grid, 600, 600);
        primaryStage.setScene(scene);
    }
    
    /**
     * Adds a new article to the help system.
     *
     * @param title The title of the article
     * @param description The description of the article
     * @param body The body of the article
     * @param keywords A list of keywords
     * @param level The difficulty level
     * @param groups A set of groups to which the article belongs
     */
    void addArticle(String title, String abs, String body, String keywords, String level, String groups) {
    	

    	
    	Set<String> keywords_article = new HashSet<>(Arrays.asList(keywords.split(",")));
    	Set<String> groups_article = new HashSet<>(Arrays.asList(groups.split(",")));
    	
    	
    	Article article = new Article(nextArticleId++, level, title, abs, body, groups_article, keywords_article);
    	articles.put(article.getId(), article);

    	for (String group : groups_article) {
            articleGroups.computeIfAbsent(group.trim(), k -> new HashSet<>()).add(article.getId());
        }

        showAlert("Article added successfully with ID: " + article.getId());
        saveNextArticleId();
    }

/**
 * View functionality - Displays a list of all articles with options to interact further (delete, update, etc.).
 */

/**
 * Shows the page listing all articles in the system.
 *
 * @param role The role of the user who is currently logged in.
 */

    private void showListArticlesPage(Role role) {
        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));

        Label titleLabel = new Label("List of Articles");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        vbox.getChildren().add(titleLabel);

        // Create a header for the article list
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER);
        Label idHeader = new Label("ID");
        Label titleHeader = new Label("Title");
        Label descriptionHeader = new Label("Abstract");
        Label levelHeader = new Label("Level");
        Label groupsHeader = new Label("Groups");

        idHeader.setMinWidth(50);
        titleHeader.setMinWidth(150);
        descriptionHeader.setMinWidth(200);
        levelHeader.setMinWidth(100);
        groupsHeader.setMinWidth(150);

        idHeader.setStyle("-fx-font-weight: bold");
        titleHeader.setStyle("-fx-font-weight: bold");
        descriptionHeader.setStyle("-fx-font-weight: bold");
        levelHeader.setStyle("-fx-font-weight: bold");
        groupsHeader.setStyle("-fx-font-weight: bold");

        header.getChildren().addAll(idHeader, titleHeader, descriptionHeader, levelHeader, groupsHeader);
        vbox.getChildren().add(header);

        // Add a separator
        Separator separator = new Separator();
        separator.setMaxWidth(650);
        vbox.getChildren().add(separator);

        // Create a ScrollPane to contain the article list
        ScrollPane scrollPane = new ScrollPane();
        VBox articleList = new VBox(5);
        articleList.setAlignment(Pos.CENTER);

        // Populate the article list
        for (Article article : articles.values()) {
            HBox articleRow = new HBox(10);
            articleRow.setAlignment(Pos.CENTER);

            Label idLabel = new Label(String.valueOf(article.getId()));
            Label titleLabelRow = new Label(article.getTitle());
            Label descriptionLabel = new Label(article.getAbstractText());
            Label levelLabel = new Label(article.getLevel());
            String groups = String.join(", ", article.getGroups());
            Label groupsLabel = new Label(groups);

            idLabel.setMinWidth(50);
            titleLabelRow.setMinWidth(150);
            descriptionLabel.setMinWidth(200);
            levelLabel.setMinWidth(100);
            groupsLabel.setMinWidth(150);

            articleRow.getChildren().addAll(idLabel, titleLabelRow, descriptionLabel, levelLabel, groupsLabel);
            articleList.getChildren().add(articleRow);
        }

        scrollPane.setContent(articleList);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);
        vbox.getChildren().add(scrollPane);

        // Add a Back button
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> showHomePage(role));
        vbox.getChildren().add(backButton);

        Scene scene = new Scene(vbox, 600, 600);
        primaryStage.setScene(scene);
    }
   
/**
 * Deletes an article from the system using its ID.
 *
 * @param id The ID of the article to delete
 */

    private void deleteArticle(long id) {
        if (articles.remove(id) != null) {
            for (Set<Long> groupArticles : articleGroups.values()) {
                groupArticles.remove(id);
            }
            showAlert("Article deleted successfully");
        } else {
            showAlert("Article not found");
        }
    }

/**
 * Delete functionality - Provides GUI to delete an article and processes the deletion.
 */

/**
 * Shows the page for deleting an article, setting up the necessary GUI components for user input.
 *
 * @param role The role of the user who is currently logged in.
 */

    private void showDeleteArticlePage(Role role) {
        GridPane grid = createGrid();

        Label titleLabel = new Label("Delete Article");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        TextField idField = new TextField();
        Button deleteButton = new Button("Delete");
        Button backButton = new Button("Back");

        grid.add(titleLabel, 0, 0, 2, 1);
        grid.add(new Label("Article ID:"), 0, 1);
        grid.add(idField, 1, 1);
        grid.add(deleteButton, 0, 2);
        grid.add(backButton, 1, 2);

        deleteButton.setOnAction(e -> {
            try {
                long id = Long.parseLong(idField.getText().trim());
                deleteArticle(id);
            } catch (NumberFormatException ex) {
                showAlert("Invalid article ID.");
            }
        });

        backButton.setOnAction(e -> showHomePage(role));

        Scene scene = new Scene(grid, 600, 600);
        primaryStage.setScene(scene);
    }

    
 /**
 * Backs up articles to a file. Can filter by groups if specified.
 *
 * @param fileName The name of the file to back up to
 * @param groupName Optional group names, comma-separated, to filter the articles for backup
 */
    
    private void backupArticles(String fileName, String groupName) {
    	Set<String> groupsToBackup = new HashSet<>();
        if (!groupName.isEmpty()) {
            groupsToBackup.addAll(Arrays.asList(groupName.split(",")));
        }
        
        List<Article> articlesToBackup = articles.values().stream()
                .filter(article -> article.getGroups().stream().anyMatch(groupsToBackup::contains) || groupsToBackup.isEmpty())
                .collect(Collectors.toList());
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            for (Article article : articlesToBackup) {
                writer.println(article.getId() + "|" + article.getLevel() + "|" + article.getTitle() + "|" + article.getAbstractText() + "|" + article.getBody() + "|" +
                               String.join(",", article.getGroups()) + "|" + String.join(",", article.getKeywords()));
            }
            showAlert("Backup completed successfully");
        } catch (IOException e) {
        	showAlert("Backup failed: " + e.getMessage());
        }
    }    	
  
/**
 * Backup functionality - Provides GUI to backup articles and processes the backup to a file.
 */

/**
 * Shows the page for backing up articles, setting up the necessary GUI components for user input.
 *
 * @param role The role of the user who is currently logged in.
 */

    private void showBackupPage(Role role) {
        GridPane grid = createGrid();

        Label titleLabel = new Label("Backup Articles");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        TextField fileNameField = new TextField();
        TextField groupsField = new TextField();
        Button backupButton = new Button("Backup");
        Button backButton = new Button("Back");

        grid.add(titleLabel, 0, 0, 2, 1);
        grid.add(new Label("File Name:"), 0, 1);
        grid.add(fileNameField, 1, 1);
        grid.add(new Label("Groups (comma-separated):"), 0, 2);
        grid.add(groupsField, 1, 2);
        grid.add(backupButton, 0, 3);
        grid.add(backButton, 1, 3);

        backupButton.setOnAction(e -> {
            String fileName = fileNameField.getText().trim();
            String groupName = groupsField.getText().trim();
            
            backupArticles(fileName, groupName);
        });

        backButton.setOnAction(e -> showHomePage(role));

        Scene scene = new Scene(grid, 600, 600);
        primaryStage.setScene(scene);
    }

/**
 * Restore functionality - Provides GUI to restore articles from a file and processes the restoration.
 */

/**
 * Shows the page for restoring articles from a file, setting up the necessary GUI components for user input.
 *
 * @param role The role of the user who is currently logged in.
 */
    
    private void showRestorePage(Role role) {
        GridPane grid = createGrid();

        Label titleLabel = new Label("Restore Articles");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        TextField fileNameField = new TextField();
        CheckBox mergeCheckBox = new CheckBox("Merge with existing");
        Button restoreButton = new Button("Restore");
        Button backButton = new Button("Back");

        grid.add(titleLabel, 0, 0, 2, 1);
        grid.add(new Label("File Name:"), 0, 1);
        grid.add(fileNameField, 1, 1);
        grid.add(mergeCheckBox, 1, 2);
        grid.add(restoreButton, 0, 3);
        grid.add(backButton, 1, 3);

        restoreButton.setOnAction(e -> {
            String fileName = fileNameField.getText().trim();
            boolean merge = mergeCheckBox.isSelected();
            restoreArticles(fileName, merge);
        });

        backButton.setOnAction(e -> showHomePage(role));

        Scene scene = new Scene(grid, 600, 600);
        primaryStage.setScene(scene);
    }

/**
 * Restores articles from a specified file, optionally merging them with existing articles.
 *
 * @param fileName The name of the file from which to restore articles
 * @param merge Whether to merge restored articles with existing ones
 */

    private void restoreArticles(String fileName, boolean merge) {
    	
    	
    	try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            if (!merge) {
                articles.clear();
                articleGroups.clear();
            }
    
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                long id = Long.parseLong(parts[0]);
                String level = parts[1];
                String title = parts[2];
                String abstractText = parts[3];
                String body = parts[4];
                Set<String> groups = new HashSet<>(Arrays.asList(parts[5].split(",")));
                Set<String> keywords = new HashSet<>(Arrays.asList(parts[6].split(",")));
    
                Article article = new Article(id, level, title, abstractText, body, groups, keywords);
                articles.put(id, article);
                for (String group : groups) {
                    articleGroups.computeIfAbsent(group.trim(), k -> new HashSet<>()).add(id);
                }
    
                // Update nextArticleId to prevent conflicts
                nextArticleId = Math.max(nextArticleId, id + 1);
            }
            showAlert("Backup completed successfully");
            System.out.println(articleGroups);
            System.out.println(articles);
        } catch (IOException e) {
        	showAlert("Backup failed: " + e.getMessage());
        }
    	
    }
    
/**
 * Utility methods
 */

/**
 * Saves the next available article ID to a file.
 */
    
    private static void saveNextArticleId() {
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream("nextArticleId.dat"))) {
            out.writeLong(nextArticleId);
        } catch (IOException e) {
            System.out.println("Failed to save article ID state: " + e.getMessage());
        }
    }

/**
 * Restores the next available article ID from a file.
 */

    private static void restoreNextArticleId() {
        try (DataInputStream in = new DataInputStream(new FileInputStream("nextArticleId.dat"))) {
            nextArticleId = in.readLong();
        } catch (FileNotFoundException e) {
            System.out.println("Starting new session with ID 1.");
        } catch (IOException e) {
            System.out.println("Error reading ID file: " + e.getMessage());
            nextArticleId = 1; // Reset to 1 in case of error reading file
        }
    }

/**
 * Updates an article in the system with new details provided by the user.
 *
 * @param articleId The ID of the article to update.
 * @param title The new title of the article, if provided.
 * @param abs The new abstract of the article, if provided.
 * @param body The new body content of the article, if provided.
 * @param keywords Comma-separated list of new keywords, if provided.
 * @param level The new level of difficulty of the article, if provided.
 * @param groups Comma-separated list of new groups the article belongs to, if provided.
 */
    
    public void updateArticle(long articleId, String title, String abs, String body, String keywords, String level, String groups) {
    	
    	Set<String> keywords_article = new HashSet<>(Arrays.asList(keywords.split(",")));
    	Set<String> groups_article = new HashSet<>(Arrays.asList(groups.split(",")));
    	
        // Retrieve the article from the map by its ID
        Article article = articles.get(articleId);

        // If the article does not exist, print an error message and exit the method
        if (article == null) {
            System.out.println("Article with ID " + articleId + " not found.");
            return;
        }


        // Update the fields of the article if new values are provided
        if (level != null) {
            article.setLevel(level);
        }
        if (title != null) {
            article.setTitle(title);
        }
        if (abs != null) {
            article.setAbstractText(abs);
        }
        if (body != null) {
            article.setBody(body);
        }
        if (groups != null) {
            article.setGroups(groups_article);
        }
        if (keywords != null) {
            article.setKeywords(keywords_article);
        }


        // Update the article in the map and display a success message
        articles.put(articleId, article);
        showAlert("Article with ID " + articleId + " updated successfully.");
    }


/**
 * Displays the page for updating an article, setting up the necessary GUI components for user input.
 *
 * @param role The role of the user who is currently logged in.
 */
    
    private void showUpdateArticlePage(Role role) {
        GridPane grid = createGrid();

        Label titleLabel = new Label("Update Article");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        TextField idField = new TextField();
        TextField titleField = new TextField();
        TextField descriptionField = new TextField();
        TextArea bodyField = new TextArea();
        TextField keywordsField = new TextField();
        TextField levelField = new TextField();
        TextField groupsField = new TextField();
        Button updateButton = new Button("Update");
        Button backButton = new Button("Back");


        // Layout the form for updating an article
        grid.add(titleLabel, 0, 0, 2, 1);
        grid.add(new Label("Article ID:"), 0, 1);
        grid.add(idField, 1, 1);
        grid.add(new Label("Title:"), 0, 2);
        grid.add(titleField, 1, 2);
        grid.add(new Label("Description:"), 0, 3);
        grid.add(descriptionField, 1, 3);
        grid.add(new Label("Body:"), 0, 4);
        grid.add(bodyField, 1, 4);
        grid.add(new Label("Keywords (comma-separated):"), 0, 5);
        grid.add(keywordsField, 1, 5);
        grid.add(new Label("Level:"), 0, 6);
        grid.add(levelField, 1, 6);
        grid.add(new Label("Groups (comma-separated):"), 0, 7);
        grid.add(groupsField, 1, 7);
        grid.add(updateButton, 0, 8);
        grid.add(backButton, 1, 8);

        // Define actions for the update button
        updateButton.setOnAction(e -> {
            try {
                long id = Long.parseLong(idField.getText().trim());
                String title = titleField.getText().trim();
                String description = descriptionField.getText().trim();
                String body = bodyField.getText().trim();
                String keywords = keywordsField.getText().trim();
                String level = levelField.getText().trim();
                String groups = groupsField.getText().trim();
                updateArticle(id, title, description, body, keywords, level, groups);
            } catch (NumberFormatException ex) {
                showAlert("Invalid article ID.");
            }
        });

        // Define action for the back button
        backButton.setOnAction(e -> showHomePage(role));

        // Display the scene containing the update form
        Scene scene = new Scene(grid, 1000, 1000);
        primaryStage.setScene(scene);
    }
    
/**
 * Classes and Enums
 */

/**
 * Represents a user in the system.
 */
    
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

        // Getter and setter methods
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
    
/**
* Enum representing the possible roles in the system.
*/

    private enum Role {
        ADMIN, STUDENT, INSTRUCTOR
    }
    
/**
 * Represents an article in the system.
 */
    public class Article implements Serializable {
    	
    	private static final long serialVersionUID = 1L;
		private long id;
    	private String level;
    	private String title;
	    private String abstractText;
	    private String body;
	    private Set<String> groups;
	    private Set<String> keywords;

        public Article(long id, String level, String title, String abstractText, String body, Set<String> groups, Set<String> keywords) {
        	this.id = id;
            this.level = level;
            this.title = title;
            this.abstractText = abstractText;
            this.body = body;
            this.groups = groups;
            this.keywords = keywords;
        }

     // Getter methods
        public long getId() {
            return id;
        }

        public String getLevel() {
            return level;
        }

        public String getTitle() {
            return title;
        }

        public String getAbstractText() {
            return abstractText;
        }

        public String getBody() {
            return body;
        }

        public Set<String> getGroups() {
            return groups;
        }

        public Set<String> getKeywords() {
            return keywords;
        }

        // Setter methods
        public void setId(long id) {
            this.id = id;
        }

        public void setLevel(String level) {
            this.level = level;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setAbstractText(String abstractText) {
            this.abstractText = abstractText;
        }

        public void setBody(String body) {
            this.body = body;
        }

        public void setGroups(Set<String> groups) {
            this.groups = groups;
        }

        public void setKeywords(Set<String> keywords) {
            this.keywords = keywords;
        }

        @Override
        public String toString() {
            return "Article ID: " + id + "\nLevel: " + level + "\nTitle: " + title + "\nAbstract: " + abstractText + "\nBody: " + body;
        }
    }
    
}
