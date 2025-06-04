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

    public static void registrarPuntuacionEnNeo4j(String correoUsuario, String comidaId, int puntuacion, org.neo4j.driver.Session session) {
        session.writeTransaction(tx -> {
            tx.run("""
                MATCH (u:Usuario {correo: $correo})
                MATCH (f:Food {id: $comidaId})
                MERGE (u)-[r:CALIFICO]->(f)
                SET r.puntuacion = $puntuacion, r.fecha = date()
            """, org.neo4j.driver.Values.parameters(
                "correo", correoUsuario,
                "comidaId", comidaId,
                "puntuacion", puntuacion
            ));
            return null;
        });
    }
}
