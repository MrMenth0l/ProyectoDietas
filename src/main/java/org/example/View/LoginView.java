package org.example.View;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.Model.Comida;
import org.example.Model.Neo4JConnect;
import org.example.Model.Usuario;
import org.example.Model.ComidaService;
import org.example.View.PreferenciasInicialesView;
import org.mindrot.jbcrypt.BCrypt;
import org.neo4j.driver.Session;

import java.util.Map;
import java.util.List;

public class LoginView {

    private final Neo4JConnect connector = new Neo4JConnect("bolt://localhost:7687", "Neo4j", "Estructuras123");

    public void start(Stage stage) {
        stage.setTitle("Iniciar Sesión");

        TextField correoField = new TextField();
        correoField.setPromptText("Correo electrónico");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Contraseña");
        Button loginBtn = new Button("Iniciar Sesión");
        loginBtn.getStyleClass().add("button");
        Button irARegistro = new Button("Registrarse");
        irARegistro.getStyleClass().add("button-secundario");
        Label mensaje = new Label();
        mensaje.setId("mensaje-label");

        loginBtn.setOnAction(e -> {
            String correo = correoField.getText().trim();
            String pass = passwordField.getText().trim();

            if (correo.isEmpty() || pass.isEmpty()) {
                mensaje.setText("Completa todos los campos.");
                return;
            }

            if (verificarCredenciales(correo, pass)) {
                try (Session session = connector.getSession()) {
                    boolean tienePreferencias = session.run(
                        "MATCH (u:Usuario {correo: $correo}) " +
                        "RETURN EXISTS { MATCH (u)-[:PREFIERE_PREFERENCIA]->(:Preference) } AS tiene",
                        Map.of("correo", correo)
                    ).single().get("tiene").asBoolean();

                    if (tienePreferencias) {
                        new PanelPrincipalView(new Usuario(correo, null)).start(stage);
                    } else {
                        List<Comida> comidas = ComidaService.obtenerMuestrasIniciales();
                        new PreferenciasInicialesView(stage, comidas, new Usuario(correo, null));
                    }
                }
            } else {
                mensaje.setText("Credenciales incorrectas.");
            }
        });

        irARegistro.setOnAction(e -> new RegistroView().start(stage));

        Label header = new Label("Iniciar Sesión");
        header.getStyleClass().add("titulo");

        VBox formulario = new VBox(12);
        formulario.setPadding(new Insets(32));
        formulario.setAlignment(Pos.CENTER);
        formulario.setFillWidth(true);
        formulario.getStyleClass().add("form-card");
        formulario.getChildren().addAll(
            correoField,
            passwordField,
            loginBtn,
            irARegistro,
            mensaje
        );

        VBox root = new VBox(28);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("form-background");
        root.getChildren().addAll(header, formulario);

        Scene scene = new Scene(root, 450, 500);
        scene.getStylesheets().add(getClass().getResource("/estilos.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    private boolean verificarCredenciales(String correo, String password) {
        try (Session session = connector.getSession()) {
            var result = session.run("MATCH (u:Usuario {correo: $correo}) RETURN u.password AS hash",
                    Map.of("correo", correo));

            if (result.hasNext()) {
                String storedHash = result.single().get("hash").asString();
                return BCrypt.checkpw(password, storedHash);
            }
            return false;
        }
    }
}