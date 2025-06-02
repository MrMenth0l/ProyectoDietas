package org.example.Model;

import java.time.LocalDate;

public class RegistroPuntuacion {
    private String idUsuario;
    private String idComida;
    private int puntuacion;
    private LocalDate fecha;

    public RegistroPuntuacion(String idUsuario, String idComida, int puntuacion, LocalDate fecha) {
        this.idUsuario = idUsuario;
        this.idComida = idComida;
        this.puntuacion = puntuacion;
        this.fecha = fecha;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public String getIdComida() {
        return idComida;
    }

    public int getPuntuacion() {
        return puntuacion;
    }

    public LocalDate getFecha() {
        return fecha;
    }
}
