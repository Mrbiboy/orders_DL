package com.example.orders;

import com.google.gson.*;
import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

class ParseJsonThread extends Thread {
    private String filePath;
    private List<JsonObject> parsedData;

    public ParseJsonThread(String filePath) {
        this.filePath = filePath;
        this.parsedData = new ArrayList<>();
    }

    @Override
    public void run() {
        try {
            System.out.println("Parsing JSON file in thread: " + filePath);
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            JsonArray jsonArray = JsonParser.parseString(content).getAsJsonArray();
            for (JsonElement element : jsonArray) {
                parsedData.add(element.getAsJsonObject());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<JsonObject> getParsedData() {
        return parsedData;
    }
}

public class OrderProcessor {

    public static void main(String[] args) {
        String inputFilePath = "src/data/input.json";
        String outputFilePath = "src/data/output.json";
        String errorFilePath = "src/data/error.json";

        // Planificateur pour exécuter la tâche chaque heure
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        // Tâche à exécuter
        Runnable task = () -> {
            System.out.println("Début du traitement des orders à : " + new java.util.Date());
            processOrders(inputFilePath, outputFilePath, errorFilePath);
        };

        // Exécuter la tâche chaque heure
        scheduler.scheduleAtFixedRate(task, 0, 1, TimeUnit.HOURS);
    }

    private static void processOrders(String inputFilePath, String outputFilePath, String errorFilePath) {
        // Start the JSON parsing thread
        ParseJsonThread parseThread = new ParseJsonThread(inputFilePath);
        parseThread.start();

        try {
            // Wait for the parsing thread to finish
            parseThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Get the parsed data
        List<JsonObject> inputOrders = parseThread.getParsedData();

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/orders", "root", "")) {
            List<JsonObject> outputOrders = new ArrayList<>();
            List<JsonObject> errorOrders = new ArrayList<>();

            for (JsonObject order : inputOrders) {
                String customerEmail = order.get("customer_email").getAsString();
                int customerId = getCustomerId(connection, customerEmail);

                if (customerId != -1) {
                    // Add order to database and output list
                    insertOrder(connection, order, customerId);
                    outputOrders.add(order);
                } else {
                    // Add order to error list
                    errorOrders.add(order);
                }
            }

            // Write output and error lists to files
            writeJsonFile(outputFilePath, outputOrders);
            writeJsonFile(errorFilePath, errorOrders);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static int getCustomerId(Connection connection, String email) {
        String query = "SELECT id FROM customer WHERE email = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private static void insertOrder(Connection connection, JsonObject order, int customerId) {
        String query = "INSERT INTO `order` (date, amount, customer_id) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            long currentTimeMillis = System.currentTimeMillis();
            statement.setTimestamp(1, new Timestamp(currentTimeMillis));
            statement.setDouble(2, order.get("amount").getAsDouble());
            statement.setInt(3, customerId);

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void writeJsonFile(String filePath, List<JsonObject> jsonObjects) {
        try (FileWriter writer = new FileWriter(filePath)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(jsonObjects, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
