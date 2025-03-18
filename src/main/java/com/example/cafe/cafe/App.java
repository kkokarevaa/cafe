package com.example.cafe.cafe;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Загружаем макет интерфейса из файла login.fxml
        AnchorPane root = FXMLLoader.load(getClass().getResource("login.fxml"));

        // Создаем сцену с размерами 600x400 пикселей
        Scene scene = new Scene(root, 600, 400);

        // Добавляем таблицу стилей для оформления сцены
        scene.getStylesheets().add(getClass().getResource("css/login.css").toExternalForm());

        // Устанавливаем заголовок окна
        primaryStage.setTitle("Вход");

        // Назначаем сцену главному окну приложения
        primaryStage.setScene(scene);

        // Добавляем обработчик закрытия окна, чтобы запросить подтверждение выхода
        primaryStage.setOnCloseRequest(event -> {
            event.consume(); // Отменяем стандартное закрытие окна
            showExitConfirmation(primaryStage); // Вызываем метод для отображения диалогового окна
        });

        // Отображаем главное окно приложения
        primaryStage.show();
    }

    /**
     * Метод отображает окно подтверждения выхода при попытке закрыть приложение.
     */
    private void showExitConfirmation(Stage stage) {
        // Создаем диалоговое окно подтверждения
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение выхода");
        alert.setHeaderText("Вы действительно хотите выйти?");
        alert.setContentText("Выберите действие:");

        // Создаем кнопки для подтверждения и отмены
        ButtonType buttonYes = new ButtonType("ОК");
        ButtonType buttonNo = new ButtonType("Отмена");

        // Добавляем кнопки в диалоговое окно
        alert.getButtonTypes().setAll(buttonYes, buttonNo);

        // Ожидаем выбор пользователя
        alert.showAndWait().ifPresent(response -> {
            if (response == buttonYes) {
                stage.close(); // Закрываем приложение, если нажата кнопка "ОК"
            }
        });
    }

    /**
     * Главный метод, с которого начинается выполнение приложения.
     */
    public static void main(String[] args) {
        launch(args); // Запускаем JavaFX-приложение
    }
}