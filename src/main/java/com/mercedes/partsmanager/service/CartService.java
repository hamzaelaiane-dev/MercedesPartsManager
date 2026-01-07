package com.mercedes.partsmanager.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.math.BigDecimal;

public class CartService {

    private static final CartService INSTANCE = new CartService();
    private final ObservableList<CartItem> items = FXCollections.observableArrayList();

    private CartService() {}

    public static CartService getInstance() {
        return INSTANCE;
    }

    public ObservableList<CartItem> getItems() {
        return items;
    }

    public void clear() {
        items.clear();
    }

    public BigDecimal getTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem ci : items) total = total.add(ci.total());
        return total;
    }

    public void addOrIncrease(int productId, String name, BigDecimal price, int qty, int stock, String imagePath) {
        if (qty <= 0) qty = 1;
        if (qty > stock) qty = stock;

        CartItem existing = items.stream()
                .filter(i -> i.getProductId() == productId)
                .findFirst().orElse(null);

        if (existing == null) {
            items.add(new CartItem(productId, name, price, qty, imagePath));
        } else {
            int newQty = existing.getQty() + qty;
            if (newQty > stock) newQty = stock;
            existing.setQty(newQty);

            // au cas où image est vide au début
            if ((existing.getImagePath() == null || existing.getImagePath().isBlank()) && imagePath != null) {
                existing.setImagePath(imagePath);
            }
        }
    }

    public void remove(CartItem item) {
        items.remove(item);
    }

    public static class CartItem {
        private final int productId;
        private final String name;
        private final BigDecimal price;
        private int qty;

        private String imagePath; // ✅ NEW

        public CartItem(int productId, String name, BigDecimal price, int qty, String imagePath) {
            this.productId = productId;
            this.name = name;
            this.price = price == null ? BigDecimal.ZERO : price;
            this.qty = qty;
            this.imagePath = imagePath;
        }

        public int getProductId() { return productId; }
        public String getName() { return name; }
        public BigDecimal getPrice() { return price; }
        public int getQty() { return qty; }
        public void setQty(int qty) { this.qty = qty; }

        public String getImagePath() { return imagePath; }
        public void setImagePath(String imagePath) { this.imagePath = imagePath; }

        public BigDecimal total() { return price.multiply(new BigDecimal(qty)); }
    }
}
