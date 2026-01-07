package com.mercedes.partsmanager.dao;

import com.mercedes.partsmanager.model.Category;
import com.mercedes.partsmanager.util.DB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    // Obtenir toutes les catégories
    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories ORDER BY nom";

        try (Connection conn = DB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Category category = new Category(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("description")
                );
                categories.add(category);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    // Obtenir les noms des catégories (pour ComboBox)
    public List<String> getCategoryNames() {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT DISTINCT categorie FROM products WHERE categorie IS NOT NULL ORDER BY categorie";

        try (Connection conn = DB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                categories.add(rs.getString("categorie"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }
}