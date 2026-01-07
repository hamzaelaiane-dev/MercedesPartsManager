package com.mercedes.partsmanager.model;

public class CartItem {
    private Product product;
    private int quantity;

    // Constructeurs
    public CartItem() {}

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    // Getters
    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }

    // Setters
    public void setProduct(Product product) { this.product = product; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    // Méthodes utiles
    public double getSubtotal() {
        return product.getPrixDouble() * quantity;
    }

    public void incrementQuantity() {
        this.quantity++;
    }

    public void decrementQuantity() {
        if (this.quantity > 1) {
            this.quantity--;
        }
    }

    @Override
    public String toString() {
        return product.getNom() + " x" + quantity + " = " + getSubtotal() + " DH";
    }
}