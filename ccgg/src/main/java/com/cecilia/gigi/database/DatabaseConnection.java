/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.cecilia.gigi.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author marco
 */
public class DatabaseConnection {
    private static final String URL_MYSQL = "jdbc:mysql://localhost:3306/gigi";
    private static final String USER = "gigi";
    private static final String PASSWORD = "cecilia";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL_MYSQL, USER, PASSWORD);
        } catch (SQLException err) {
            throw new RuntimeException("Erro ao conectar ao banco de dados: " + err.getMessage(), err);
        }
    }
}
