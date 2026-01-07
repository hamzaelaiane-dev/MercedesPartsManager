package com.mercedes.partsmanager.controller;

import com.mercedes.partsmanager.service.UserService;
import com.mercedes.partsmanager.util.SceneUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class SignupController {

    @FXML private TextField nomField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;

    private final UserService userService = new UserService();

    @FXML
    private void handleSignup() {
        String nom = nomField.getText() == null ? "" : nomField.getText().trim();
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();
        String confirm = confirmPasswordField.getText() == null ? "" : confirmPasswordField.getText();

        if (nom.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        if (!password.equals(confirm)) {
            showError("Les mots de passe ne correspondent pas.");
            return;
        }

        if (!email.contains("@")) {
            showError("Veuillez entrer un email valide.");
            return;
        }

        try {
            boolean success = userService.registerUser(nom, email, password);

            if (success) {
                // Redirection vers la page de login
                SceneUtil.switchScene(nomField, "/fxml/login-view.fxml", "Mercedes Parts Manager - Login");
            } else {
                showError("Erreur lors de l'inscription (email peut-être déjà utilisé).");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur technique lors de l'inscription.");
        }
    }

    @FXML
    private void goToLogin() {
        SceneUtil.switchScene(nomField, "/fxml/login-view.fxml", "Mercedes Parts Manager - Login");
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }
}