package com.mercedes.partsmanager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        // Charger le FXML
        Parent root = FXMLLoader.load(
                Objects.requireNonNull(
                        getClass().getResource("/fxml/login-view.fxml"),
                        "login-view.fxml introuvable"
                )
        );

        Scene scene = new Scene(root);

        // ✅ Lier le CSS ICI (et PAS dans le FXML)
        scene.getStylesheets().add(
                Objects.requireNonNull(
                        getClass().getResource("/css/style.css"),
                        "style.css introuvable"
                ).toExternalForm()
        );

        stage.setTitle("Mercedes Parts Manager");
        stage.setScene(scene);

        stage.setMaximized(true);
        stage.setResizable(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
