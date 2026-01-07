package com.mercedes.partsmanager.service;

import com.mercedes.partsmanager.model.User;
import com.mercedes.partsmanager.util.DB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserService {

    public String loginRole(String email, String password) {
        String sql = "SELECT role FROM users WHERE email = ? AND password = ?";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("role");
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur login: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public boolean registerUser(String nom, String email, String password) {
        String sql = "INSERT INTO users (nom, email, password, role) VALUES (?, ?, ?, 'client')";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nom);
            ps.setString(2, email);
            ps.setString(3, password);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("Erreur inscription: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public User getUserByEmail(String email) {
        String sql = "SELECT id, nom, email, role FROM users WHERE email = ?";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("nom"),
                            rs.getString("email"),
                            "", // ne pas retourner le mot de passe
                            rs.getString("role")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}