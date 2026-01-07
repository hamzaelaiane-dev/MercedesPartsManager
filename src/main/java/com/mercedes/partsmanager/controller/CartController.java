package com.mercedes.partsmanager.controller;

import com.mercedes.partsmanager.service.CartService;
import com.mercedes.partsmanager.util.SceneUtil;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Path;

public class CartController {

    @FXML private ListView<CartService.CartItem> cartList;
    @FXML private Label totalLabel;
    @FXML private Label msgLabel;

    private final CartService cartService = CartService.getInstance();

    @FXML
    private void initialize() {
        cartList.setItems(cartService.getItems());
        cartList.setCellFactory(lv -> new CartItemCardCell());


        updateTotal();
    }

    @FXML
    private void handleBack() {
        SceneUtil.switchScene(cartList, "/fxml/user-dashboard.fxml", "User Dashboard");
    }

    @FXML
    private void handleRemove() {
        var selected = cartList.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        cartService.remove(selected);
        cartList.refresh();
        updateTotal();
    }

    @FXML
    private void handleClear() {
        cartService.clear();
        msgLabel.setText("");
        updateTotal();
    }

    @FXML
    private void handleCheckout() {
        msgLabel.setText("");

        if (cartService.getItems().isEmpty()) {
            msgLabel.setText("Panier vide.");
            return;
        }

        Alert a = new Alert(Alert.AlertType.INFORMATION,
                "Achat validé ✅\nTotal = " + totalLabel.getText());
        a.showAndWait();

        cartService.clear();
        updateTotal();
    }

    private void updateTotal() {
        totalLabel.setText(formatMoney(cartService.getTotal()));
    }

    private String formatMoney(BigDecimal v) {
        if (v == null) v = BigDecimal.ZERO;
        return v.toPlainString() + " DH";
    }

    private Image loadImageOrPlaceholder(String imagePath) {
        try {
            if (imagePath != null && !imagePath.isBlank()) {
                String fileUrl = "file:" + Path.of(imagePath).toAbsolutePath().toString().replace("\\", "/");
                Image img = new Image(fileUrl, 90, 70, true, true, true);
                if (!img.isError()) return img;
            }
        } catch (Exception ignored) {}

        try (InputStream is = getClass().getResourceAsStream("/images/placeholder.png")) {
            if (is != null) return new Image(is);
        } catch (Exception ignored) {}

        return null;
    }

    // ✅ Cell futuriste + image
    private class CartItemCardCell extends ListCell<CartService.CartItem> {
        @Override
        protected void updateItem(CartService.CartItem item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setGraphic(null);
                return;
            }

            ImageView img = new ImageView(loadImageOrPlaceholder(item.getImagePath()));
            img.setFitWidth(90);
            img.setFitHeight(70);
            img.setPreserveRatio(true);

            StackPane imgWrap = new StackPane(img);
            imgWrap.setStyle("""
                -fx-background-color: rgba(255,255,255,0.08);
                -fx-background-radius: 14;
                -fx-padding: 8;
            """);

            Label name = new Label(item.getName());
            name.setStyle("-fx-text-fill:white; -fx-font-size:14px; -fx-font-weight:bold;");

            Label unit = new Label("Unité: " + formatMoney(item.getPrice()));
            unit.setStyle("-fx-text-fill: rgba(255,255,255,0.65); -fx-font-size:11px;");

            Label total = new Label("Total: " + formatMoney(item.total()));
            total.setStyle("-fx-text-fill:white; -fx-font-weight:bold;");

            Button minus = new Button("−");
            Button plus = new Button("+");

            String btnStyle = """
                -fx-background-color: rgba(255,255,255,0.12);
                -fx-text-fill: white;
                -fx-background-radius: 10;
                -fx-padding: 6 10;
                -fx-cursor: hand;
            """;
            minus.setStyle(btnStyle);
            plus.setStyle(btnStyle);

            Label qty = new Label("Qté: " + item.getQty());
            qty.setStyle("-fx-text-fill:white;");

            minus.setOnAction(e -> {
                if (item.getQty() > 1) item.setQty(item.getQty() - 1);
                cartList.refresh();
                updateTotal();
            });

            plus.setOnAction(e -> {
                item.setQty(item.getQty() + 1);
                cartList.refresh();
                updateTotal();
            });

            HBox qtyBox = new HBox(8, minus, qty, plus);
            VBox info = new VBox(4, name, unit, total, qtyBox);
            info.setPadding(new Insets(6, 0, 0, 0));

            HBox root = new HBox(12, imgWrap, info);
            root.setPadding(new Insets(12));
            root.setStyle("""
                -fx-background-color: rgba(255,255,255,0.06);
                -fx-background-radius: 16;
                -fx-border-radius: 16;
                -fx-border-color: rgba(255,255,255,0.10);
                -fx-border-width: 1;
            """);

            setGraphic(root);
        }
    }
}
