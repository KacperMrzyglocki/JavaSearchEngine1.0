package com.example;

import com.example.controller.QueryController;

import static spark.Spark.*;

public class QueryAPI {
    public static void main(String[] args) {
        port(8080);

        // Konfiguracja CORS
        after((req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            res.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
        });

        // Rejestracja endpointów w kontrolerze
        QueryController controller = new QueryController();
        controller.registerRoutes();

        System.out.println("Query Engine API is running on http://localhost:8080");
    }
}
