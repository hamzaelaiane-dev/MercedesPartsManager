package com.mercedes.partsmanager.util;

public class DbTest {
    public static void main(String[] args) {
        try {
            System.out.println("Testing DB connection...");
            System.out.println(DB.getConnection() != null ? "✅ DB CONNECTED" : "❌ DB NULL");
        } catch (Exception e) {
            System.out.println("❌ DB FAILED:");
            e.printStackTrace();
        }
    }
}
