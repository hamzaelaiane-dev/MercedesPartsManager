package com.mercedes.partsmanager.dao;

import com.mercedes.partsmanager.model.Product;
import com.mercedes.partsmanager.util.DB;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class CartDAO {

    // Pour sauvegarder le panier temporaire (vous pouvez utiliser une session ou base temporaire)
    public boolean saveCart(int userId, Map<Product, Integer> cartItems) {
        // Si vous voulez sauvegarder dans une table temporaire
        String deleteSql = "DELETE FROM temp_cart WHERE user_id = ?";
        String insertSql = "INSERT INTO temp_cart (user_id, product_id, quantity) VALUES (?, ?, ?)";

        try (Connection conn = DB.getConnection()) {
            conn.setAutoCommit(false);

            // Supprimer l'ancien panier
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, userId);
                deleteStmt.executeUpdate();
            }

            // Insérer les nouveaux items
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                for (Map.Entry<Product, Integer> entry : cartItems.entrySet()) {
                    insertStmt.setInt(1, userId);
                    insertStmt.setInt(2, entry.getKey().getId());
                    insertStmt.setInt(3, entry.getValue());
                    insertStmt.addBatch();
                }
                insertStmt.executeBatch();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Charger le panier depuis la table temporaire
    public Map<Product, Integer> loadCart(int userId) {
        Map<Product, Integer> cartItems = new HashMap<>();
        ProductDAO productDAO = new ProductDAO();

        String sql = "SELECT product_id, quantity FROM temp_cart WHERE user_id = ?";

        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Product product = productDAO.getProductById(rs.getInt("product_id"));
                if (product != null) {
                    cartItems.put(product, rs.getInt("quantity"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cartItems;
    }
}