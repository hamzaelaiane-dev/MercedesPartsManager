package com.mercedes.partsmanager.service;

import com.mercedes.partsmanager.model.Product;
import com.mercedes.partsmanager.util.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDashboardService {

    public List<Product> loadProducts(Integer categoryId, String search) {
        List<Product> list = new ArrayList<>();

        String base = """
            SELECT p.id, p.name, p.description, p.price, p.stock, p.category_id,
                   c.name AS category_name
            FROM products p
            LEFT JOIN categories c ON p.category_id = c.id
            WHERE 1=1
        """;

        if (categoryId != null && categoryId > 0) base += " AND p.category_id = ? ";
        if (search != null && !search.isBlank()) base += " AND p.name LIKE ? ";
        base += " ORDER BY p.id DESC";

        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(base)) {

            int i = 1;
            if (categoryId != null && categoryId > 0) ps.setInt(i++, categoryId);
            if (search != null && !search.isBlank()) ps.setString(i++, "%" + search.trim() + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Product p = new Product(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getBigDecimal("price"),
                            rs.getInt("stock"),
                            rs.getObject("category_id") == null ? 0 : rs.getInt("category_id"),
                            rs.getString("category_name"),
                            rs.getString("image_path")   // ✅ AJOUT ICI
                    );
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}
