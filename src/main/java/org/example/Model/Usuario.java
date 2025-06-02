package org.example.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class Usuario {
    private String correo;
    private String password;
    private String id;
    private String nombre;
    private int edad;
    private String genero;
    private double altura;
    private double peso;
    private List<Comida> PreferenciasIniciales;
    private List<RegistroPuntuacion> puntuacionesRealizadas;


    public Usuario(String correo, String password) {
        this.correo = correo;
        this.password = password;
        this.PreferenciasIniciales = new ArrayList<>();
        this.puntuacionesRealizadas = new ArrayList<>();

    }

    public Usuario(String correo, String hashed, String nombre, int edad, String genero, double altura, double peso) {
        this.correo = correo;
        this.password = hashed;
        this.nombre = nombre;
        this.edad = edad;
        this.genero = genero;
        this.altura = altura;
        this.peso = peso;
        this.puntuacionesRealizadas = new ArrayList<>();

    }

    public String getCorreo() {
        return correo;
    }

    public String getPassword() {
        return password;
    }

    public String getNombre() {
        return nombre;
    }

    public String getGenero() {
        return genero;
    }

    public String getId() {
        return id;
    }

    public int getEdad() {
        return edad;
    }

    public double getAltura() {
        return altura;
    }

    public double getPeso() {
        return peso;
    }

    public void agregarPreferenciaInicial(Comida comida) {
        this.PreferenciasIniciales.add(comida);
    }

    public List<Comida> getPreferenciasIniciales() {
        return PreferenciasIniciales;
    }

    public void agregarPuntuacion(RegistroPuntuacion registro) {
        this.puntuacionesRealizadas.add(registro);
    }

    public List<RegistroPuntuacion> getPuntuacionesRealizadas() {
        return puntuacionesRealizadas;
    }

    public List<RegistroComida> obtenerHistorial() {
        List<RegistroComida> historial = new ArrayList<>();
        try (var session = Neo4JConnect.getInstance().getSession()) {
            if (this.id == null) this.id = this.correo;
            var result = session.run("""
                MATCH (u:Usuario {correo: $correo})-[r:CONSUMIO]->(c:Food)
                RETURN c.id AS id, c.calories AS calorias, r.fecha AS fecha
            """, org.neo4j.driver.Values.parameters("correo", this.correo));

            while (result.hasNext()) {
                var record = result.next();
                String comidaId = record.get("id").asString();
                double calorias = record.get("calorias").asDouble();
                if (record.get("fecha").isNull()) continue;
                java.time.LocalDate fecha = record.get("fecha").asLocalDate();
                historial.add(new RegistroComida(this.id, comidaId, fecha, (int) calorias));
            }
        } catch (Exception e) {
            System.err.println("Error al obtener historial: " + e.getMessage());
        }
        return historial;
    }

    public List<Comida> obtenerTodasLasComidas() {
        List<Comida> comidas = new ArrayList<>();
        try (var session = Neo4JConnect.getInstance().getSession()) {
            var result = session.run("""
                MATCH (c:Food)
                OPTIONAL MATCH (c)-[:TIENE_PREFERENCIA]->(p:Preference)
                RETURN c, collect(p.type) AS preferencias
            """);

            while (result.hasNext()) {
                var record = result.next();
                var node = record.get("c").asNode();

                Set<String> preferencias = new HashSet<>();
                for (var pref : record.get("preferencias").values()) {
                    preferencias.add(pref.asString());
                }

                Comida comida = new Comida(
                    node.get("id").asString(),
                    node.get("name").asString(),
                    node.get("calories").asInt(),
                    node.get("protein").asDouble(),
                    node.get("carbs").asDouble(),
                    node.get("fats").asDouble(),
                    node.get("type").asString(),
                    node.get("price").asDouble(),
                    preferencias,
                    node.get("imagePath").asString(),
                    node.get("receta").asString()
                );
                comidas.add(comida);
            }
        }
        return comidas;
    }
}
