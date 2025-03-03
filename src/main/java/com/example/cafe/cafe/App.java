package com.example.cafe.cafe;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        AnchorPane root = FXMLLoader.load(getClass().getResource("login.fxml"));
        Scene scene = new Scene(root, 600, 400);
        scene.getStylesheets().add(getClass().getResource("css/login.css").toExternalForm());

        primaryStage.setTitle("Вход");

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
