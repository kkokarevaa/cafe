package com.example.cafe.cafe;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Данные для подключения к базе данных
    private final String LOGIN = "root"; // Логин пользователя базы данных
    private final String PASS = ""; // Пароль пользователя базы данных (пустой по умолчанию)
    private final String DBNAME = "cafe"; // Имя базы данных
    private final String IP = "localhost"; // Адрес сервера базы данных

    /**
     * Метод для установки соединения с базой данных.
     */
    public Connection connect() {
        // Формируем URL для подключения к MySQL
        String url = "jdbc:mysql://" + IP + ":3306/" + DBNAME + "?useSSL=false";
        Connection conn = null;
        try {
            // Загружаем драйвер MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Устанавливаем соединение с базой данных
            conn = DriverManager.getConnection(url, LOGIN, PASS);
            System.out.println("Подключение к базе данных успешно выполнено.");
        } catch (ClassNotFoundException e) {
            // Обработка ошибки отсутствия драйвера
            System.err.println("Драйвер базы данных не найден: " + e.getMessage());
        } catch (SQLException e) {
            // Обработка ошибки подключения к базе данных
            System.err.println("Ошибка подключения к базе данных: " + e.getMessage());
        }
        return conn;
    }

    /**
     * Метод для закрытия соединения с базой данных.
     */
    public void disconnect(Connection conn) {
        if (conn != null) {
            try {
                conn.close(); // Закрываем соединение
                System.out.println("Соединение с базой данных закрыто.");
            } catch (SQLException e) {
                // Обработка ошибки при закрытии соединения
                System.err.println("Ошибка при закрытии соединения с базой данных: " + e.getMessage());
            }
        }
    }
}