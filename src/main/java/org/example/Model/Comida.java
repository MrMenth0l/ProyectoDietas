package org.example.Model;// Commit Samuel 

import java.util.Set;

public class Comida  {
    private String id;
    private String nombre;
    private int calorias;
    private int puntuacion;
    private Set<String> preferencias;

    public void ajustarPuntuacion(int delta) {
        this.puntuacion += delta;
    }

    public boolean cumplePreferencias(Usuario u) {
        // lógica futura, por ahora retorna true
        return true;
    }

    public String getNombre() {
        return nombre;
    }

    public String getId() {
        return id;
    }

    public int getCalorias() {
        return calorias;
    }

    public int getPuntuacion() {
        return puntuacion;
    }

    public Set<String> getPreferencias() {
        return preferencias;
    }
}
