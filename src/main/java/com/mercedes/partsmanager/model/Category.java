package com.mercedes.partsmanager.model;

public class Category {
    private int id;
    private String nom;
    private String description;

    // Constructeurs
    public Category() {}

    public Category(int id, String nom, String description) {
        this.id = id;
        this.nom = nom;
        this.description = description;
    }

    public Category(String nom, String description) {
        this.nom = nom;
        this.description = description;
    }

    // Getters français
    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getDescription() { return description; }

    // ALIAS EN ANGLAIS
    public String getName() { return nom; }
    public String getDesc() { return description; }

    // Setters français
    public void setId(int id) { this.id = id; }
    public void setNom(String nom) { this.nom = nom; }
    public void setDescription(String description) { this.description = description; }

    // Alias setters en anglais
    public void setName(String name) { this.nom = name; }
    public void setDesc(String desc) { this.description = desc; }

    @Override
    public String toString() {
        return nom;
    }
}