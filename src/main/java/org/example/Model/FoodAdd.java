package org.example.Model;

import org.neo4j.driver.*;

import java.io.*;
import java.util.*;

import static org.neo4j.driver.Values.parameters;

public class FoodAdd {
    private static final String URI = "bolt://localhost:7687";
    private static final String USER = "neo4j";
    private static final String PASSWORD = "Estructuras123";

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        Driver driver = GraphDatabase.driver(URI, AuthTokens.basic(USER, PASSWORD));

        while (true) {
            System.out.println("\n--- Diet Recommender Menu ---");
            System.out.println("1. Add food manually");
            System.out.println("2. Add foods from CSV/TXT file");
            System.out.println("3. Exit");
            System.out.print("Select an option: ");
            String choice = scanner.nextLine();

            try (Session session = driver.session()) {
                switch (choice) {
                    case "1" -> addFoodManually(session);
                    case "2" -> loadFoodsFromFile(session);
                    case "3" -> {
                        driver.close();
                        return;
                    }
                    default -> System.out.println("Invalid choice.");
                }
            }
        }
    }

    private static void addFoodManually(Session session) {
        System.out.print("Name: ");
        String name = scanner.nextLine();

        String id = UUID.randomUUID().toString();
        int calories = promptInt("Calories: ");
        int protein = promptInt("Protein: ");
        int carbs = promptInt("Carbs: ");
        int fats = promptInt("Fats: ");

        System.out.print("Type (vegan/vegetarian/omnivore): ");
        String type = scanner.nextLine();
        System.out.print("Price: ");
        double price = Double.parseDouble(scanner.nextLine());
        System.out.print("Preferences (pipe-separated, e.g. high_protein|gluten_free): ");
        String prefLine = scanner.nextLine();
        List<String> preferences = Arrays.asList(prefLine.split("\\|"));

        System.out.print("Image Path: ");
        String imagePath = scanner.nextLine();
        System.out.print("Recipe: ");
        String receta = scanner.nextLine();

        createFoodNode(session, id, name, calories, protein, carbs, fats, type, price, preferences, imagePath, receta);
    }

    private static void loadFoodsFromFile(Session session) {
        System.out.print("Enter file path: ");
        String path = scanner.nextLine();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            boolean headerSkipped = false;
            while ((line = br.readLine()) != null) {
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue;
                }

                String[] parts = line.split(",", -1); // Allow blank values
                String id = parts.length > 0 && !parts[0].isBlank() ? parts[0] : UUID.randomUUID().toString();
                String name = parts.length > 1 ? parts[1] : "Unnamed";
                int calories = parseIntSafe(parts, 2);
                int protein = parseIntSafe(parts, 3);
                int carbs = parseIntSafe(parts, 4);
                int fats = parseIntSafe(parts, 5);
                String type = parts.length > 6 ? parts[6] : "";
                double price = parseDoubleSafe(parts, 7);
                List<String> prefs = new ArrayList<>();
                if (parts.length > 8 && !parts[8].isBlank()) {
                    prefs = Arrays.asList(parts[8].split("\\|"));
                }
                String imagePath = parts.length > 9 ? parts[9] : "";
                String receta = parts.length > 10 ? parts[10] : "";

                createFoodNode(session, id, name, calories, protein, carbs, fats, type, price, prefs, imagePath, receta);
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    private static int promptInt(String prompt) {
        System.out.print(prompt);
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (Exception e) {
            return 0;
        }
    }

    private static int parseIntSafe(String[] arr, int index) {
        if (index >= arr.length || arr[index].isBlank()) return 0;
        try {
            return Integer.parseInt(arr[index]);
        } catch (Exception e) {
            return 0;
        }
    }

    private static double parseDoubleSafe(String[] arr, int index) {
        if (index >= arr.length || arr[index].isBlank()) return 0.0;
        try {
            return Double.parseDouble(arr[index]);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private static void createFoodNode(Session session, String id, String name, int calories, int protein,
                                       int carbs, int fats, String type, double price, List<String> preferences,
                                       String imagePath, String receta) {
        session.writeTransaction(tx -> {
            tx.run("""
                CREATE (f:Food {
                    id: $id, name: $name, calories: $calories,
                    protein: $protein, carbs: $carbs, fats: $fats,
                    type: $type, price: $price,
                    imagePath: $imagePath, receta: $receta,
                    totalVotos: 0, sumaPuntuaciones: 0
                })
            """, parameters("id", id, "name", name, "calories", calories,
                    "protein", protein, "carbs", carbs, "fats", fats,
                    "type", type, "price", price, "imagePath", imagePath, "receta", receta));

            List<String> parsedPreferences = new ArrayList<>();
            for (String group : preferences) {
                for (String indiv : group.split(";")) {
                    if (!indiv.isBlank()) parsedPreferences.add(indiv.trim());
                }
            }

            for (String pref : parsedPreferences) {
                if (!pref.isBlank()) {
                    tx.run("""
                        MERGE (p:Preference {type: $pref})
                        WITH p
                        MATCH (f:Food {id: $id})
                        MERGE (f)-[:HAS_PROPERTY]->(p)
                    """, parameters("id", id, "pref", pref.trim()));
                }
            }

            System.out.println("Created food node: " + name);
            return null;
        });
    }
}