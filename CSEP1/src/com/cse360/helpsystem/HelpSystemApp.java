package com.cse360.helpsystem;

// Below are the JavaFX imports needed to support the Graphical User Interface
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
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.SecureRandom;
import java.util.Base64;


/*******
 * <p> HelpSystemApp Class </p>
 * 
 * <p> Description: A JavaFX-based GUI implementation of a Help System for CSE360 </p>
 * 
 * <p> Copyright: CSE360 Team(Ashish Kumar, Kamaal Alag, Grace Mower, Ishaan Kurmi, Anshuman Yadav) © 2024 </p>
 * 
 * @author CSE360 Team(Ashish Kumar, Kamaal Alag, Grace Mower, Ishaan Kurmi, Anshuman Yadav)
 * 
 * @version 2.00 2024-10-30 Added new features including addition of article, deletion of article ect.
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
    
    private static Map<Long, Article> articles = new HashMap<>();
    private static Map<String, Set<Long>> articleGroups = new HashMap<>();
    
    private static long nextArticleId = 1;
    
    private Role currentRole;
    
    private SpecialAccessGroup specialAccessGroup = new SpecialAccessGroup("Special");
    
    
    
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
//            String passwordValidationResult = validatePassword(password);
//            if (passwordValidationResult != null) {
//                showAlert(passwordValidationResult);
//                return;
//            }
            
            User admin = new User(username, password, new HashSet<>(Arrays.asList(Role.INSTRUCTOR)));
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
            Button searchButton = new Button("Search");
            Button manageGroupButton = new Button("Manage Special Access Group");


            logoutButton.setOnAction(e -> showLoginPage());
            addArticleButton.setOnAction(e -> showspecialAddArticlePage(role));
            listArticleButton.setOnAction(e -> showListArticlesPage(role));
            updateArticleButton.setOnAction(e -> showUpdateArticlePage(role));
            deleteArticleButton.setOnAction(e -> showDeleteArticlePage(role));
            backupButton.setOnAction(e -> showBackupPage(role));
            restoreButton.setOnAction(e -> showRestorePage(role));
            searchButton.setOnAction(e -> showSearchPage(role));
            manageGroupButton.setOnAction(e -> showManagePermissionsPage(specialAccessGroup));

            vbox.getChildren().addAll(logoutButton, addArticleButton,listArticleButton, updateArticleButton, deleteArticleButton, backupButton, restoreButton, searchButton, manageGroupButton);
        }
        
        if (role == Role.STUDENT) {
            Label roleLabel = new Label(role.toString() + " Dashboard");
            
            Button quitButton = new Button("Quit");
            Button sendMessageButton = new Button("Send Message");
            Button searchButton = new Button("Search");
            Button listArticleButton = new Button("List Articles");
            Button logoutButton = new Button("Logout");
            Button restoreButton = new Button("Restore Articles");
            
            
            
            quitButton.setOnAction(e -> showLoginPage());
            sendMessageButton.setOnAction(e -> showSendMessagePage());
            searchButton.setOnAction(e -> showSearchPage(role));
            logoutButton.setOnAction(e -> showLoginPage());
            listArticleButton.setOnAction(e -> showListArticlesPage(role));
            restoreButton.setOnAction(e -> showRestorePage(role));
            
            vbox.getChildren().addAll(roleLabel, quitButton, sendMessageButton, searchButton, listArticleButton, restoreButton);
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
    private void addArticle(String title, String abs, String body, String keywords, String level, String groups) {
    	

    	
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
    
    private void specialaddArticle(String title, String abs, String body, String keywords, String level, String groups, boolean isSpecialAccess) {
        Set<String> keywordsArticle = new HashSet<>(Arrays.asList(keywords.split(",")));
        Set<String> groupsArticle = new HashSet<>(Arrays.asList(groups.split(",")));

        if (isSpecialAccess) {
            body = encodeBody(body); // Encode body
        }

        // Create the article object
        Article article = new Article(nextArticleId++, level, title, abs, body, groupsArticle, keywordsArticle);
        article.setSpecialAccess(isSpecialAccess); // Mark as special access if needed
        articles.put(article.getId(), article);

        for (String group : groupsArticle) {
            articleGroups.computeIfAbsent(group.trim(), k -> new HashSet<>()).add(article.getId());
        }

        if (isSpecialAccess) {
            // Add the article to the SpecialAccessGroup
            specialAccessGroup.getEncryptedArticles().add(article);
        }

        showAlert("Article added successfully with ID: " + article.getId());
        saveNextArticleId();
    }
    private void showspecialAddArticlePage(Role role) {
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

        CheckBox specialAccessCheckbox = new CheckBox("Special Access Group");
        grid.add(specialAccessCheckbox, 0, 8);

        submitButton.setOnAction(e -> {
            String title = titleField.getText().trim();
            String abs = abstractField.getText().trim();
            String body = bodyField.getText().trim();
            String keywords = keywordsField.getText().trim();
            String level = levelField.getText().trim();
            String groups = groupsField.getText().trim();
            boolean isSpecialAccess = specialAccessCheckbox.isSelected();

            specialaddArticle(title, abs, body, keywords, level, groups, isSpecialAccess);
        });

        backButton.setOnAction(e -> showHomePage(role));


        // Display the scene
        Scene scene = new Scene(grid, 600, 600);
        primaryStage.setScene(scene);
    }
    
    
// phase 3
    /**
     * Displays the page for sending messages in the help system.
     * Allows users to send either a generic message or a specific message.
     */
    
    private void showSendMessagePage() {
        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));

        Label messageLabel = new Label("Send a Message to the Help System");
        messageLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Generic Message Section
        Label genericMessageLabel = new Label("Generic Message:");
        Button genericMessageButton = new Button("I am confused about using this tool");
        genericMessageButton.setOnAction(e -> sendMessage("I am confused about using this tool"));

        // Specific Message Section
        Label specificMessageLabel = new Label("Specific Message:");
        TextArea specificMessageArea = new TextArea();
        specificMessageArea.setPromptText("Enter your specific message here...");
        specificMessageArea.setWrapText(true);

        Button sendSpecificMessageButton = new Button("Send Specific Message");
        sendSpecificMessageButton.setOnAction(e -> {
            String specificMessage = specificMessageArea.getText().trim();
            if (!specificMessage.isEmpty()) {
                sendMessage(specificMessage);
                showAlert("Your message has been sent successfully!");
            } else {
            	showAlert("Please enter a specific message before sending.");
            }
        });

        // Back Button
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> showHomePage(Role.STUDENT)); 

        vbox.getChildren().addAll(
            messageLabel,
            genericMessageLabel, genericMessageButton,
            specificMessageLabel, specificMessageArea, sendSpecificMessageButton,
            backButton
        );

        Scene scene = new Scene(vbox, 600, 400);
        primaryStage.setScene(scene);
    }
    
    /**
     * Sends a message to the help system.
     * Logs the message for review by system administrators.
     *
     * @param message The message content to be sent.
     */    
    private void sendMessage(String message) {
       
        System.out.println("Message sent: " + message);
    }
    
   
    /**
     * Executes a search for articles based on the provided query, content level, and group.
     * Displays the search results, including article counts by level, and allows users to view specific articles.
     *
     * @param query The search query string (keywords, title, or identifier).
     * @param contentLevel The content level to filter by (e.g., beginner, intermediate, expert, or All).
     * @param group The group to filter by (e.g., specific group name or All).
     * @param role The role of the current user (determines access permissions).
     */


    private void performSearch(String query, String contentLevel, String group, Role role) {
        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));

        Label titleLabel = new Label("Search Results");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        vbox.getChildren().add(titleLabel);

        // Display the current active group
        Label activeGroupLabel = new Label("Current Active Group: " + group);
        vbox.getChildren().add(activeGroupLabel);

        // Filter articles based on search criteria
        List<Article> filteredArticles = new ArrayList<>();
        for (Article article : articles.values()) {
            boolean matchesQuery = query.isEmpty() || 
                article.getTitle().toLowerCase().contains(query.toLowerCase()) || 
                article.getAbstractText().toLowerCase().contains(query.toLowerCase()) || 
                String.valueOf(article.getId()).equalsIgnoreCase(query);

            boolean matchesLevel = contentLevel.equals("All") || article.getLevel().equalsIgnoreCase(contentLevel);

            boolean matchesGroup = group.equals("All") || article.getGroups().contains(group);

            if (matchesQuery && matchesLevel && matchesGroup) {
                filteredArticles.add(article);
            }
        }

        // Count articles by content level
        Map<String, Long> levelCounts = filteredArticles.stream()
            .collect(Collectors.groupingBy(Article::getLevel, Collectors.counting()));

        // Display article count by level
        VBox levelCountBox = new VBox(5);
        levelCounts.forEach((level, count) -> {
            Label countLabel = new Label(level + ": " + count + " article(s)");
            levelCountBox.getChildren().add(countLabel);
        });
        vbox.getChildren().add(levelCountBox);

        // Short list of matching articles
        VBox articleList = new VBox(5);
        articleList.setAlignment(Pos.CENTER);

        int sequenceNumber = 1;
        Map<Integer, Article> sequenceToArticleMap = new HashMap<>();
        for (Article article : filteredArticles) {
            sequenceToArticleMap.put(sequenceNumber, article);
            Label articleLabel = new Label(sequenceNumber + ". " + article.getTitle() + "\nAbstract: " + article.getAbstractText());
            articleLabel.setWrapText(true);
            articleList.getChildren().add(articleLabel);
            sequenceNumber++;
        }

        // ScrollPane to display articles
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(articleList);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);
        vbox.getChildren().add(scrollPane);

        // Input for viewing article by sequence number
        HBox viewArticleBox = new HBox(10);
        viewArticleBox.setAlignment(Pos.CENTER);
        TextField sequenceInputField = new TextField();
        sequenceInputField.setPromptText("Enter sequence number to view article");
        Button viewArticleButton = new Button("View Article");
        viewArticleButton.setOnAction(e -> {
            try {
                int sequence = Integer.parseInt(sequenceInputField.getText().trim());
                if (sequenceToArticleMap.containsKey(sequence)) {
                    showArticleDetails(sequenceToArticleMap.get(sequence), role);
                } else {
                	showAlert("Invalid sequence number.");
                }
            } catch (NumberFormatException ex) {
            	showAlert("Please enter a valid sequence number.");
            }
        });
        viewArticleBox.getChildren().addAll(sequenceInputField, viewArticleButton);
        vbox.getChildren().add(viewArticleBox);

        // Add actions
        Button searchAgainButton = new Button("Search Again");
        searchAgainButton.setOnAction(e -> showSearchPage(role));
        vbox.getChildren().add(searchAgainButton);

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> showHomePage(role));
        vbox.getChildren().add(backButton);

        Scene scene = new Scene(vbox, 600, 600);
        primaryStage.setScene(scene);
    }

    
    /**
     * Displays the detailed view of a selected article, including title, abstract, body, and metadata.
     * Handles special access articles by checking if the user has the required permissions.
     *
     * @param article The article object containing the details to display.
     * @param role The role of the current user (used to navigate back to the appropriate page).
     */


    
    private void showArticleDetails(Article article, Role role) {
        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));

        Label titleLabel = new Label("Article Details");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        vbox.getChildren().add(titleLabel);

        Label idLabel = new Label("ID: " + article.getId());
        Label titleLabelRow = new Label("Title: " + article.getTitle());
        Label abstractLabel = new Label("Abstract: " + article.getAbstractText());

        Label bodyLabel;

        if (article.isSpecialAccess()) {
            // Check if the current user has permission to view the article
            if (specialAccessGroup.canView(currentUser.getUsername())) {
                bodyLabel = new Label("Body: " + decodeBody(article.getBody()));
            } else {
                bodyLabel = new Label("Body: Access Denied - You do not have permission to view this content.");
            }
        } else {
            bodyLabel = new Label("Body: " + article.getBody());
        }

        Label keywordLabel = new Label("Keywords: " + String.join(", ", article.getKeywords()));
        Label levelLabel = new Label("Level: " + article.getLevel());
        Label groupsLabel = new Label("Groups: " + String.join(", ", article.getGroups()));

        vbox.getChildren().addAll(idLabel, titleLabelRow, abstractLabel, bodyLabel, levelLabel, keywordLabel, groupsLabel);

        // Back to search results
        Button backButton = new Button("Back to Search Results");
        backButton.setOnAction(e -> performSearch("", "All", "All", role)); // Adjust parameters as needed
        vbox.getChildren().add(backButton);

        Scene scene = new Scene(vbox, 600, 400);
        primaryStage.setScene(scene);
    }

    /**
     * Displays the search page for users to search for articles.
     * Provides fields for entering a search query, selecting content level, and filtering by groups.
     * Allows users to initiate a search or navigate back to their dashboard.
     *
     * @param role The role of the current user (determines navigation and access controls).
     */

    

    private void showSearchPage(Role role) {
        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));

        Label titleLabel = new Label("Search Page");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label searchLabel = new Label("Search Articles:");
        TextField searchField = new TextField();
        searchField.setPromptText("Enter keywords, title, or identifier");

        ComboBox<String> contentLevelComboBox = new ComboBox<>();
        contentLevelComboBox.getItems().addAll("All", "beginner", "intermediate", "expert");
        contentLevelComboBox.setValue("All");

        ComboBox<String> groupComboBox = new ComboBox<>();
        groupComboBox.getItems().addAll("All", "360", "eclipse");
        groupComboBox.setValue("All");

        Button searchButton = new Button("Search");
        searchButton.setOnAction(e -> performSearch(searchField.getText(), contentLevelComboBox.getValue(), groupComboBox.getValue(), role));

        Button backButton = new Button("Back to Dashboard");
        backButton.setOnAction(e -> showHomePage(role));

        vbox.getChildren().addAll(titleLabel, searchLabel, searchField, contentLevelComboBox, groupComboBox, searchButton, backButton);

        Scene scene = new Scene(vbox, 600, 400);
        primaryStage.setScene(scene);
    }
    
    
    /**
     * Encodes the body content of an article using Base64 encoding.
     * Ensures secure storage or transmission of sensitive article content.
     *
     * @param body The plain text body of the article to encode.
     * @return The Base64-encoded string of the article body.
     */


    private String encodeBody(String body) {
        return Base64.getEncoder().encodeToString(body.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Decodes a Base64-encoded article body back into plain text.
     * Used to retrieve the readable content of encrypted articles for authorized users.
     *
     * @param encodedBody The Base64-encoded string representing the article body.
     * @return The decoded plain text body of the article.
     */


    private String decodeBody(String encodedBody) {
        return new String(Base64.getDecoder().decode(encodedBody), StandardCharsets.UTF_8);
    }
    
    
    /**
     * Displays the permissions management page for a specific special access group.
     * Allows admins to view and modify which users can access the group's articles.
     * Provides options to grant or revoke permissions for existing users and add new users to the group.
     *
     * @param group The SpecialAccessGroup object representing the group whose permissions are being managed.
     */

    private void showManagePermissionsPage(SpecialAccessGroup group) {
        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));

        Label titleLabel = new Label("Manage Permissions for Group: " + group.getGroupName());
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        vbox.getChildren().add(titleLabel);

        // Existing users with permissions
        VBox userList = new VBox(5);
        for (String username : group.getInstructorUsernames()) {
            HBox userRow = new HBox(10);
            userRow.setAlignment(Pos.CENTER);

            Label usernameLabel = new Label(username);
            CheckBox permissionCheckBox = new CheckBox("Can View Decoded Body");
            permissionCheckBox.setSelected(group.canView(username));

            permissionCheckBox.setOnAction(e -> {
                if (permissionCheckBox.isSelected()) {
                    group.grantPermission(username);
                } else {
                    group.revokePermission(username);
                }
            });

            userRow.getChildren().addAll(usernameLabel, permissionCheckBox);
            userList.getChildren().add(userRow);
        }

        ScrollPane scrollPane = new ScrollPane(userList);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);

        // Add new user section
        Label addUserLabel = new Label("Add User to Special Access Group:");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username");

        Button addUserButton = new Button("Add User");
        addUserButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            if (!username.isEmpty()) {
                addUserToSpecialAccessGroup(group, username);
            } else {
                showAlert("Please enter a valid username.");
            }
        });

        VBox addUserBox = new VBox(10, addUserLabel, usernameField, addUserButton);
        addUserBox.setAlignment(Pos.CENTER);

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> showHomePage(Role.INSTRUCTOR));

        vbox.getChildren().addAll(scrollPane, addUserBox, backButton);

        Scene scene = new Scene(vbox, 600, 400);
        primaryStage.setScene(scene);
    }
    
    /**
     * Adds a user to a special access group and grants them viewing permissions.
     * Checks if the user exists in the system before granting permissions.
     *
     * @param group The SpecialAccessGroup object to which the user will be added.
     * @param username The username of the user to add to the group.
     */

    private void addUserToSpecialAccessGroup(SpecialAccessGroup group, String username) {
        if (users.containsKey(username)) {
            group.grantPermission(username);
            showAlert("Permission granted to " + username + " for Special Access Group: " + group.getGroupName());
        } else {
            showAlert("User not found: " + username);
        }
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
    private class Article implements Serializable {
    	
    	private static final long serialVersionUID = 1L;
		private long id;
    	private String level;
    	private String title;
	    private String abstractText;
	    private String body;
	    private Set<String> groups;
	    private Set<String> keywords;
	    private boolean isSpecialAccess;

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
        
        public boolean isSpecialAccess() {
            return isSpecialAccess;
        }

        public void setSpecialAccess(boolean specialAccess) {
            isSpecialAccess = specialAccess;
        }

        @Override
        public String toString() {
            return "Article ID: " + id + "\nLevel: " + level + "\nTitle: " + title + "\nAbstract: " + abstractText + "\nBody: " + body;
        }
    }
/**
* Represents an Special Access Group in the system.
*/ 
    private class SpecialAccessGroup {
        private String groupName;
        private Set<String> adminUsernames;
        private Set<String> instructorUsernames;
        private Set<String> studentUsernames;
        private Map<String, Boolean> viewPermissions; // New: Maps username to view permissions
        private List<Article> encryptedArticles;

        public SpecialAccessGroup(String groupName) {
            this.groupName = groupName;
            this.adminUsernames = new HashSet<>();
            this.instructorUsernames = new HashSet<>();
            this.studentUsernames = new HashSet<>();
            this.viewPermissions = new HashMap<>(); // Initialize the map
            this.encryptedArticles = new ArrayList<>();
        }

        public String getGroupName() {
            return groupName;
        }

        public void setGroupName(String groupName) {
            this.groupName = groupName;
        }

        public Set<String> getAdminUsernames() {
            return adminUsernames;
        }

        public void setAdminUsernames(Set<String> adminUsernames) {
            this.adminUsernames = adminUsernames;
        }

        public Set<String> getInstructorUsernames() {
            return instructorUsernames;
        }

        public void setInstructorUsernames(Set<String> instructorUsernames) {
            this.instructorUsernames = instructorUsernames;
        }

        public Set<String> getStudentUsernames() {
            return studentUsernames;
        }

        public void setStudentUsernames(Set<String> studentUsernames) {
            this.studentUsernames = studentUsernames;
        }

        public List<Article> getEncryptedArticles() {
            return encryptedArticles;
        }

        public void setEncryptedArticles(List<Article> encryptedArticles) {
            this.encryptedArticles = encryptedArticles;
        }
        
        public void grantPermission(String username) {
            viewPermissions.put(username, true);
        }

        public void revokePermission(String username) {
            viewPermissions.put(username, false);
        }

        public boolean canView(String username) {
            return viewPermissions.getOrDefault(username, false);
        }
    }
    
}
