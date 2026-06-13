package com.cecilia.gigi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.cecilia.gigi.database.DatabaseConnection;

@SpringBootApplication
public class GigiApplication {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(GigiApplication.class, args);
		try {
			DatabaseConnection.getConnection();
		} catch (Exception err) {
			System.err.println("Failed to connect to the database: " + err.getMessage());
			throw new RuntimeException(err.getMessage());
		}
		System.err.println("Gigi Application started successfully.");
	}

}
