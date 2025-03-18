package com.example.cafe.cafe;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;

import java.util.Optional;

public class AlertManager {

    /**
     * Отображает информационное уведомление.
     */
    public static void showInfoAlert(String title, String content) {
        showAlert(AlertType.INFORMATION, title, content);
    }

    /**
     * Отображает предупреждающее уведомление.
     */
    public static void showWarningAlert(String title, String content) {
        showAlert(AlertType.WARNING, title, content);
    }

    /**
     * Отображает уведомление об ошибке.
     */
    public static void showErrorAlert(String title, String content) {
        showAlert(AlertType.ERROR, title, content);
    }

    /**
     * Отображает диалог подтверждения и возвращает результат выбора пользователя.
     */
    public static boolean showConfirmationAlert(String title, String content) {
        Alert alert = createAlert(AlertType.CONFIRMATION, content);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Универсальный метод для отображения уведомления.
     */
    private static void showAlert(AlertType alertType, String title, String content) {
        Alert alert = createAlert(alertType, content);
        alert.showAndWait();
    }

    /**
     * Создаёт диалоговое окно с заданным типом и контентом, а также применяет стили.
     */
    private static Alert createAlert(AlertType alertType, String content) {
        Alert alert = new Alert(alertType);
        alert.setHeaderText(null); // Убираем стандартный заголовок
        alert.setContentText(content);

        // Основные стили для диалогового окна
        alert.getDialogPane().setStyle("""
                    -fx-background-color: #ffffff;
                    -fx-padding: 15px;
                """);

        // Стили текста контента
        alert.getDialogPane().lookup(".content.label").setStyle("""
                    -fx-text-fill: #000000;
                    -fx-font-size: 14px;
                    -fx-font-family: 'Tahoma';
                    -fx-font-weight: bold;
                """);

        // Стили для кнопок
        alert.getDialogPane().lookupButton(ButtonType.OK).setStyle("""
                    -fx-background-color: #ee232e;
                    -fx-text-fill: #ffffff;
                    -fx-font-size: 14px;
                    -fx-background-radius: 5px;
                    -fx-padding: 10px;
                """);

        // Обновление стилей с задержкой для гарантии их применения
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.getDialogPane().applyCss();

        return alert;
    }
}