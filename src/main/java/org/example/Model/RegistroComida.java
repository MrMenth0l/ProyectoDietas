package org.example.Model;

import java.time.LocalDate;

public class RegistroComida {
    private String usuarioId;
    private String comidaId;
    private LocalDate fecha;
    private int calorias;

    public RegistroComida(String u, String c, LocalDate f, int kcal) {
        this.usuarioId = u;
        this.comidaId = c;
        this.fecha = f;
        this.calorias = kcal;
    }

    public String getComidaId() {
        return comidaId;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public int getCalorias() {
        return calorias;
    }
}
