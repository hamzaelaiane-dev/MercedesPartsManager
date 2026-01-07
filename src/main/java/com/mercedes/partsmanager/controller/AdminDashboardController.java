package com.mercedes.partsmanager.controller;

import com.mercedes.partsmanager.model.Category;
import com.mercedes.partsmanager.model.Product;
import com.mercedes.partsmanager.model.User;
import com.mercedes.partsmanager.util.DB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.UUID;

public class AdminDashboardController {

    // Tables
    @FXML private TableView<Product> productsTable;
    @FXML private TableColumn<Product, Integer> colPId;
    @FXML private TableColumn<Product, String> colPName;
    @FXML private TableColumn<Product, BigDecimal> colPPrice;
    @FXML private TableColumn<Product, Integer> colPStock;
    @FXML private TableColumn<Product, String> colPCategory;

    @FXML private TableView<Category> categoriesTable;
    @FXML private TableColumn<Category, Integer> colCId;
    @FXML private TableColumn<Category, String> colCName;

    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Integer> colUId;
    @FXML private TableColumn<User, String> colUFullName;
    @FXML private TableColumn<User, String> colUUsername;
    @FXML private TableColumn<User, String> colURole;
    @FXML private TableColumn<User, Boolean> colUEnabled;

    // Produit form
    @FXML private TextField pNameField;
    @FXML private TextArea pDescField;
    @FXML private TextField pPriceField;
    @FXML private TextField pStockField;
    @FXML private ComboBox<Category> pCategoryCombo;
    @FXML private Label pMsgLabel;

    // NEW (Image)
    @FXML private Label pImageLabel;          // afficher le chemin
    @FXML private ImageView pImagePreview;    // optionnel si tu l'ajoutes dans FXML

    // Category form
    @FXML private TextField cNameField;
    @FXML private Label cMsgLabel;

    // Users msg
    @FXML private Label uMsgLabel;

    private final ObservableList<Product> products = FXCollections.observableArrayList();
    private final ObservableList<Category> categories = FXCollections.observableArrayList();
    private final ObservableList<User> users = FXCollections.observableArrayList();

    // NEW: store chosen image path temporarily (relative path: images/xxx.png)
    private String selectedImagePath = null;

