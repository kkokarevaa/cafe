package com.example.cafe.cafe;

import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class regController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button registerButton;

    @FXML
    private Button login_href;

    private DatabaseConnection databaseConnection;

    @FXML
    public void initialize() {
        databaseConnection = new DatabaseConnection();
        registerButton.setOnAction(event -> handleRegister());
        login_href.setOnAction(event -> openLoginWindow());
    }

    /**
     * Обрабатывает процесс регистрации пользователя
     */
    private void handleRegister() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Проверка на пустоту полей
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            AlertManager.showWarningAlert("Ошибка", "Пожалуйста, заполните все поля.");
            return;
        }

        // Проверка длины логина и пароля
        if (username.length() > 19 || password.length() > 19 || confirmPassword.length() > 19) {
            AlertManager.showWarningAlert("Ошибка", "Длина имени, логина и пароля не должна превышать 19 символов.");
            return;
        }

        // Проверка совпадения паролей
        if (!password.equals(confirmPassword)) {
            AlertManager.showWarningAlert("Ошибка", "Пароли не совпадают!");
            return;
        }

        // Проверка существования пользователя с таким логином
        if (isUsernameTaken(username)) {
            AlertManager.showWarningAlert("Ошибка", "Пользователь с таким логином уже существует.");
            return;
        }

        // Регистрация нового пользователя
        try (Connection conn = databaseConnection.connect()) {
            String query = "INSERT INTO Users (username, password, role_id) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                stmt.setInt(3, 1); // Устанавливаем стандартную роль
                stmt.executeUpdate();
                AlertManager.showInfoAlert("Успешная регистрация", "Добро пожаловать!");

                // Открываем окно логина
                openLoginWindow();

                // Закрываем текущее окно регистрации
                ((Stage) registerButton.getScene().getWindow()).close();
            }
        } catch (SQLException e) {
            AlertManager.showErrorAlert("Ошибка регистрации", "Ошибка при регистрации: " + e.getMessage());
        }
    }

    /**
     * Проверяет, существует ли пользователь с таким логином
     * @param username логин пользователя
     * @return true если пользователь уже существует, false если нет
     */
    private boolean isUsernameTaken(String username) {
        String query = "SELECT username FROM Users WHERE username = ?";
        try (Connection conn = databaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // Если существует хотя бы одна запись, логин уже занят
            }
        } catch (SQLException e) {
            AlertManager.showErrorAlert("Ошибка базы данных", "Ошибка при проверке логина: " + e.getMessage());
            return false;
        }
    }

    /**
     * Отображает диалоговое окно подтверждения выхода
     * @param stage Текущее окно
     */
    private void showExitConfirmation(Stage stage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение выхода");
        alert.setHeaderText("Вы действительно хотите выйти?");
        alert.setContentText("Выберите действие:");

        ButtonType buttonYes = new ButtonType("ОК");
        ButtonType buttonNo = new ButtonType("Отмена");

        alert.getButtonTypes().setAll(buttonYes, buttonNo);

        alert.showAndWait().ifPresent(response -> {
            if (response == buttonYes) {
                stage.close();
            }
        });
    }

    /**
     * Метод для открытия окна логина
     */
    private void openLoginWindow() {
        ((Stage) registerButton.getScene().getWindow()).close();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml")); // Убедитесь, что путь к файлу правильный
            Parent root = loader.load();
            Stage loginStage = new Stage();
            loginStage.setScene(new Scene(root));
            loginStage.setTitle("Вход");

            // Добавляем обработчик закрытия окна
            loginStage.setOnCloseRequest(event -> {
                event.consume(); // Отменяем стандартное закрытие
                showExitConfirmation(loginStage);
            });

            loginStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}