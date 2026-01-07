package com.mercedes.partsmanager.service;

import com.mercedes.partsmanager.util.DB;

import java.math.BigDecimal;
import java.sql.*;
import java.util.List;

public class OrderService {

    public static class OrderItemDTO {
        public int productId;
        public int quantity;
        public BigDecimal unitPrice;

        public OrderItemDTO(int productId, int quantity, BigDecimal unitPrice) {
            this.productId = productId;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }
    }

    public int createOrder(int userId, BigDecimal total, List<OrderItemDTO> items) throws SQLException {
        String insertOrder = "INSERT INTO orders(user_id, total, status) VALUES(?,?, 'PAID')";
        String insertItem  = "INSERT INTO order_items(order_id, product_id, quantity, unit_price, line_total) VALUES(?,?,?,?,?)";
        String updateStock = "UPDATE products SET stock = stock - ? WHERE id=? AND stock >= ?";

        try (Connection cn = DB.getConnection()) {
            cn.setAutoCommit(false);

            try (PreparedStatement psOrder = cn.prepareStatement(insertOrder, Statement.RETURN_GENERATED_KEYS)) {
                psOrder.setInt(1, userId);
                psOrder.setBigDecimal(2, total);
                psOrder.executeUpdate();

                int orderId;
                try (ResultSet keys = psOrder.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("Order ID not generated");
                    orderId = keys.getInt(1);
                }

                // insert items + update stock
                try (PreparedStatement psItem = cn.prepareStatement(insertItem);
                     PreparedStatement psStock = cn.prepareStatement(updateStock)) {

                    for (OrderItemDTO it : items) {
                        BigDecimal lineTotal = it.unitPrice.multiply(new BigDecimal(it.quantity));

                        // stock check + decrement
                        psStock.setInt(1, it.quantity);
                        psStock.setInt(2, it.productId);
                        psStock.setInt(3, it.quantity);
                        int updated = psStock.executeUpdate();
                        if (updated == 0) {
                            cn.rollback();
                            throw new SQLException("Stock insuffisant pour product_id=" + it.productId);
                        }

                        psItem.setInt(1, orderId);
                        psItem.setInt(2, it.productId);
                        psItem.setInt(3, it.quantity);
                        psItem.setBigDecimal(4, it.unitPrice);
                        psItem.setBigDecimal(5, lineTotal);
                        psItem.addBatch();
                    }
                    psItem.executeBatch();
                }

                cn.commit();
                return orderId;

            } catch (SQLException e) {
                cn.rollback();
                throw e;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }
}
