package com.mercedes.partsmanager.controller;

import com.mercedes.partsmanager.service.UserService;
import com.mercedes.partsmanager.util.SceneUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final UserService userService = new UserService();

    @FXML
    private void handleLogin() {
        String username = emailField.getText() == null ? "" : emailField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        try {
            String role = userService.loginRole(username, password);

            if (role == null) {
                showError("Identifiants incorrects ou compte désactivé.");
                return;
            }

            hideError();
            System.out.println("LOGIN OK => username=" + username + " role=" + role);

            if ("ADMIN".equalsIgnoreCase(role)) {
                // ADMIN
                SceneUtil.switchScene(emailField, "/fxml/admin-dashboard.fxml", "Admin Dashboard");
            } else {
                // USER ✅ passer username au controller
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user-dashboard.fxml"));
                Parent root = loader.load();

                UserDashboardController controller = loader.getController();
                controller.setConnectedUsername(username); // ✅ méthode non static

                Stage stage = (Stage) emailField.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("User Dashboard");
                stage.setMaximized(true);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors de la connexion (voir console).");
        }
    }

    @FXML
    private void goToSignup() {
        try {
            SceneUtil.switchScene(emailField, "/fxml/signup-view.fxml", "Mercedes Parts Manager - Sign Up");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Impossible d'ouvrir la page signup (voir console).");
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setText("");
    }
}