    @FXML
    private void initialize() {

        // CORRECTION : Utiliser "id", "nom" au lieu de "id", "name"
        colPId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
        colPName.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("nom")); // Changé de "name" à "nom"
        colPPrice.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("prix")); // Changé de "price" à "prix"
        colPStock.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("stock"));
        colPCategory.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("categorie")); // Changé de "categoryName" à "categorie"

        colCId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
        colCName.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("nom")); // Changé de "name" à "nom"

        colUId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
        colUFullName.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("nom")); // Changé de "fullName" à "nom"
        colUUsername.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("email")); // Changé de "username" à "email"
        colURole.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("role"));
        colUEnabled.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("enabled"));

        productsTable.setItems(products);
        categoriesTable.setItems(categories);
        usersTable.setItems(users);

        // combo categories
        pCategoryCombo.setItems(categories);
        pCategoryCombo.setCellFactory(cb -> new ListCell<>() {
            @Override protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                // CORRECTION : Utiliser getNom() au lieu de getName()
                setText(empty || item == null ? "" : item.getNom());
            }
        });
        pCategoryCombo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                // CORRECTION : Utiliser getNom() au lieu de getName()
                setText(empty || item == null ? "" : item.getNom());
            }
        });

        // click row product => fill form
        productsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, p) -> {
            if (p != null) {
                // CORRECTION : Utiliser getNom() et getPrix() au lieu de getName() et getPrice()
                pNameField.setText(p.getNom());
                pDescField.setText(p.getDescription() == null ? "" : p.getDescription());
                pPriceField.setText(p.getPrix() == null ? "0.00" : p.getPrix().toString());
                pStockField.setText(String.valueOf(p.getStock()));

                Category cat = categories.stream()
                        .filter(c -> c.getId() == p.getCategoryId())
                        .findFirst().orElse(null);
                pCategoryCombo.setValue(cat);

                // NEW: image
                // CORRECTION : Utiliser getImageUrl() au lieu de getImagePath()
                selectedImagePath = p.getImageUrl();
                updateImageUI(selectedImagePath);
            }
        });

        refreshAll();
    }

    @FXML
    public void refreshAll() {
        pMsgLabel.setText("");
        cMsgLabel.setText("");
        uMsgLabel.setText("");
        loadCategories();
        loadProducts();
        loadUsers();
        clearProductForm(); // NEW: reset image too
    }

    // -------------------- CATEGORIES --------------------
    private void loadCategories() {
        categories.clear();
        // CORRECTION : Vérifier le nom de la colonne dans la base de données
        // Si c'est "nom" et non "name"
        String sql = "SELECT id, nom FROM categories ORDER BY id DESC";
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                // CORRECTION : Ajouter le 3ème paramètre (description) ou ajuster le constructeur
                categories.add(new Category(rs.getInt("id"), rs.getString("nom"), ""));
            }
        } catch (SQLException e) {
            cMsgLabel.setText("Erreur chargement catégories: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddCategory() {
        cMsgLabel.setText("");
        String name = safeTrim(cNameField.getText());
        if (name.isEmpty()) {
            cMsgLabel.setText("Nom catégorie obligatoire");
            return;
        }

        // CORRECTION : Utiliser "nom" au lieu de "name" si c'est le nom de la colonne
        String sql = "INSERT INTO categories(nom) VALUES(?)";
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.executeUpdate();
            cNameField.clear();
            loadCategories();
        } catch (SQLException e) {
            cMsgLabel.setText("Erreur ajout catégorie (doublon?)");
        }
    }

    @FXML
    private void handleDeleteCategory() {
        cMsgLabel.setText("");
        Category selected = categoriesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            cMsgLabel.setText("Sélectionne une catégorie");
            return;
        }

        try (Connection cn = DB.getConnection()) {
            cn.setAutoCommit(false);

            try (PreparedStatement ps1 = cn.prepareStatement("UPDATE products SET category_id=NULL WHERE category_id=?")) {
                ps1.setInt(1, selected.getId());
                ps1.executeUpdate();
            }

            try (PreparedStatement ps2 = cn.prepareStatement("DELETE FROM categories WHERE id=?")) {
                ps2.setInt(1, selected.getId());
                ps2.executeUpdate();
            }

            cn.commit();
            loadCategories();
            loadProducts();
        } catch (SQLException e) {
            cMsgLabel.setText("Erreur suppression catégorie");
        }
    }

    // -------------------- PRODUCTS --------------------
    private void loadProducts() {
        products.clear();
        // CORRECTION : Vérifier les noms des colonnes dans la base
        String sql = """
            SELECT p.id, p.nom, p.description, p.prix, p.stock, p.category_id, p.image_url,
                   c.nom AS categorie_nom
            FROM products p
            LEFT JOIN categories c ON p.category_id = c.id
            ORDER BY p.id DESC
        """;

        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Product p = new Product(
                        rs.getInt("id"),
                        rs.getString("nom"), // CORRECTION : "nom" au lieu de "name"
                        rs.getString("description"),
                        rs.getBigDecimal("prix"), // CORRECTION : "prix" au lieu de "price"
                        rs.getInt("stock"),
                        rs.getObject("category_id") == null ? 0 : rs.getInt("category_id"),
                        rs.getString("categorie_nom"),
                        rs.getString("image_url") // CORRECTION : "image_url" au lieu de "image_path"
                );
                products.add(p);
            }

        } catch (SQLException e) {
            pMsgLabel.setText("Erreur chargement produits");
            e.printStackTrace();
        }
    }

    // NEW: Choose image button
    @FXML
    private void handleChooseProductImage() {
        pMsgLabel.setText("");

        Stage stage = (Stage) productsTable.getScene().getWindow();

        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir une image produit");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.webp")
        );

        File selected = fc.showOpenDialog(stage);
        if (selected == null) return;

        try {
            selectedImagePath = copyToImagesFolder(selected); // returns "images/uuid.png"
            updateImageUI(selectedImagePath);
        } catch (Exception e) {
            pMsgLabel.setText("Erreur image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddProduct() {
        pMsgLabel.setText("");

        String name = safeTrim(pNameField.getText());
        String desc = pDescField.getText();
        String priceStr = safeTrim(pPriceField.getText());
        String stockStr = safeTrim(pStockField.getText());

        if (name.isEmpty()) {
            pMsgLabel.setText("Nom produit obligatoire");
            return;
        }

        BigDecimal price;
        int stock;
        try {
            price = priceStr.isEmpty() ? new BigDecimal("0.00") : new BigDecimal(priceStr);
            if (price.compareTo(BigDecimal.ZERO) < 0) {
                pMsgLabel.setText("Prix invalide");
                return;
            }
        } catch (Exception ex) {
            pMsgLabel.setText("Prix invalide (ex: 120.50)");
            return;
        }

        try {
            stock = stockStr.isEmpty() ? 0 : Integer.parseInt(stockStr);
            if (stock < 0) {
                pMsgLabel.setText("Stock invalide");
                return;
            }
        } catch (Exception ex) {
            pMsgLabel.setText("Stock invalide (ex: 10)");
            return;
        }

        Category cat = pCategoryCombo.getValue();
        Integer categoryId = (cat == null) ? null : cat.getId();

        // CORRECTION : Utiliser les bons noms de colonnes
        String sql = "INSERT INTO products(nom, description, prix, stock, category_id, image_url) VALUES(?,?,?,?,?,?)";

        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, (desc == null || desc.isBlank()) ? null : desc);
            ps.setBigDecimal(3, price);
            ps.setInt(4, stock);

            if (categoryId == null) ps.setNull(5, Types.INTEGER);
            else ps.setInt(5, categoryId);

            // NEW image
            ps.setString(6, (selectedImagePath == null || selectedImagePath.isBlank()) ? null : selectedImagePath);

            ps.executeUpdate();
            clearProductForm();
            loadProducts();

        } catch (SQLException e) {
            pMsgLabel.setText("Erreur ajout produit");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdateProduct() {
        pMsgLabel.setText("");
        Product selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            pMsgLabel.setText("Sélectionne un produit à modifier");
            return;
        }

        String name = safeTrim(pNameField.getText());
        String desc = pDescField.getText();
        String priceStr = safeTrim(pPriceField.getText());
        String stockStr = safeTrim(pStockField.getText());

        if (name.isEmpty()) {
            pMsgLabel.setText("Nom produit obligatoire");
            return;
        }

        BigDecimal price;
        int stock;
        try {
            price = priceStr.isEmpty() ? new BigDecimal("0.00") : new BigDecimal(priceStr);
        } catch (Exception ex) {
            pMsgLabel.setText("Prix invalide");
            return;
        }
        try {
            stock = stockStr.isEmpty() ? 0 : Integer.parseInt(stockStr);
        } catch (Exception ex) {
            pMsgLabel.setText("Stock invalide");
            return;
        }

        Category cat = pCategoryCombo.getValue();
        Integer categoryId = (cat == null) ? null : cat.getId();

        // CORRECTION : Utiliser les bons noms de colonnes
        String sql = "UPDATE products SET nom=?, description=?, prix=?, stock=?, category_id=?, image_url=? WHERE id=?";

        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, (desc == null || desc.isBlank()) ? null : desc);
            ps.setBigDecimal(3, price);
            ps.setInt(4, stock);

            if (categoryId == null) ps.setNull(5, Types.INTEGER);
            else ps.setInt(5, categoryId);

            // NEW image
            ps.setString(6, (selectedImagePath == null || selectedImagePath.isBlank()) ? null : selectedImagePath);

            ps.setInt(7, selected.getId());

            ps.executeUpdate();
            clearProductForm();
            loadProducts();

        } catch (SQLException e) {
            pMsgLabel.setText("Erreur modification produit");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteProduct() {
        pMsgLabel.setText("");
        Product selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            pMsgLabel.setText("Sélectionne un produit à supprimer");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer ce produit ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() != ButtonType.YES) return;

        String sql = "DELETE FROM products WHERE id=?";
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, selected.getId());
            ps.executeUpdate();
            clearProductForm();
            loadProducts();
        } catch (SQLException e) {
            pMsgLabel.setText("Erreur suppression produit");
        }
    }

    private void clearProductForm() {
        pNameField.clear();
        pDescField.clear();
        pPriceField.clear();
        pStockField.clear();
        pCategoryCombo.setValue(null);

        // NEW reset image
        selectedImagePath = null;
        updateImageUI(null);
    }

    // -------------------- USERS --------------------
    private void loadUsers() {
        users.clear();
        // CORRECTION : Vérifier les noms des colonnes dans la table users
        String sql = "SELECT id, nom, email, role FROM users ORDER BY id DESC";
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("nom"), // CORRECTION : "nom" au lieu de "full_name"
                        rs.getString("email"), // CORRECTION : "email" au lieu de "username"
                        "", // password vide
                        rs.getString("role")
                );
                // CORRECTION : Ajouter une méthode pour déterminer si enabled
                users.add(user);
            }
        } catch (SQLException e) {
            uMsgLabel.setText("Erreur chargement users");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleToggleUserEnabled() {
        uMsgLabel.setText("");
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            uMsgLabel.setText("Sélectionne un user");
            return;
        }

        // CORRECTION : Votre classe User n'a pas de méthode isEnabled()
        // Vous devez ajouter un champ "enabled" ou utiliser le rôle
        // Pour l'instant, cette fonctionnalité est désactivée
        uMsgLabel.setText("Fonctionnalité à implémenter");

        /*
        boolean newValue = !selected.isEnabled();
        String sql = "UPDATE users SET enabled=? WHERE id=?";
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, newValue ? 1 : 0);
            ps.setInt(2, selected.getId());
            ps.executeUpdate();
            loadUsers();
        } catch (SQLException e) {
            uMsgLabel.setText("Erreur update enabled");
        }
        */
    }

    // -------------------- NAV --------------------
    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login-view.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) productsTable.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Mercedes Parts Manager - Login");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // -------------------- HELPERS (IMAGE) --------------------

    private String copyToImagesFolder(File selected) throws Exception {
        Path imagesDir = Path.of("images");
        Files.createDirectories(imagesDir);

        String ext = getExtension(selected.getName()); // ".png"
        String newName = UUID.randomUUID() + ext;
        Path target = imagesDir.resolve(newName);

        Files.copy(selected.toPath(), target, StandardCopyOption.REPLACE_EXISTING);

        // store relative path in DB
        return "images/" + newName;
    }

    private String getExtension(String name) {
        int i = name.lastIndexOf(".");
        return (i >= 0) ? name.substring(i) : "";
    }

    private void updateImageUI(String path) {
        if (pImageLabel != null) {
            pImageLabel.setText(path == null ? "Aucune image" : path);
        }

        // preview is optional
        if (pImagePreview != null) {
            if (path == null || path.isBlank()) {
                pImagePreview.setImage(null);
                return;
            }
            try {
                String fileUrl = "file:" + Path.of(path).toAbsolutePath().toString().replace("\\", "/");
                Image img = new Image(fileUrl, 160, 110, true, true, true);
                pImagePreview.setImage(img);
            } catch (Exception e) {
                pImagePreview.setImage(null);
            }
        }
    }

    private String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }
}