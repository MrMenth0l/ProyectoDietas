package org.example.View;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.Model.Neo4JConnect;
import org.example.Model.Usuario;
import org.mindrot.jbcrypt.BCrypt;
import org.neo4j.driver.Session;

import java.util.Map;

public class RegistroView {

    private final Neo4JConnect connector = new Neo4JConnect("bolt://localhost:7687", "Neo4j", "Estructuras123");

    public void start(Stage stage) {
        TextField correoField = new TextField();
        PasswordField passwordField = new PasswordField();
        TextField nombreField = new TextField();
        TextField edadField = new TextField();
        ComboBox<String> generoCombo = new ComboBox<>();
        TextField alturaField = new TextField();
        TextField pesoField = new TextField();
        Button registrarBtn = new Button("Registrarse");
        Label mensaje = new Label();

        correoField.setPromptText("Correo electrónico");
        passwordField.setPromptText("Contraseña");
        nombreField.setPromptText("Nombre completo");
        edadField.setPromptText("Edad");
        generoCombo.setPromptText("Género");
        generoCombo.setMaxWidth(Double.MAX_VALUE);
        alturaField.setPromptText("Altura (m)");
        pesoField.setPromptText("Peso (kg)");

        generoCombo.getItems().addAll("Masculino", "Femenino", "Otro");

        registrarBtn.setOnAction(e -> {
            String correo = correoField.getText().trim();
            String pass = passwordField.getText().trim();
            String nombre = nombreField.getText().trim();
            String edad = edadField.getText().trim();
            String genero = generoCombo.getValue();
            String altura = alturaField.getText().trim();
            String peso = pesoField.getText().trim();

            if (correo.isEmpty() || pass.isEmpty() || nombre.isEmpty() || edad.isEmpty() || genero == null || altura.isEmpty() || peso.isEmpty()) {
                mensaje.setText("Por favor completa todos los campos.");
                return;
            }

            // Encriptar contraseña con bcrypt
            String hashed = BCrypt.hashpw(pass, BCrypt.gensalt());
            Usuario usuario = new Usuario(correo, hashed, nombre, Integer.parseInt(edad), genero, Double.parseDouble(altura), Double.parseDouble(peso));

            boolean exito = registrarUsuario(usuario);

            if (exito) {
                mensaje.setText("¡Usuario registrado exitosamente!");
                correoField.clear();
                passwordField.clear();
                nombreField.clear();
                edadField.clear();
                generoCombo.setValue(null);
                alturaField.clear();
                pesoField.clear();
                new LoginView().start(stage);
            } else {
                mensaje.setText("El correo ya está registrado.");
            }
        });

        // Crear header
        Label header = new Label("Registro");
        header.getStyleClass().add("titulo");

        // Crear formulario VBox
        VBox formulario = new VBox(12);
        formulario.setPadding(new Insets(32, 32, 32, 32));
        formulario.getStyleClass().add("form-card");
        formulario.setFillWidth(true);
        mensaje.setId("mensaje-label");
        formulario.getChildren().addAll(
                correoField,
                passwordField,
                nombreField,
                edadField,
                generoCombo,
                alturaField,
                pesoField,
                registrarBtn,
                mensaje
        );

        // VBox raíz para centrar verticalmente
        VBox root = new VBox(28);
        root.setAlignment(Pos.CENTER);
        root.getChildren().addAll(header, formulario);
        root.getStyleClass().add("form-background");

        Scene scene = new Scene(root, 450, 600);
        scene.getStylesheets().add(getClass().getResource("/estilos.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    private boolean registrarUsuario(Usuario usuario) {
        try (Session session = connector.getSession()) {
            var result = session.run("MATCH (u:Usuario {correo: $correo}) RETURN u",
                    Map.of("correo", usuario.getCorreo()));

            if (result.hasNext()) {
                return false; // ya existe
            }

            session.run("CREATE (u:Usuario {correo: $correo, nombre: $nombre, edad: $edad, genero: $genero, altura: $altura, peso: $peso, password: $password})",
                    Map.of(
                            "correo", usuario.getCorreo(),
                            "nombre", usuario.getNombre(),
                            "edad", usuario.getEdad(),
                            "genero", usuario.getGenero(),
                            "altura", usuario.getAltura(),
                            "peso", usuario.getPeso(),
                            "password", usuario.getPassword()
                    ));
            return true;
        }
    }
}