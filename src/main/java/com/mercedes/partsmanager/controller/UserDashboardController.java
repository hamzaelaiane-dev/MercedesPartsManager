package com.mercedes.partsmanager.controller;

import com.mercedes.partsmanager.model.Category;
import com.mercedes.partsmanager.model.Product;
import com.mercedes.partsmanager.service.CartService;
import com.mercedes.partsmanager.service.UserService;
import com.mercedes.partsmanager.util.DB;
import com.mercedes.partsmanager.util.SceneUtil;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDashboardController {

    @FXML private Label helloLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<Category> categoryCombo;
    @FXML private Label msgLabel;

    @FXML private ListView<Product> productsList;

    private final ObservableList<Product> products = FXCollections.observableArrayList();
    private final ObservableList<Category> categories = FXCollections.observableArrayList();

    private final CartService cartService = CartService.getInstance();
    private final UserService userService = new UserService();

    private String connectedUsername = null;

    @FXML
    private void initialize() {

        // ✅ Products list
        productsList.setItems(products);
        productsList.setCellFactory(list -> new ProductCardCell());

        // ✅ Categories combo
        categoryCombo.setItems(categories);
        categoryCombo.setCellFactory(cb -> new ListCell<>() {
            @Override protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                // CORRECTION : Utiliser getNom() au lieu de getName()
                setText(empty || item == null ? "" : item.getNom());
            }
        });
        categoryCombo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                // CORRECTION : Utiliser getNom() au lieu de getName()
                setText(empty || item == null ? "" : item.getNom());
            }
        });

        handleRefresh();
    }

    public void setConnectedUsername(String username) {
        this.connectedUsername = username;
        helloLabel.setText((username != null && !username.isBlank())
                ? "Bonjour " + username + " 👋"
                : "Bonjour 👋");
    }

    @FXML
    private void handleRefresh() {
        msgLabel.setText("");
        loadCategories();
        loadProducts(null, null);
    }

    @FXML
    private void handleSearch() {
        msgLabel.setText("");
        String q = safe(searchField.getText());
        Category cat = categoryCombo.getValue();
        Integer catId = (cat == null) ? null : cat.getId();

        loadProducts(q.isBlank() ? null : q, catId);
    }

    private void loadCategories() {
        categories.clear();
        // CORRECTION : Vérifier le nom de la colonne (nom ou name)
        String sql = "SELECT id, nom FROM categories ORDER BY nom ASC";

        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            // CORRECTION : Utiliser getNom() pour l'affichage
            categories.add(new Category(0, "Toutes", "Toutes catégories"));
            while (rs.next()) {
                categories.add(new Category(rs.getInt("id"), rs.getString("nom"), ""));
            }
            categoryCombo.setValue(categories.get(0));

        } catch (Exception e) {
            msgLabel.setText("Erreur chargement catégories");
            e.printStackTrace();
        }
    }

    private void loadProducts(String query, Integer categoryId) {
        products.clear();

        // CORRECTION : Vérifier les noms des colonnes dans votre base
        String base = """
            SELECT p.id, p.nom, p.description, p.prix, p.stock, p.category_id, p.image_url,
                   c.nom AS categorie_nom
            FROM products p
            LEFT JOIN categories c ON p.category_id = c.id
            WHERE 1=1
        """;

        StringBuilder sql = new StringBuilder(base);
        List<Object> params = new ArrayList<>();

        if (query != null) {
            sql.append(" AND (p.nom LIKE ? OR p.description LIKE ?)");
            params.add("%" + query + "%");
            params.add("%" + query + "%");
        }

        if (categoryId != null && categoryId != 0) {
            sql.append(" AND p.category_id = ?");
            params.add(categoryId);
        }

        sql.append(" ORDER BY p.id DESC");

        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // CORRECTION : Utiliser les bons noms de colonnes
                    products.add(new Product(
                            rs.getInt("id"),
                            rs.getString("nom"), // CORRECTION : "nom" au lieu de "name"
                            rs.getString("description"),
                            rs.getBigDecimal("prix"), // CORRECTION : "prix" au lieu de "price"
                            rs.getInt("stock"),
                            rs.getObject("category_id") == null ? 0 : rs.getInt("category_id"),
                            rs.getString("categorie_nom"),
                            rs.getString("image_url") // CORRECTION : "image_url" au lieu de "image_path"
                    ));
                }
            }

        } catch (Exception e) {
            msgLabel.setText("Erreur chargement produits");
            e.printStackTrace();
        }
    }

    // ✅ bouton 🛒 Panier
    @FXML
    private void handleOpenCart() {
        SceneUtil.switchScene(productsList, "/fxml/cart-view.fxml", "Panier");
    }

    @FXML
    private void handleLogout() {
        SceneUtil.switchScene(productsList, "/fxml/login-view.fxml", "Mercedes Parts Manager - Login");
    }

    // ✅ Ajout panier via CartService (persist entre pages)
    private void addToCart(Product p, int qty) {
        msgLabel.setText("");

        if (p.getStock() <= 0) {
            msgLabel.setText("Produit hors stock.");
            return;
        }
        if (qty <= 0) qty = 1;
        if (qty > p.getStock()) {
            msgLabel.setText("Qté demandée > stock.");
            return;
        }

        // CORRECTION : Utiliser getNom(), getPrix(), getImageUrl()
        cartService.addOrIncrease(
                p.getId(),
                p.getNom(), // CORRECTION : getNom() au lieu de getName()
                p.getPrix(), // CORRECTION : getPrix() au lieu de getPrice()
                qty,
                p.getStock(),
                p.getImageUrl() // CORRECTION : getImageUrl() au lieu de getImagePath()
        );
        msgLabel.setText("Ajouté au panier ✅");
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private String formatMoney(BigDecimal v) {
        if (v == null) v = BigDecimal.ZERO;
        return v.toPlainString() + " DH";
    }

    private Image loadProductImageOrPlaceholder(Product p) {
        try {
            // CORRECTION : Utiliser getImageUrl() au lieu de getImagePath()
            String path = p.getImageUrl();
            if (path != null && !path.isBlank()) {
                String fileUrl = "file:" + Path.of(path).toAbsolutePath().toString().replace("\\", "/");
                Image img = new Image(fileUrl, 120, 90, true, true, true);
                if (!img.isError()) return img;
            }
        } catch (Exception ignored) {}

        try {
            return new Image(getClass().getResourceAsStream("/images/placeholder.png"));
        } catch (Exception ignored) {}

        return null;
    }

    // ✅ Product card
    private class ProductCardCell extends ListCell<Product> {
        @Override
        protected void updateItem(Product p, boolean empty) {
            super.updateItem(p, empty);

            if (empty || p == null) {
                setGraphic(null);
                return;
            }

            ImageView img = new ImageView(loadProductImageOrPlaceholder(p));
            img.setFitWidth(120);
            img.setFitHeight(90);
            img.setPreserveRatio(true);

            StackPane imgWrap = new StackPane(img);
            imgWrap.setStyle("""
                -fx-background-color: rgba(30,144,255,0.08);
                -fx-background-radius: 14;
                -fx-padding: 10;
            """);

            // CORRECTION : Utiliser getNom() au lieu de getName()
            Label name = new Label(p.getNom());
            name.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#0F3557;");

            Label desc = new Label(p.getDescription() == null ? "" : p.getDescription());
            desc.setWrapText(true);
            desc.setStyle("-fx-text-fill: rgba(15,53,87,0.75);");

            // CORRECTION : Utiliser getPrix() au lieu de getPrice()
            Label price = new Label("Prix: " + formatMoney(p.getPrix()));
            price.setStyle("-fx-font-weight:bold; -fx-text-fill:#0F3557;");

            Label stock = new Label("Stock: " + p.getStock());
            stock.setStyle("-fx-text-fill: rgba(15,53,87,0.60);");

            Spinner<Integer> qty = new Spinner<>(1, Math.max(1, p.getStock()), 1);
            qty.setPrefWidth(90);

            Button add = new Button("Ajouter");
            add.setStyle("""
                -fx-background-color: rgba(15,53,87,0.08);
                -fx-text-fill:#0F3557;
                -fx-font-weight:bold;
                -fx-background-radius: 12;
                -fx-padding: 8 12;
                -fx-cursor: hand;
            """);
            add.setOnAction(e -> addToCart(p, qty.getValue()));

            Button buy = new Button("Acheter");
            buy.setStyle("""
                -fx-background-color: linear-gradient(to right, #1E90FF, #00C2FF);
                -fx-text-fill:white;
                -fx-font-weight:bold;
                -fx-background-radius: 12;
                -fx-padding: 8 12;
                -fx-cursor: hand;
            """);
            buy.setOnAction(e -> {
                addToCart(p, qty.getValue());
                handleOpenCart(); // ✅ direct vers panier si tu veux
            });

            HBox actions = new HBox(10, new Label("Qté:"), qty, add, buy);
            actions.setStyle("-fx-text-fill:#0F3557;");

            VBox info = new VBox(6, name, desc, price, stock, actions);
            info.setPadding(new Insets(8));

            HBox root = new HBox(12, imgWrap, info);
            root.setPadding(new Insets(12));
            root.setStyle("""
                -fx-background-color: rgba(255,255,255,0.95);
                -fx-border-color: rgba(15,53,87,0.10);
                -fx-border-width: 1;
                -fx-border-radius: 16;
                -fx-background-radius: 16;
            """);

            setGraphic(root);
        }
    }
}