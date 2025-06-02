package org.example.Model;

import java.time.LocalDate;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;


public class RecomendadorAlgo {

    // Datos del usuario
    private Usuario usuario;

    // Base de datos de comidas y sus puntuaciones
    private List<Comida> baseDeComidas;

    // Historial de comidas registradas (pasadas)
    private List<RegistroComida> historial;

    public RecomendadorAlgo(Usuario usuario, List<RegistroComida> historial) {
        this.usuario = usuario;
        this.baseDeComidas = ComidaService.getTodas();
        this.historial = historial;
    }

    public static List<Comida> recomendar(Usuario usuario, List<RegistroComida> historial, int cantidad) {
        double promedioCaloriasHistorico = historial.stream()
            .filter(r -> r.getUsuarioId().equals(usuario.getId()))
            .mapToDouble(RegistroComida::getCalorias)
            .average()
            .orElse(0);

        String tipoRecomendado;
        int hora = java.time.LocalTime.now().getHour();
        if (hora < 11) {
            tipoRecomendado = "Desayuno";
        } else if (hora < 17) {
            tipoRecomendado = "Almuerzo";
        } else {
            tipoRecomendado = "Cena";
        }

        List<Comida> comidasFiltradas = new ArrayList<>(ComidaService.getTodas().stream()
            .filter(c -> c.cumplePreferencias(usuario) && c.getType().equalsIgnoreCase(tipoRecomendado))
            .toList());


        if (comidasFiltradas.stream().allMatch(c -> c.getPuntuacion() == 0)) {
            Collections.shuffle(comidasFiltradas);
        }

        return comidasFiltradas.stream()
            .sorted((c1, c2) -> {
                int cmp = Integer.compare(c2.getPuntuacion(), c1.getPuntuacion());
                if (cmp == 0) {
                    return Double.compare(
                        Math.abs(c1.getCalorias() - promedioCaloriasHistorico),
                        Math.abs(c2.getCalorias() - promedioCaloriasHistorico)
                    );
                }
                return cmp;
            })
            .limit(cantidad)
            .toList();
    }

    public void registrarPreferenciaInicial(Comida comida, RespuestaUsuario respuesta) {
        switch (respuesta) {
            case NO_LO_COMERIA -> comida.ajustarPuntuacion(-1);
            case LO_COMERIA -> comida.ajustarPuntuacion(+1);
            case LO_COMERE_AHORITA -> {
                comida.ajustarPuntuacion(+2);
                registrarConsumo(comida);
            }
        }
    }

    public void recomendarComidaPorMomentoDelDia(MomentoDelDia momento) {
        double promedioCaloriasHistorico = calcularPromedioCaloricoHistorial();

        List<Comida> recomendadas = baseDeComidas.stream()
            .filter(c -> c.cumplePreferencias(usuario))
            .sorted((c1, c2) -> {
                int cmp = Integer.compare(c2.getPuntuacion(), c1.getPuntuacion());
                if (cmp == 0) {
                    // Si tienen la misma puntuación, ordenar por proximidad al promedio calórico
                    return Double.compare(
                        Math.abs(c1.getCalorias() - promedioCaloriasHistorico),
                        Math.abs(c2.getCalorias() - promedioCaloriasHistorico)
                    );
                }
                return cmp;
            })
            .limit(3)
            .toList();

        mostrarOpciones(recomendadas);
    }

    private void registrarConsumo(Comida comida) {
        historial.add(new RegistroComida(usuario.getId(), comida.getId(), LocalDate.now(), comida.getCalorias()));
    }

    private double calcularPromedioCaloricoHistorial() {
        return historial.stream()
            .filter(r -> r.getUsuarioId().equals(usuario.getId()))
            .mapToDouble(RegistroComida::getCalorias)
            .average()
            .orElse(0);
    }

    private void mostrarOpciones(List<Comida> comidas) {
        System.out.println("Comidas recomendadas:");
        for (Comida c : comidas) {
            System.out.println("• " + c.getNombre() + " - " + c.getCalorias() + " kcal - score: " + c.getPuntuacion());
        }
    }
}