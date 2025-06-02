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
import org.example.Model.*;
import org.neo4j.driver.Session;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PanelPrincipalView {

    private final Usuario usuario;
    private Comida comidaActual;
    private VBox root;
    private Stage stage;
    private final Set<String> comidasVistas = new HashSet<>();
    private List<Comida> disponibles = new ArrayList<>();
    private int indiceActual = 0;

    public PanelPrincipalView(Usuario usuario) {
        this.usuario = usuario;
    }

    public void start(Stage stage) {
        this.stage = stage;
        root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPrefWidth(500);
        root.setPrefHeight(600);
        root.getStyleClass().add("card-comida");

        mostrarSiguienteComida();

        Scene scene = new Scene(root, 500, 600);
        scene.getStylesheets().add(getClass().getResource("/estilos.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Recomendaciones");
        stage.show();
    }

    private void mostrarSiguienteComida() {
        root.getChildren().clear();

        List<RegistroComida> historial = usuario.obtenerHistorial();

        if (disponibles == null) {
            disponibles = new ArrayList<>();
        }

        if (indiceActual >= disponibles.size()) {
            RecomendadorAlgo recomendador = new RecomendadorAlgo(usuario, historial);
            List<Comida> nuevas = recomendador.recomendar(usuario,historial,20);
            disponibles.addAll(nuevas);
        }

        comidaActual = null;
        while (indiceActual < disponibles.size()) {
            Comida c = disponibles.get(indiceActual);
            indiceActual++;
            if (!comidasVistas.contains(c.getId())) {
                comidaActual = c;
                comidasVistas.add(c.getId());
                break;
            }
        }

        if (comidaActual == null) {
            Label fin = new Label("¿Deseas ver más recomendaciones?");
            fin.getStyleClass().add("titulo-comida");

            Button si = new Button("Sí, muéstrame más");
            Button no = new Button("No por ahora");

            si.setOnAction(e -> {
                comidasVistas.clear();
                disponibles.clear();
                indiceActual = 0;
                mostrarSiguienteComida();
            });

            no.setOnAction(e -> {
                Label despedida = new Label("¡Vuelve pronto para más sugerencias!");
                root.getChildren().setAll(despedida);
            });

            HBox opciones = new HBox(20, si, no);
            opciones.setAlignment(Pos.CENTER);

            root.getChildren().addAll(fin, opciones);
            return;
        }

        Label titulo = new Label(comidaActual.getNombre());
        titulo.getStyleClass().add("titulo-comida");
        titulo.setWrapText(true);
        titulo.setMaxWidth(480);

        String imageName = new java.io.File(comidaActual.getImagePath()).getName();
        java.net.URL imageUrl = getClass().getResource("/images/" + imageName);
        Image imagen;
        if (imageUrl != null) {
            imagen = new Image(imageUrl.toExternalForm());
        } else {
            imagen = new Image("https://via.placeholder.com/300x200.png?text=Imagen+no+encontrada");
        }
        ImageView imageView = new ImageView(imagen);
        imageView.setFitWidth(300);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.getStyleClass().add("imagen-comida");

        Label macros = new Label(
            String.format("Calorías: %d kcal | Proteínas: %.1f g | Carbs: %.1f g | Grasas: %.1f g",
                comidaActual.getCalorias(), comidaActual.getProtein(), comidaActual.getCarbs(), comidaActual.getFats())
        );
        macros.getStyleClass().add("macro-label");
        macros.getStyleClass().add("calorias-label");
        macros.setAlignment(Pos.CENTER);
        macros.setMaxWidth(480);

        Button btn0 = new Button("❌ No lo comería");
        Button btn1 = new Button("✅ Lo comería");
        Button btn2 = new Button("🍽️ Lo comeré ahorita");

        btn0.setMinWidth(160);
        btn0.setWrapText(true);
        btn0.setMinHeight(60);

        btn1.setMinWidth(160);
        btn1.setWrapText(true);
        btn1.setMinHeight(60);

        btn2.setMinWidth(160);
        btn2.setWrapText(true);
        btn2.setMinHeight(60);

        btn0.getStyleClass().add("voto-button-negativo");
        btn1.getStyleClass().add("voto-button-positivo");
        btn2.getStyleClass().add("voto-button-ahorita");

        btn0.setOnAction(e -> votar(0));
        btn1.setOnAction(e -> votar(1));
        btn2.setOnAction(e -> votar(2));

        HBox buttonBox = new HBox(20, btn0, btn2, btn1);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getStyleClass().add("voto-buttons");

        HBox logoutBox = new HBox();
        logoutBox.setAlignment(Pos.BOTTOM_RIGHT);
        logoutBox.setPrefWidth(500);
        Button logout = new Button("Cerrar sesión");
        logout.setOnAction(e -> new LoginView().start(stage));
        logout.getStyleClass().add("boton-logout");
        logoutBox.getChildren().add(logout);

        root.getChildren().addAll(titulo, imageView, macros, buttonBox, logoutBox);
    }

    private void votar(int valor) {
        try (Session session = Neo4JConnect.getInstance().getSession()) {
            session.writeTransaction(tx -> {
                tx.run("""
                    MATCH (u:Usuario {correo: $correo}), (f:Food {id: $id})
                    MERGE (u)-[r:CALIFICO]->(f)
                    SET r.valor = $valor,
                        f.totalVotos = coalesce(f.totalVotos, 0) + 1,
                        f.sumaPuntuaciones = coalesce(f.sumaPuntuaciones, 0) + $valor
                    """, org.neo4j.driver.Values.parameters(
                        "correo", usuario.getCorreo(),
                        "id", comidaActual.getId(),
                        "valor", valor));
                return null;
            });
        }

        if (valor == 2) {
            try (Session session = Neo4JConnect.getInstance().getSession()) {
                session.writeTransaction(tx -> {
                    tx.run("""
                        MATCH (u:Usuario {correo: $correo}), (f:Food {id: $id})
                        MERGE (u)-[:CONSUMIO {fechaHora: datetime()}]->(f)
                    """, org.neo4j.driver.Values.parameters(
                        "correo", usuario.getCorreo(),
                        "id", comidaActual.getId()
                    ));
                    return null;
                });
            }
            mostrarReceta(comidaActual);
        } else {
            mostrarSiguienteComida();
        }
    }

    private void mostrarReceta(Comida comida) {
        root.getChildren().clear();

        Label titulo = new Label("Receta de " + comida.getNombre());
        titulo.getStyleClass().add("titulo-comida");
        titulo.setWrapText(true);
        titulo.setMaxWidth(480);

        Label receta = new Label(comida.getReceta());
        receta.setWrapText(true);
        receta.setPrefHeight(100);
        receta.setMaxWidth(480);
        receta.getStyleClass().add("receta-label");
        receta.setAlignment(Pos.CENTER);

        Button volver = new Button("Ver más recomendaciones");
        volver.setOnAction(e -> mostrarSiguienteComida());

        VBox contenedor = new VBox(15, titulo, receta, volver);
        contenedor.setAlignment(Pos.CENTER);
        contenedor.setPrefWidth(500);
        contenedor.setPrefHeight(600);
        contenedor.getStyleClass().add("card-comida");

        root.getChildren().setAll(contenedor);
    }
}