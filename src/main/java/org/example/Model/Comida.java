package org.example.Model;

import java.util.HashSet;
import java.util.Set;

public class Comida  {
    private String id;
    private String name;
    private int calories;
    private double protein;
    private double carbs;
    private double fats;
    private String type;
    private double price;
    private Set<String> preferencias;
    private String imagePath;
    private String receta;
    private int totalVotos;
    private int sumaPuntuaciones;

    public Comida(String id, String name, int calories, double protein, double carbs, double fats,
                  String type, double price, Set<String> preferencias, String imagePath, String receta) {
        this.id = id;
        this.name = name;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fats = fats;
        this.type = type;
        this.price = price;
        this.preferencias = preferencias;
        this.imagePath = imagePath;
        this.receta = receta;
        this.totalVotos = 0;
        this.sumaPuntuaciones = 0;
    }

    public boolean cumplePreferencias(Usuario u) {
        Set<String> preferenciasUsuario = new HashSet<>();
        for (Comida comida : u.getPreferenciasIniciales()) {
            preferenciasUsuario.addAll(comida.getPreferencias());
        }

        if (preferenciasUsuario.isEmpty()) {
            return true;
        }

        for (String pref : this.preferencias) {
            if (preferenciasUsuario.contains(pref)) {
                return true;
            }
        }

        return false;
    }

    public void registrarPuntuacion(int puntuacion) {
        this.sumaPuntuaciones += puntuacion;
        this.totalVotos++;
    }

    public double getPuntuacionPromedio() {
        return totalVotos == 0 ? 0 : (double) sumaPuntuaciones / totalVotos;
    }

    public int getPuntuacionRedondeada() {
        return (int) Math.round(getPuntuacionPromedio());
    }

    public String getNombre() {
        return name;
    }

    public String getId() {
        return id;
    }

    public int getCalorias() {
        return calories;
    }

    public int getPuntuacion() {
        return sumaPuntuaciones;
    }

    public Set<String> getPreferencias() {
        return preferencias;
    }

    public String getName() {
        return name;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getReceta() {
        return receta;
    }

    public String getType() {
        return type;
    }

    public double getProtein() {
        return protein;
    }

    public double getCarbs() {
        return carbs;
    }

    public double getFats() {
        return fats;
    }

    public double getPrice() {
        return price;
    }

    public void setTotalVotos(int totalVotos) {
        this.totalVotos = totalVotos;
    }

    public void setSumaPuntuaciones(int sumaPuntuaciones) {
        this.sumaPuntuaciones = sumaPuntuaciones;
    }

    public void ajustarPuntuacion(int i) {
        this.sumaPuntuaciones += i;
        this.totalVotos++;

        try (org.neo4j.driver.Session session = Neo4JConnect.getInstance().getSession()) {
            session.writeTransaction(tx -> {
                tx.run("MATCH (c:Food {id: $id}) " +
                        "SET c.sumaPuntuaciones = coalesce(c.sumaPuntuaciones, 0) + $i, " +
                        "    c.totalVotos = coalesce(c.totalVotos, 0) + 1",
                        org.neo4j.driver.Values.parameters("id", this.id, "i", i));
                return null;
            });
        }
    }
}
