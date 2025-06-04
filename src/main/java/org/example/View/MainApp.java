package org.example.View;

import javafx.application.Application;
import javafx.stage.Stage;
import org.example.View.LoginView;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Mostrar vista de login
        LoginView loginView = new LoginView();
        loginView.start(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}