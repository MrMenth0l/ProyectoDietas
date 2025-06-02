package org.example.View;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.Model.Comida;
import org.example.Model.Usuario;

import java.io.File;
import java.net.URL;
import java.util.List;

public class PreferenciasInicialesView {

    private int index = 0;
    private final List<Comida> platillos;
    private final Usuario usuario;
    private final Stage stage;

    public PreferenciasInicialesView(Stage stage, List<Comida> platillos, Usuario usuario) {
        this.stage = stage;
        this.platillos = platillos;
        this.usuario = usuario;
        mostrarSiguiente();
    }

    private void mostrarSiguiente() {
        if (index >= platillos.size()) {
            try (org.neo4j.driver.Session session = org.example.Model.Neo4JConnect.getInstance().getSession()) {
                for (Comida comida : usuario.getPreferenciasIniciales()) {
                    for (String pref : comida.getPreferencias()) {

                        session.writeTransaction(tx -> {
                            tx.run("""
                                MATCH (u:Usuario {correo: $correo})
                                MERGE (p:Preference {type: $type})
                                MERGE (u)-[r:PREFIERE_PREFERENCIA]->(p)
                                ON CREATE SET r.via = "preferencias_iniciales"
                            """, org.neo4j.driver.Values.parameters(
                                    "correo", usuario.getCorreo(),
                                    "type", pref
                            ));
                            return null;
                        });
                    }
                }
            }
            // Redirigir al panel principal
            new PanelPrincipalView(usuario).start(stage);
            return;
        }

        Comida comida = platillos.get(index);

        Label titulo = new Label(comida.getName());
        titulo.getStyleClass().add("titulo-comida");

        String imageName = new File(comida.getImagePath()).getName();
        URL imageUrl = getClass().getResource("/images/" + imageName);
        Image imagen;
        if (imageUrl != null) {
            imagen = new Image(imageUrl.toExternalForm());
        } else {
            System.err.println("No se encontró la imagen: /images/" + imageName);
            imagen = new Image("https://via.placeholder.com/300x200.png?text=Imagen+no+encontrada");
        }
        ImageView imageView = new ImageView(imagen);
        imageView.setFitWidth(300);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.getStyleClass().add("imagen-comida");

        Label macros = new Label(
            String.format("Calorías: %d kcal | Proteínas: %.1f g | Carbs: %.1f g | Grasas: %.1f g",
                comida.getCalorias(), comida.getProtein(), comida.getCarbs(), comida.getFats())
        );
        macros.getStyleClass().add("macro-label");
        macros.getStyleClass().add("calorias-label");
        macros.setAlignment(Pos.CENTER);

        Label recetaTitulo = new Label("Receta:");
        recetaTitulo.getStyleClass().add("section-header");
        recetaTitulo.setAlignment(Pos.CENTER);
        recetaTitulo.setStyle("-fx-alignment: center;");

        Label receta = new Label(comida.getReceta());
        receta.setWrapText(true);
        receta.setMaxWidth(350);
        receta.getStyleClass().add("receta-label");
        receta.setAlignment(Pos.CENTER);
        receta.setStyle("-fx-alignment: center;");

        Button btnSi = new Button("✅ Lo comería");
        Button btnNo = new Button("❌ No lo comería");

        btnSi.getStyleClass().add("voto-button-positivo");
        btnNo.getStyleClass().add("voto-button-negativo");

        btnSi.setOnAction(e -> {
            usuario.agregarPreferenciaInicial(comida);
            try (org.neo4j.driver.Session session = org.example.Model.Neo4JConnect.getInstance().getSession()) {
                // Registrar la relación CALIFICO con puntuación 1
                org.example.Model.RegistroComida.registrarPuntuacionEnNeo4j(usuario.getCorreo(), comida.getId(), 1, session);
                // Actualizar acumuladores en el nodo Food
                session.writeTransaction(tx -> {
                    tx.run("""
                        MATCH (f:Food {id: $id})
                        SET f.totalVotos = coalesce(f.totalVotos, 0) + 1,
                            f.sumaPuntuaciones = coalesce(f.sumaPuntuaciones, 0) + 1
                    """, org.neo4j.driver.Values.parameters("id", comida.getId()));
                    return null;
                });
                // Registrar localmente el voto
                comida.registrarPuntuacion(1);
                usuario.agregarPuntuacion(new org.example.Model.RegistroPuntuacion(usuario.getCorreo(), comida.getId(), 1, java.time.LocalDate.now()));
            }
            index++;
            mostrarSiguiente();
        });

        btnNo.setOnAction(e -> {
            try (org.neo4j.driver.Session session = org.example.Model.Neo4JConnect.getInstance().getSession()) {
                // Registrar la relación CALIFICO en la base de datos con puntuación 0
                org.example.Model.RegistroComida.registrarPuntuacionEnNeo4j(usuario.getCorreo(), comida.getId(), 0, session);
                // Actualizar totalVotos y sumaPuntuaciones en el nodo Food
                session.writeTransaction(tx -> {
                    tx.run("""
                        MATCH (f:Food {id: $id})
                        SET f.totalVotos = coalesce(f.totalVotos, 0) + 1,
                            f.sumaPuntuaciones = coalesce(f.sumaPuntuaciones, 0) + 0
                    """, org.neo4j.driver.Values.parameters("id", comida.getId()));
                    return null;
                });
                // Registrar localmente el voto
                comida.registrarPuntuacion(0);
                usuario.agregarPuntuacion(new org.example.Model.RegistroPuntuacion(usuario.getCorreo(), comida.getId(), 0, java.time.LocalDate.now()));
            }
            index++;
            mostrarSiguiente();
        });

        HBox buttonBox = new HBox(20, btnSi, btnNo);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getStyleClass().add("voto-buttons");

        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("card-comida");

        // Configuración visual uniforme y límites de tamaño
        root.setPrefHeight(600);
        root.setPrefWidth(500);
        titulo.setMaxWidth(480);
        titulo.setWrapText(true);
        imageView.setFitHeight(200); // asegura altura fija para la imagen
        macros.setMaxWidth(480);
        recetaTitulo.setMaxWidth(480);
        receta.setPrefHeight(100); // fija altura para la receta
        receta.setMaxWidth(480);
        root.getChildren().addAll(titulo, imageView, macros, recetaTitulo, receta, buttonBox);

        Scene scene = new Scene(root, 500, 600);
        scene.getStylesheets().add("estilos.css"); // Aplica tu estilo general
        stage.setScene(scene);
        stage.show();
    }
}