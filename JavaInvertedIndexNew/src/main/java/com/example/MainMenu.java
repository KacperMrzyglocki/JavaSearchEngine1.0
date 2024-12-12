package com.example;

import java.util.Scanner;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;

public class MainMenu {
    private static final InvertedIndexFolders index = new InvertedIndexFolders();
    private static final HazelcastInstance hazelcastInstance =
            HazelcastConfig.getHazelcastInstance();

    public static void main(String[] args) {
        boolean exit = false;
        Scanner scanner = new Scanner(System.in);

        while (!exit) {
            System.out.println("1. Index books");
            System.out.print("?> ");
            int choice = -1;
            // wait for the input to be attached
            try {
                choice = scanner.nextInt();
                scanner.nextLine(); // consume the newline character
            } catch (Exception e) {
                System.out.println("No input detected.");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                continue;
            }

            switch (choice) {
                case 1:
                    IExecutorService executorService = hazelcastInstance.getExecutorService("default");
                    executorService.submit(new IndexTask());
                    break;

                default:
                    exit = true;
            }
        }
        scanner.close();
    }

    static class IndexTask implements Runnable {
        @Override
        public void run() {
            long startTime = System.currentTimeMillis();
            index.buildIndex("/app/books.json");
            long endTime = System.currentTimeMillis();
            System.out.println("Index completed in " + (endTime - startTime) + "ms");

            startTime = System.currentTimeMillis();
            index.serialize();
            endTime = System.currentTimeMillis();
            System.out.println("Index built and serialized in " + (endTime - startTime) + " ms.");
            System.out.println("Index built.");
        }
    }
}
