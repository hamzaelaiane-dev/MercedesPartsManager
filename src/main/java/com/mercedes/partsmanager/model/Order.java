package com.mercedes.partsmanager.model;

import java.time.LocalDateTime;

public class Order {
    private int id;
    private int userId;
    private double total;
    private String status;
    private LocalDateTime dateCommande;

    // Constructeurs
    public Order() {}

    public Order(int id, int userId, double total, String status, LocalDateTime dateCommande) {
        this.id = id;
        this.userId = userId;
        this.total = total;
        this.status = status;
        this.dateCommande = dateCommande;
    }

    public Order(int userId, double total, String status) {
        this.userId = userId;
        this.total = total;
        this.status = status;
        this.dateCommande = LocalDateTime.now();
    }

    // Getters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public double getTotal() { return total; }
    public String getStatus() { return status; }
    public LocalDateTime getDateCommande() { return dateCommande; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setTotal(double total) { this.total = total; }
    public void setStatus(String status) { this.status = status; }
    public void setDateCommande(LocalDateTime dateCommande) { this.dateCommande = dateCommande; }

    @Override
    public String toString() {
        return "Commande #" + id + " - " + total + " DH (" + status + ")";
    }
}