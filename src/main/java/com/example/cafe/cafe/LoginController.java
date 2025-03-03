package com.example.cafe.cafe;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    private DatabaseConnection dbConnection;
    private int userId = -1;
    private String userRole;

    @FXML
    public void initialize() {
        dbConnection = new DatabaseConnection();
        setupButtonActions();
    }

    private void setupButtonActions() {
        loginButton.setOnAction(event -> authenticateUser());
        registerButton.setOnAction(event -> showRegistrationWindow());
    }

    private void showRegistrationWindow() {
        try {
            loadStage("register.fxml", "Регистрация");
            closeCurrentStage();
        } catch (IOException e) {
            AlertManager.showInfoAlert("Ошибка открытия окна регистрации", "Не удалось открыть окно регистрации.");
            e.printStackTrace();
        }
    }

    private void authenticateUser() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            AlertManager.showInfoAlert("Поля пусты", "Пожалуйста, заполните все поля.");
            return;
        }

        userId = authenticateCredentials(username, password);

        if (userId != -1) {
            navigateToMainPage();
        } else {
            AlertManager.showInfoAlert("Ошибка входа", "Неверный логин или пароль.");
        }
    }

    private int authenticateCredentials(String username, String password) {
        String query = "SELECT u.user_id, r.role_name FROM users u JOIN roles r ON u.role_id = r.role_id WHERE u.username = ? AND u.password = ?";

        try (Connection conn = dbConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                userId = rs.getInt("user_id");
                userRole = rs.getString("role_name");
            }
        } catch (SQLException e) {
            AlertManager.showInfoAlert("Ошибка базы данных", "Ошибка при проверке учетных данных.");
            e.printStackTrace();
        }
        return userId;
    }

    private void navigateToMainPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("shop.fxml"));
            Parent root = loader.load();

            ShopController shopController = loader.getController();
            shopController.setUserId(userId, userRole);

            Stage stage = setupStage(root, "Моя страница", 1154, 714);
            stage.show();
        } catch (IOException e) {
            AlertManager.showInfoAlert("Ошибка перехода", "Не удалось открыть основное окно.");
            e.printStackTrace();
        }
    }

    private Stage setupStage(Parent root, String title, double width, double height) {
        Stage stage = (Stage) loginButton.getScene().getWindow();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("css/shop.css").toExternalForm());
        stage.setScene(scene);
        stage.setWidth(width);
        stage.setHeight(height);
        stage.setResizable(false);
        centerStage(stage);
        stage.setTitle(title);
        return stage;
    }

    private void centerStage(Stage stage) {
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        stage.setX(screenBounds.getMinX() + (screenBounds.getWidth() - stage.getWidth()) / 2);
        stage.setY(screenBounds.getMinY() + (screenBounds.getHeight() - stage.getHeight()) / 2);
    }

    private void closeCurrentStage() {
        Stage currentStage = (Stage) registerButton.getScene().getWindow();
        currentStage.close();
    }

    private void loadStage(String fxmlFile, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.show();
    }
}
