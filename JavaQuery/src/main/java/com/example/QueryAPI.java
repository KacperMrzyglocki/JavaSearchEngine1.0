package com.example;

import com.example.controller.*;

import static spark.Spark.*;

public class QueryAPI {
    public static void main(String[] args) {
        port(8081);

        // Konfiguracja CORS
        after((req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            res.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
        });

        // Rejestracja endpointów w kontrolerach
        DictionaryController dictionaryController = new DictionaryController();
        dictionaryController.registerRoutes();

        NormalController normalController = new NormalController();
        normalController.registerRoutes();

        FolderController folderController = new FolderController();
        folderController.registerRoutes();

        MetadataController metadataController = new MetadataController();
        metadataController.registerRoutes();

        HazelcastController hazelcastController = new HazelcastController();
        hazelcastController.registerRoutes();

        StatsController statsController = new StatsController();
        statsController.registerRoutes();

        System.out.println("Query Engine API is running on http://localhost:8081");
    }
}
