package com.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class LoginScreen extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;

        URL loginView = getClass().getResource("/views/login.fxml");

        if (loginView == null) {
            throw new IOException(
                    "Không tìm thấy giao diện /views/login.fxml"
            );
        }

        FXMLLoader loader = new FXMLLoader(loginView);
        Parent root = loader.load();

        Scene scene = new Scene(root);

        stage.setTitle("Quản lý phòng mạch tư — Demo giao diện");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.centerOnScreen();
        stage.show();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}