package org.example.Model;

import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ComidaService {

    private static final Neo4JConnect connector = new Neo4JConnect("bolt://localhost:7687", "Neo4j", "Estructuras123");

    public static List<Comida> obtenerMuestrasIniciales() {
        List<Comida> comidas = new ArrayList<>();

        try (Session session = connector.getSession()) {
            Result result = session.run("MATCH (f:Food) RETURN f ORDER BY rand() LIMIT 10");

            while (result.hasNext()) {
                Record r = result.next();
                Node n = r.get("f").asNode();

                String id = n.get("id").asString("");
                String name = n.get("name").asString("");
                int calories = n.get("calories").asInt(0);
                double protein = n.get("protein").asInt();
                double carbs = n.get("carbs").asInt();
                double fats = n.get("fats").asInt();
                String type = n.get("type").asString("");
                double price = n.get("price").asDouble(0.0);
                String imagePath = n.get("imagePath").asString("");
                String receta = n.get("receta").asString("");
                int totalVotos = n.get("totalVotos").asInt(0);
                int sumaPuntuaciones = n.get("sumaPuntuaciones").asInt(0);

                // Obtener las preferencias asociadas al nodo
                Set<String> preferencias = new HashSet<>();
                Result prefResult = session.run("MATCH (f:Food {id: $id})-[:HAS_PROPERTY]->(p:Preference) RETURN p.type AS type",
                        Values.parameters("id", id));

                while (prefResult.hasNext()) {
                    String tipo = prefResult.next().get("type").asString();
                    preferencias.add(tipo);
                }

                Comida comida = new Comida(id, name, calories, protein, carbs, fats, type, price, preferencias, imagePath, receta);
                comida.setTotalVotos(totalVotos);
                comida.setSumaPuntuaciones(sumaPuntuaciones);
                comidas.add(comida);
            }
        }

        return comidas;
    }

    public static List<Comida> getTodas() {
        List<Comida> comidas = new ArrayList<>();

        try (Session session = connector.getSession()) {
            Result result = session.run("MATCH (f:Food) RETURN f");

            while (result.hasNext()) {
                Record r = result.next();
                Node n = r.get("f").asNode();

                String id = n.get("id").asString("");
                String name = n.get("name").asString("");
                int calories = n.get("calories").asInt(0);
                double protein = n.get("protein").asDouble(0.0);
                double carbs = n.get("carbs").asDouble(0.0);
                double fats = n.get("fats").asDouble(0.0);
                String type = n.get("type").asString("");
                double price = n.get("price").asDouble(0.0);
                String imagePath = n.get("imagePath").asString("");
                String receta = n.get("receta").asString("");
                int totalVotos = n.get("totalVotos").asInt(0);
                int sumaPuntuaciones = n.get("sumaPuntuaciones").asInt(0);

                // Obtener las preferencias asociadas al nodo
                Set<String> preferencias = new HashSet<>();
                Result prefResult = session.run("MATCH (f:Food {id: $id})-[:HAS_PROPERTY]->(p:Preference) RETURN p.type AS type",
                        Values.parameters("id", id));

                while (prefResult.hasNext()) {
                    String tipo = prefResult.next().get("type").asString();
                    preferencias.add(tipo);
                }

                Comida comida = new Comida(id, name, calories, protein, carbs, fats, type, price, preferencias, imagePath, receta);
                comida.setTotalVotos(totalVotos);
                comida.setSumaPuntuaciones(sumaPuntuaciones);
                comidas.add(comida);
            }
        }

        return comidas;
    }
}