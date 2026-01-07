package com.mercedes.partsmanager.dao;

import com.mercedes.partsmanager.model.Order;
import com.mercedes.partsmanager.model.OrderLine;
import com.mercedes.partsmanager.util.DB;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    // Créer une commande
    public int createOrder(int userId, double total, List<OrderLine> orderLines) {
        Connection conn = null;
        try {
            conn = DB.getConnection();
            conn.setAutoCommit(false);

            // 1. Créer la commande
            String orderSql = "INSERT INTO orders (user_id, total, status, date_commande) VALUES (?, ?, 'en_attente', NOW())";
            PreparedStatement orderStmt = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS);
            orderStmt.setInt(1, userId);
            orderStmt.setDouble(2, total);

            int rows = orderStmt.executeUpdate();
            if (rows == 0) {
                conn.rollback();
                return -1;
            }

            // 2. Récupérer l'ID de la commande
            ResultSet generatedKeys = orderStmt.getGeneratedKeys();
            int orderId = -1;
            if (generatedKeys.next()) {
                orderId = generatedKeys.getInt(1);
            }

            // 3. Créer les lignes de commande
            String lineSql = "INSERT INTO order_lines (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
            PreparedStatement lineStmt = conn.prepareStatement(lineSql);

            for (OrderLine line : orderLines) {
                lineStmt.setInt(1, orderId);
                lineStmt.setInt(2, line.getProductId());
                lineStmt.setInt(3, line.getQuantity());
                lineStmt.setDouble(4, line.getPrice());
                lineStmt.addBatch();
            }

            lineStmt.executeBatch();
            conn.commit();
            return orderId;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return -1;
    }

    // Obtenir les commandes d'un utilisateur
    public List<Order> getUserOrders(int userId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE user_id = ? ORDER BY date_commande DESC";

        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Order order = new Order(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getDouble("total"),
                        rs.getString("status"),
                        rs.getTimestamp("date_commande").toLocalDateTime()
                );
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    // Obtenir les lignes d'une commande
    public List<OrderLine> getOrderLines(int orderId) {
        List<OrderLine> lines = new ArrayList<>();
        String sql = "SELECT * FROM order_lines WHERE order_id = ?";

        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                OrderLine line = new OrderLine(
                        rs.getInt("id"),
                        rs.getInt("order_id"),
                        rs.getInt("product_id"),
                        rs.getInt("quantity"),
                        rs.getDouble("price")
                );
                lines.add(line);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lines;
    }

    // Mettre à jour le statut d'une commande
    public boolean updateOrderStatus(int orderId, String status) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";

        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setInt(2, orderId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}