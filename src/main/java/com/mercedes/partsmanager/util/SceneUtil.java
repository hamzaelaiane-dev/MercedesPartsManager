package com.mercedes.partsmanager.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class SceneUtil {

    public static void switchScene(Node anyNodeInCurrentScene, String fxmlPath, String title) {
        try {
            URL url = SceneUtil.class.getResource(fxmlPath);
            if (url == null) {
                throw new IllegalStateException("FXML introuvable: " + fxmlPath
                        + " (vérifie src/main/resources" + fxmlPath + ")");
            }

            Parent root = FXMLLoader.load(url);

            Stage stage = (Stage) anyNodeInCurrentScene.getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(new Scene(root));

            // ✅ plein écran (maximisé)
            stage.setMaximized(true);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur switchScene vers " + fxmlPath, e);
        }
    }
}
