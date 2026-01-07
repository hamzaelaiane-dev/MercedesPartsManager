package com.mercedes.partsmanager.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB {

    // ⚠️ change "mercedes_parts" par le NOM EXACT de ta base
    private static final String URL =
            "jdbc:mysql://localhost:3306/mercedes_parts_manager?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = ""; // XAMPP souvent vide

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
