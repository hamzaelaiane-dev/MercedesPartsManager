package com.mercedes.partsmanager.model;

import java.math.BigDecimal;

public class Product {
    private int id;
    private String nom;
    private String description;
    private BigDecimal prix;
    private int stock;
    private int categoryId;
    private String categorie;
    private String imageUrl;

    // Constructeurs
    public Product() {}

    // Constructeur complet
    public Product(int id, String nom, String description, BigDecimal prix,
                   int stock, int categoryId, String categorie, String imageUrl) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.stock = stock;
        this.categoryId = categoryId;
        this.categorie = categorie;
        this.imageUrl = imageUrl;
    }

    // Constructeur simplifié
    public Product(int id, String nom, String description, double prix,
                   int stock, String categorie) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.prix = BigDecimal.valueOf(prix);
        this.stock = stock;
        this.categoryId = 0;
        this.categorie = categorie;
        this.imageUrl = "";
    }

    // Constructeur pour création
    public Product(String nom, String description, BigDecimal prix,
                   int stock, String categorie, String imageUrl) {
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.stock = stock;
        this.categorie = categorie;
        this.imageUrl = imageUrl;
    }

    // Getters
    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getDescription() { return description; }
    public BigDecimal getPrix() { return prix; }
    public double getPrixDouble() { return prix.doubleValue(); }
    public int getStock() { return stock; }
    public int getCategoryId() { return categoryId; }
    public String getCategorie() { return categorie; }
    public String getImageUrl() { return imageUrl; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setNom(String nom) { this.nom = nom; }
    public void setDescription(String description) { this.description = description; }
    public void setPrix(BigDecimal prix) { this.prix = prix; }
    public void setPrix(double prix) { this.prix = BigDecimal.valueOf(prix); }
    public void setStock(int stock) { this.stock = stock; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    @Override
    public String toString() {
        return nom + " - " + prix + " DH";
    }
}