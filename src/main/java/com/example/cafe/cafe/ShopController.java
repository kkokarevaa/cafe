
package com.example.cafe.cafe;
//ctrl + A
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ShopController {
    @FXML
    private FlowPane cardsContainer; // Контейнер для карточек напитков
    @FXML
    private TextField searchField; // Поле для поиска
    @FXML
    private Button settingsButton; // Кнопка настроек
    @FXML
    private Button cartButton; // Кнопка корзины
    @FXML
    private Button editButton; // Кнопка редактирования

    private List<Drink> drinks; // Список напитков
    private final DatabaseConnection databaseConnection = new DatabaseConnection(); // Подключение к базе данных
    private ObservableList<ScheduleItem> scheduleItems = FXCollections.observableArrayList(); // Список элементов расписания

    private Cart cart = new Cart(); // Корзина

    private int userId; // Идентификатор пользователя
    private String userRole; // Роль пользователя

    public void setUserId(int userId, String userRole) {
        this.userId = userId;
        this.userRole = userRole;

        // Показывать или скрывать кнопки в зависимости от роли пользователя
        if ("Сотрудник".equals(userRole)) {
            settingsButton.setVisible(true);
            cartButton.setVisible(true);
            editButton.setVisible(true); // Добавлена кнопка "редактор"
        } else {
            settingsButton.setVisible(false);
            cartButton.setVisible(false);
            editButton.setVisible(false); // Добавлена кнопка "редактор"
        }

        try (Connection conn = databaseConnection.connect()) {
            drinks = getDrinks(conn); // Загрузка напитков из базы данных
        } catch (Exception e) {
            System.err.println("Ошибка при подключении к базе данных: " + e.getMessage());
            drinks = new ArrayList<>();
        }
        cardsContainer.setAlignment(Pos.CENTER);
        loadCards(""); // Загрузка карточек напитков

        // Динамический поиск: обновление при каждом вводе символа
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            loadCards(newValue.toLowerCase());
        });

        settingsButton.setOnAction(e -> openScheduleSettings()); // Открытие настроек расписания
        editButton.setOnAction(e -> openDrinkEditor()); // Открытие редактора напитков
        cartButton.setOnAction(e -> openCartView()); // Открытие корзины
    }

    @FXML
    public void initialize() {
        // Инициализация контроллера
    }

    /* ================= ЗАГРУЗКА ДАННЫХ ================= */

    // Загружает список напитков из базы данных
    private List<Drink> getDrinks(Connection conn) {
        List<Drink> drinks = new ArrayList<>();

        // Проверяем, что соединение не равно null, если нет — возвращаем пустой список
        if (conn == null) return drinks;

        try {
            // SQL-запрос: выбираем информацию о напитках, а также их рецепты, если они есть
            String sql = "SELECT d.drink_id, d.name, d.description, d.photo_url, d.price, r.recipe_text " +
                    "FROM drinks d LEFT JOIN recipes r ON d.drink_id = r.drink_id";

            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            // Обрабатываем результаты запроса
            while (rs.next()) {
                int id = rs.getInt("drink_id");          // ID напитка
                String name = rs.getString("name");      // Название напитка
                String description = rs.getString("description"); // Описание напитка
                String photoUrl = rs.getString("photo_url"); // URL изображения напитка
                double price = rs.getDouble("price");    // Цена напитка
                String recipe = rs.getString("recipe_text"); // Текст рецепта напитка

                // Получаем список ингредиентов для данного напитка
                List<Ingredient> ingredients = getIngredientsForDrink(id, conn);

                // Добавляем новый объект Drink в список
                drinks.add(new Drink(id, name, description, photoUrl, price, ingredients, recipe));
            }
        } catch (SQLException e) {
            // Выводим сообщение об ошибке в случае проблем с базой данных
            System.err.println("Ошибка при загрузке напитков: " + e.getMessage());
        }

        return drinks; // Возвращаем список напитков
    }

    // Загружаем список ингредиентов из базы данных
    private List<Ingredient> getIngredientsForDrink(int drinkId, Connection conn) {
        List<Ingredient> ingredients = new ArrayList<>();
        try {
            String sql = "SELECT ingredient_id, name, price " +
                    "FROM ingredients";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("ingredient_id");
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                ingredients.add(new Ingredient(id, name, price));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при загрузке ингредиентов: " + e.getMessage());
        }
        return ingredients;
    }

    // Цикл для создания всех карточек
    private void loadCards(String query) {
        cardsContainer.getChildren().clear();
        for (Drink drink : drinks) {
            if (drink.getName().toLowerCase().contains(query)) {
                cardsContainer.getChildren().add(createCard(drink)); // Создание карточки для напитка
            }
        }
    }

    // Создает карточку для отображения напитка в интерфейсе
    private VBox createCard(Drink drink) {
        VBox card = new VBox(10); // Создаем контейнер VBox с отступами в 10px между элементами
        card.setAlignment(Pos.CENTER); // Выравниваем содержимое по центру
        card.setMinWidth(1113); // Устанавливаем минимальную ширину карточки

        // Устанавливаем стиль для карточки (границы, отступы, тень и скругление углов)
        card.setStyle("-fx-border-color: #ddd; -fx-padding: 15; -fx-background-color: white; " +
                "-fx-border-radius: 10px; -fx-background-radius: 10px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 2);");

        // Создаем объект ImageView для отображения изображения напитка
        ImageView imageView = new ImageView();
        try {
            if (drink.getPhotoUrl().startsWith("http")) {
                // Загружаем изображение из интернета, если URL начинается с "http"
                Image image = new Image(drink.getPhotoUrl());
                imageView.setImage(image);
            } else {
                // Загружаем локальное изображение из папки "images"
                Image image = new Image(getClass().getResourceAsStream("images/" + drink.getPhotoUrl()));
                imageView.setImage(image);
            }
            imageView.setFitWidth(400); // Устанавливаем ширину изображения
            imageView.setStyle("-fx-border-radius: 20px"); // Скругляем углы изображения
            imageView.setPreserveRatio(true); // Сохраняем пропорции изображения
        } catch (Exception e) {
            System.out.println("Не удалось загрузить изображение: " + e.getMessage()); // Обработка ошибки загрузки
        }

        // Создаем метку с названием напитка
        Label name = new Label(drink.getName());
        name.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;"); // Делаем текст крупным и жирным

        // Метка с описанием напитка
        Label description = new Label(drink.getDescription());

        // Метка с ценой напитка
        Label price = new Label("Цена: " + drink.getPrice() + " руб.");

        // Кнопка "Рецепт" для отображения рецепта напитка
        Button recipeButton = new Button("Рецепт");
        recipeButton.setOnAction(e -> showRecipe(drink.getName(), drink.getRecipe())); // При нажатии открывается окно с рецептом

        // Кнопка "Добавить" для добавления напитка в корзину
        Button addButton = new Button("Добавить");
        addButton.setOnAction(e -> openAddToCartModal(drink)); // При нажатии открывается окно добавления в корзину

        // Добавляем все элементы в карточку
        card.getChildren().addAll(imageView, name, description, price, recipeButton, addButton);

        return card; // Возвращаем карточку
    }

    /* ================= ДЕЙСТВИЯ С КАРТОЧКАМИ НАПИТКА ================= */

    // Открывает модальное окно для выбора дополнительных ингредиентов перед добавлением напитка в корзину
    private void openAddToCartModal(Drink drink) {
        // Создаем новое модальное окно (Stage)
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL); // Устанавливаем модальность (блокирует основное окно)
        modalStage.setTitle("Добавить в корзину"); // Заголовок окна

        // Создаем основной контейнер (VBox) с отступами между элементами
        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER); // Выравниваем элементы по центру

        // Создаем заголовок с названием напитка
        Label title = new Label("Выберите ингредиенты для " + drink.getName());

        // Список для хранения чекбоксов с дополнительными ингредиентами
        List<CheckBox> ingredientCheckboxes = new ArrayList<>();

        // Перебираем ингредиенты напитка и создаем чекбоксы
        for (Ingredient ingredient : drink.getIngredients()) {
            CheckBox checkBox = new CheckBox(ingredient.getName() + " (+" + ingredient.getPrice() + " руб.)");
            ingredientCheckboxes.add(checkBox);
        }

        // Создаем кнопку "Добавить в корзину"
        Button addToCartButton = new Button("Добавить в корзину");
        addToCartButton.setOnAction(e -> {
            List<Ingredient> selectedIngredients = new ArrayList<>();

            // Проверяем, какие ингредиенты выбраны, и добавляем их в список
            for (int i = 0; i < ingredientCheckboxes.size(); i++) {
                if (ingredientCheckboxes.get(i).isSelected()) {
                    selectedIngredients.add(drink.getIngredients().get(i));
                }
            }

            // Добавляем напиток с выбранными ингредиентами в корзину
            cart.addItem(drink, selectedIngredients);

            // Закрываем модальное окно
            modalStage.close();
        });

        // Добавляем заголовок, чекбоксы и кнопку в макет окна
        layout.getChildren().add(title);
        layout.getChildren().addAll(ingredientCheckboxes);
        layout.getChildren().add(addToCartButton);

        // Создаем сцену для модального окна и отображаем его
        Scene scene = new Scene(layout, 300, 400);
        modalStage.setScene(scene);
        modalStage.showAndWait(); // Показываем окно и ждем его закрытия
    }

    // Открывает окно с рецептом кофе
    private void showRecipe(String name, String recipe) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Рецепт");
        alert.setHeaderText(name);
        alert.setContentText(recipe);
        alert.showAndWait();
    }

    /* ================= ДЕЙСТВИЯ С КОРЗИНОЙ СОТРУДНИКОМ ================= */

    // Открывает окно просмотра корзины с возможностью оформления заказа или очистки корзины
    private void openCartView() {
        // Создаем новое модальное окно (Stage)
        Stage cartStage = new Stage();
        cartStage.initModality(Modality.APPLICATION_MODAL); // Устанавливаем модальность (блокирует основное окно)
        cartStage.setTitle("Корзина"); // Заголовок окна

        // Основной контейнер (VBox) для содержимого корзины
        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER); // Выравниваем содержимое по центру

        // Создаем ScrollPane для прокрутки списка элементов корзины
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(layout); // Устанавливаем содержимое ScrollPane
        scrollPane.setFitToWidth(true); // Масштабирование по ширине
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // Полоса прокрутки по вертикали

        double totalPrice = 0.0; // Переменная для хранения общей стоимости заказа

        // Перебираем элементы корзины
        for (Cart.CartItem item : cart.getItems()) {
            // Контейнер для одного элемента корзины
            VBox itemLayout = new VBox(5);
            itemLayout.setStyle("-fx-border-color: #ddd; -fx-padding: 10; -fx-background-color: white; " +
                    "-fx-border-radius: 5px; -fx-background-radius: 5px;");

            // Название напитка
            Label drinkName = new Label(item.getDrink().getName());
            drinkName.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

            // Заголовок списка ингредиентов
            Label ingredientsList = new Label("Ингредиенты:");

            // Добавляем название напитка и заголовок ингредиентов в макет
            itemLayout.getChildren().addAll(drinkName, ingredientsList);

            // Рассчитываем стоимость напитка с дополнительными ингредиентами
            double itemPrice = item.getDrink().getPrice();
            for (Ingredient ingredient : item.getIngredients()) {
                Label ingredientLabel = new Label("- " + ingredient.getName()); // Отображение ингредиента
                itemLayout.getChildren().add(ingredientLabel);
                itemPrice += ingredient.getPrice(); // Увеличение стоимости напитка
            }

            // Итоговая стоимость напитка
            Label itemTotalPrice = new Label("Цена: " + itemPrice + " руб.");
            itemTotalPrice.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

            // Добавляем цену напитка в макет
            itemLayout.getChildren().addAll(itemTotalPrice);
            layout.getChildren().add(itemLayout); // Добавляем элемент в корзину

            totalPrice += itemPrice; // Обновляем итоговую стоимость
        }

        // Метка с общей стоимостью всех товаров в корзине
        Label totalPriceLabel = new Label("Итоговая стоимость: " + totalPrice + " руб.");
        totalPriceLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: red;");

        // Создаем контейнер для кнопок управления корзиной
        VBox buttonLayout = new VBox(10);
        buttonLayout.setAlignment(Pos.CENTER);

        // Кнопка очистки корзины
        Button clearCartButton = new Button("Очистить корзину");
        clearCartButton.setOnAction(e -> {
            cart.clear(); // Очищаем корзину
            layout.getChildren().clear(); // Удаляем все элементы из отображения
            totalPriceLabel.setText("Итоговая стоимость: 0 руб."); // Обновляем стоимость
            layout.getChildren().add(totalPriceLabel); // Добавляем обновленный текст
        });

        // Кнопка оформления заказа
        Button placeOrderButton = new Button("Оформить заказ");
        placeOrderButton.setOnAction(e -> placeOrder(layout, totalPriceLabel)); // Вызов метода оформления заказа

        // Добавляем кнопки в контейнер
        buttonLayout.getChildren().addAll(clearCartButton, placeOrderButton);

        // Создаем макет BorderPane для разделения содержимого
        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(scrollPane); // В центр помещаем список товаров
        borderPane.setBottom(buttonLayout); // Внизу кнопки управления

        // Добавляем метку общей стоимости в макет
        layout.getChildren().add(totalPriceLabel);

        // Устанавливаем сцену и отображаем окно
        Scene scene = new Scene(borderPane, 600, 400); // Устанавливаем размер окна
        cartStage.setScene(scene);
        cartStage.showAndWait(); // Отображаем окно и ждем его закрытия
    }

    // Оформляет заказ, добавляя его в базу данных и очищая корзину после успешного оформления
    private void placeOrder(VBox layout, Label totalPriceLabel) {
        // Проверяем, пуста ли корзина
        if (cart.getItems().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText(null);
            alert.setContentText("Корзина пуста. Добавьте товары перед оформлением заказа.");
            alert.showAndWait();
            return; // Прерываем выполнение метода
        }

        try (Connection conn = databaseConnection.connect()) {
            // SQL-запрос для вставки заказа в таблицу orders
            String orderSql = "INSERT INTO orders (user_id, order_date, total_price) VALUES (?, ?, ?)";
            PreparedStatement orderStmt = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS);

            // Получаем текущую дату и форматируем её
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedDate = now.format(formatter);

            // Получаем итоговую стоимость заказа
            double totalPrice = cart.getTotalPrice();

            // Устанавливаем параметры запроса
            orderStmt.setInt(1, userId); // ID пользователя
            orderStmt.setString(2, formattedDate); // Дата заказа
            orderStmt.setDouble(3, totalPrice); // Общая сумма заказа
            orderStmt.executeUpdate(); // Выполняем запрос

            // Получаем сгенерированный ID заказа
            ResultSet generatedKeys = orderStmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int orderId = generatedKeys.getInt(1); // ID нового заказа

                // SQL-запрос для вставки позиций заказа в таблицу orderitems
                String orderItemSql = "INSERT INTO orderitems (order_id, drink_id, quantity) VALUES (?, ?, ?)";
                PreparedStatement orderItemStmt = conn.prepareStatement(orderItemSql);

                // Добавляем все напитки из корзины в заказ
                for (Cart.CartItem item : cart.getItems()) {
                    orderItemStmt.setInt(1, orderId); // ID заказа
                    orderItemStmt.setInt(2, item.getDrink().getId()); // ID напитка
                    orderItemStmt.setInt(3, 1); // Количество (по умолчанию 1)
                    orderItemStmt.addBatch(); // Добавляем в пакетную операцию
                }
                orderItemStmt.executeBatch(); // Выполняем пакетную вставку

                // Очистка корзины после успешного оформления заказа
                cart.clear();

                // Очистка визуального отображения корзины
                layout.getChildren().clear();
                totalPriceLabel.setText("Итоговая стоимость: 0 руб."); // Обновляем сумму
                layout.getChildren().add(totalPriceLabel); // Добавляем обновленную метку стоимости

                // Показ уведомления об успешном оформлении заказа
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Успех");
                alert.setHeaderText(null);
                alert.setContentText("Заказ успешно оформлен!");
                alert.showAndWait();
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при оформлении заказа: " + e.getMessage());

            // Отображение ошибки пользователю
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText(null);
            alert.setContentText("Произошла ошибка при оформлении заказа. Пожалуйста, попробуйте снова.");
            alert.showAndWait();
        }
    }

    /* ================= РАСПИСАНИЕ КОФЕЙНИ ================= */

    // Открывает окно настроек расписания, позволяя пользователю просмотреть и изменить расписание
    private void openScheduleSettings() {
        Stage scheduleStage = new Stage();
        scheduleStage.initModality(Modality.APPLICATION_MODAL); // Делаем окно модальным
        scheduleStage.setTitle("Настройки расписания");

        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);

        // Список для отображения расписания
        ListView<HBox> scheduleList = new ListView<>();

        // Кнопка "Сохранить" (по умолчанию скрыта, отображается при изменениях)
        Button saveButton = new Button("Сохранить");
        saveButton.setVisible(false);
        saveButton.setOnAction(e -> saveSchedule(scheduleList, saveButton)); // Сохранение изменений

        // Кнопка "Закрыть", которая закрывает окно
        Button closeButton = new Button("Закрыть");
        closeButton.setOnAction(e -> scheduleStage.close());

        // Загрузка расписания в список
        loadSchedule(scheduleList, saveButton);

        // Добавляем элементы в макет
        layout.getChildren().addAll(scheduleList, saveButton, closeButton);

        // Создаём сцену и отображаем окно
        Scene scene = new Scene(layout, 400, 300);
        scheduleStage.setScene(scene);
        scheduleStage.showAndWait(); // Ожидание закрытия окна пользователем
    }

    // Загружает расписание из базы данных и отображает его в переданном списке
    private void loadSchedule(ListView<HBox> scheduleList, Button saveButton) {
        try (Connection conn = databaseConnection.connect()) {
            // SQL-запрос для получения данных о расписании, отсортированных по дню недели
            String sql = "SELECT day_of_week, opening_time, closing_time FROM schedule ORDER BY day_of_week";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            // Обход результатов запроса
            while (rs.next()) {
                String day = rs.getString("day_of_week");        // День недели
                String openTime = rs.getString("opening_time");  // Время открытия
                String closeTime = rs.getString("closing_time"); // Время закрытия

                Label dayLabel = new Label(day); // Метка с названием дня недели

                // Выпадающие списки для выбора времени открытия и закрытия
                ComboBox<String> openTimeComboBox = new ComboBox<>();
                ComboBox<String> closeTimeComboBox = new ComboBox<>();

                // Заполнение списков вариантами времени
                List<String> timeOptions = generateTimeOptions();
                openTimeComboBox.getItems().addAll(timeOptions);
                closeTimeComboBox.getItems().addAll(timeOptions);

                // Установка текущего времени в качестве выбранного значения
                openTimeComboBox.setValue(openTime);
                closeTimeComboBox.setValue(closeTime);

                // Создание горизонтального контейнера (HBox) для одного дня
                HBox scheduleItem = new HBox(10, dayLabel, openTimeComboBox, closeTimeComboBox);
                scheduleItem.setAlignment(Pos.CENTER_LEFT); // Выравнивание элементов по левому краю

                // Добавление элемента расписания в список
                scheduleItems.add(new ScheduleItem(day, openTimeComboBox, closeTimeComboBox));
                scheduleList.getItems().add(scheduleItem);

                // Показываем кнопку "Сохранить", если пользователь изменил время открытия
                openTimeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
                    saveButton.setVisible(true);
                });

                // Показываем кнопку "Сохранить", если пользователь изменил время закрытия
                closeTimeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
                    saveButton.setVisible(true);
                });
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при загрузке расписания: " + e.getMessage());
        }
    }

    // Сохраняет обновленное расписание в базе данных; Обновляет время открытия и закрытия для каждого дня недели
    private void saveSchedule(ListView<HBox> scheduleList, Button saveButton) {
        try (Connection conn = databaseConnection.connect()) {
            // Обход всех элементов расписания и обновление данных в базе
            for (ScheduleItem item : scheduleItems) {
                // SQL-запрос для обновления времени открытия и закрытия по дню недели
                String sql = "UPDATE schedule SET opening_time = ?, closing_time = ? WHERE day_of_week = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);

                // Установка параметров запроса (новые значения времени работы)
                stmt.setString(1, item.getOpenTimeComboBox().getValue()); // Время открытия
                stmt.setString(2, item.getCloseTimeComboBox().getValue()); // Время закрытия
                stmt.setString(3, item.getDay()); // День недели

                stmt.executeUpdate(); // Выполнение запроса
            }

            // Скрываем кнопку "Сохранить" после успешного обновления
            saveButton.setVisible(false);

            // Показ уведомления об успешном обновлении расписания
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Успех");
            alert.setHeaderText(null);
            alert.setContentText("Расписание успешно обновлено!");
            alert.showAndWait();

        } catch (SQLException e) {
            System.err.println("Ошибка при сохранении расписания: " + e.getMessage());

            // Показ уведомления об ошибке сохранения
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Ошибка");
            errorAlert.setHeaderText(null);
            errorAlert.setContentText("Произошла ошибка при обновлении расписания. Пожалуйста, попробуйте снова.");
            errorAlert.showAndWait();
        }
    }

    // Вспомогательный метод для заполнения комбо боксов
    private List<String> generateTimeOptions() {
        List<String> timeOptions = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            for (int minute = 0; minute < 60; minute += 15) {
                timeOptions.add(String.format("%02d:%02d", hour, minute));
            }
        }
        return timeOptions;
    }

    /* ================= ДЕЙСТВИЯ С НАПИТКАМИ СОТРУДНИКОМ ================= */

    // Открывает окно редактора напитков, позволяя пользователю добавлять, редактировать и удалять напитки
    private void openDrinkEditor() {
        // Создание нового окна для редактирования напитков
        Stage editStage = new Stage();
        editStage.initModality(Modality.APPLICATION_MODAL); // Делаем окно модальным (блокирует родительское окно)
        editStage.setTitle("Редактор напитков");

        VBox layout = new VBox(10); // Контейнер с вертикальным расположением элементов
        layout.setAlignment(Pos.CENTER);

        // Создание списка напитков
        ListView<Drink> drinkList = new ListView<>();
        drinkList.getItems().addAll(drinks); // Добавление напитков в список

        // Настройка отображения элементов списка
        drinkList.setCellFactory(param -> new ListCell<Drink>() {
            @Override
            protected void updateItem(Drink drink, boolean empty) {
                super.updateItem(drink, empty);
                if (empty || drink == null) {
                    setText(null);
                } else {
                    setText(drink.getName()); // Отображаем имя напитка
                }
            }
        });

        // Создание контекстного меню (правый клик по элементу)
        ContextMenu contextMenu = new ContextMenu();

        // Пункт меню "Редактировать"
        MenuItem editMenuItem = new MenuItem("Редактировать");
        editMenuItem.setOnAction(e -> {
            Drink selectedDrink = drinkList.getSelectionModel().getSelectedItem();
            if (selectedDrink != null) {
                openEditDrinkModal(selectedDrink); // Открытие окна редактирования напитка
            }
        });
        contextMenu.getItems().add(editMenuItem);

        // Пункт меню "Удалить"
        MenuItem deleteMenuItem = new MenuItem("Удалить");
        deleteMenuItem.setOnAction(e -> {
            Drink selectedDrink = drinkList.getSelectionModel().getSelectedItem();
            if (selectedDrink != null) {
                // Показ диалога подтверждения удаления
                Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmationAlert.setTitle("Подтверждение удаления");
                confirmationAlert.setHeaderText("Вы уверены, что хотите удалить этот напиток?");
                confirmationAlert.setContentText("Напиток: " + selectedDrink.getName());

                Optional<ButtonType> result = confirmationAlert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    // Удаление напитка из списка
                    drinkList.getItems().remove(selectedDrink);

                    // Удаление напитка из базы данных
                    deleteDrinkFromDatabase(selectedDrink.getId());
                }
            }
        });
        contextMenu.getItems().add(deleteMenuItem);

        // Установка контекстного меню для списка напитков
        drinkList.setContextMenu(contextMenu);

        // Кнопка для добавления нового напитка
        Button addDrinkButton = new Button("Добавить напиток");
        addDrinkButton.setOnAction(e -> openAddDrinkModal()); // Открытие окна добавления напитка

        // Кнопка закрытия окна
        Button closeButton = new Button("Закрыть");
        closeButton.setOnAction(e -> editStage.close());

        // Добавление элементов в контейнер
        layout.getChildren().addAll(drinkList, addDrinkButton, closeButton);

        // Создание и отображение сцены
        Scene scene = new Scene(layout, 400, 300);
        editStage.setScene(scene);
        editStage.showAndWait(); // Ожидание закрытия окна
    }

    // Открывает модальное окно для редактирования информации о напитке
    private void openEditDrinkModal(Drink drink) {
        // Создание нового окна (модального)
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL); // Делаем окно модальным
        modalStage.setTitle("Редактировать напиток");
        modalStage.setWidth(400);
        modalStage.setHeight(500);

        VBox layout = new VBox(10); // Контейнер с вертикальным расположением элементов
        layout.setAlignment(Pos.CENTER);

        // Поле ввода названия напитка
        TextField nameField = new TextField(drink.getName());

        // Поле ввода описания напитка (многострочное)
        TextArea descriptionField = new TextArea(drink.getDescription());
        descriptionField.setWrapText(true); // Включение переноса текста

        // Поле ввода URL изображения напитка
        TextField photoUrlField = new TextField(drink.getPhotoUrl());

        // Поле для ввода цены напитка
        TextField priceField = new TextField(String.valueOf(drink.getPrice()));

        // Отключение редактирования URL, если ссылка не начинается с "http"
        if (!drink.getPhotoUrl().toLowerCase().startsWith("http")) {
            photoUrlField.setDisable(true);
        }

        // Кнопка сохранения изменений
        Button saveButton = new Button("Сохранить");
        saveButton.setOnAction(e -> {
            // Обновление данных напитка из полей ввода
            drink.setName(nameField.getText());
            drink.setDescription(descriptionField.getText());
            drink.setPhotoUrl(photoUrlField.getText());

            // Обработка ввода цены (проверка на корректное число)
            try {
                double price = Double.parseDouble(priceField.getText());
                drink.setPrice(price);
            } catch (NumberFormatException ex) {
                // Вывод ошибки, если введена некорректная цена
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка");
                alert.setHeaderText(null);
                alert.setContentText("Неверный формат цены. Пожалуйста, введите число.");
                alert.showAndWait();
                return;
            }

            // Обновление информации в базе данных
            try (Connection conn = databaseConnection.connect()) {
                String sql = "UPDATE drinks SET name = ?, description = ?, photo_url = ?, price = ? WHERE drink_id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, drink.getName());
                stmt.setString(2, drink.getDescription());
                stmt.setString(3, drink.getPhotoUrl());
                stmt.setDouble(4, drink.getPrice()); // Запись обновленной цены
                stmt.setInt(5, drink.getId());
                stmt.executeUpdate();

                // Уведомление об успешном обновлении
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Успех");
                alert.setHeaderText(null);
                alert.setContentText("Напиток успешно обновлен!");
                alert.showAndWait();

                modalStage.close(); // Закрытие окна после сохранения

                loadCards(""); // Перезагрузка списка напитков
            } catch (SQLException ex) {
                System.err.println("Ошибка при обновлении напитка: " + ex.getMessage());
            }
        });

        // Кнопка отмены (закрытие без сохранения)
        Button cancelButton = new Button("Отмена");
        cancelButton.setOnAction(e -> modalStage.close());

        // Добавление всех элементов в макет окна
        layout.getChildren().addAll(
                new Label("Название:"), nameField,
                new Label("Описание:"), descriptionField,
                new Label("Фото URL:"), photoUrlField,
                new Label("Цена:"), priceField, // Поле для цены
                saveButton, cancelButton
        );

        // Создание сцены и отображение окна
        Scene scene = new Scene(layout, 400, 300);
        modalStage.setScene(scene);
        modalStage.showAndWait();
    }

    // Удаляет напиток из базы данных по его идентификатору
    private void deleteDrinkFromDatabase(int drinkId) {
        // Устанавливаем соединение с базой данных
        try (Connection conn = databaseConnection.connect()) {
            // SQL-запрос для удаления напитка по его ID
            String sql = "DELETE FROM drinks WHERE drink_id = ?";

            // Подготавливаем SQL-выражение с параметром
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                // Устанавливаем значение параметра (ID напитка)
                stmt.setInt(1, drinkId);

                // Выполняем SQL-запрос и получаем количество удалённых строк
                int affectedRows = stmt.executeUpdate();

                // Если хотя бы одна строка была удалена
                if (affectedRows > 0) {
                    // Обновляем список напитков из базы данных
                    drinks = getDrinks(conn);

                    // Обновляем пользовательский интерфейс
                    loadCards("");

                    // Показываем уведомление об успешном удалении
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Успех");
                    alert.setHeaderText(null);
                    alert.setContentText("Напиток успешно удален!");
                    alert.showAndWait();
                } else {
                    // Если напиток с таким ID не найден, показываем предупреждение
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Ошибка");
                    alert.setHeaderText(null);
                    alert.setContentText("Напиток не найден или уже был удален.");
                    alert.showAndWait();
                }
            }
        } catch (SQLException ex) {
            // Обрабатываем SQL-ошибки (например, проблемы с подключением или запросом)
            System.err.println("Ошибка при удалении напитка: " + ex.getMessage());

            // Показываем уведомление об ошибке пользователю
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Ошибка");
            errorAlert.setHeaderText(null);
            errorAlert.setContentText("Не удалось удалить напиток. Попробуйте снова.");
            errorAlert.showAndWait();
        }
    }

    // Открывает модальное окно для добавления нового напитка
    private void openAddDrinkModal() {
        // Создание нового окна
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL); // Устанавливаем модальность окна (блокирует основное окно)
        modalStage.setTitle("Добавить новый напиток"); // Заголовок окна
        modalStage.setWidth(400);
        modalStage.setHeight(500);
        modalStage.setResizable(false); // Запрещаем изменение размеров окна

        // Основной контейнер для размещения элементов
        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);

        // Поле для ввода названия напитка
        TextField nameField = new TextField();
        nameField.setPromptText("Введите название");

        // Поле для ввода описания напитка
        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Введите описание");
        descriptionField.setWrapText(true); // Разрешаем перенос текста

        // Поле для ввода цены напитка
        TextField priceField = new TextField();
        priceField.setPromptText("Введите цену");

        // Поле для ввода URL изображения напитка
        TextField photoUrlField = new TextField();
        photoUrlField.setPromptText("Введите URL фото (начинается с http)");

        // Кнопка сохранения нового напитка
        Button saveButton = new Button("Сохранить");
        saveButton.setOnAction(e -> {
            // Получаем данные из полей ввода
            String name = nameField.getText();
            String description = descriptionField.getText();
            String photoUrl = photoUrlField.getText();
            String priceText = priceField.getText();

            // Проверяем, заполнены ли все поля и корректен ли URL изображения
            if (name.isEmpty() || description.isEmpty() || photoUrl.isEmpty() || priceText.isEmpty() || !photoUrl.startsWith("http")) {
                // Выводим сообщение об ошибке, если данные некорректны
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка");
                alert.setHeaderText(null);
                alert.setContentText("Пожалуйста, заполните все поля корректно.");
                alert.showAndWait();
                return; // Прерываем выполнение, если данные некорректны
            }

            try {
                // Преобразуем цену в число
                double price = Double.parseDouble(priceText);

                // Подключаемся к базе данных и выполняем SQL-запрос на добавление нового напитка
                try (Connection conn = databaseConnection.connect()) {
                    String sql = "INSERT INTO drinks (name, description, photo_url, price) VALUES (?, ?, ?, ?)";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, name);
                    stmt.setString(2, description);
                    stmt.setString(3, photoUrl);
                    stmt.setDouble(4, price);
                    stmt.executeUpdate(); // Выполняем запрос

                    // Выводим сообщение об успешном добавлении напитка
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Успех");
                    alert.setHeaderText(null);
                    alert.setContentText("Новый напиток успешно добавлен!");
                    alert.showAndWait();

                    // Закрываем модальное окно
                    modalStage.close();

                    // Обновляем список напитков и интерфейс
                    drinks = getDrinks(conn);
                    loadCards("");
                } catch (SQLException ex) {
                    System.err.println("Ошибка при добавлении нового напитка: " + ex.getMessage());
                }
            } catch (NumberFormatException ex) {
                // Выводим сообщение об ошибке, если цена введена некорректно
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка");
                alert.setHeaderText(null);
                alert.setContentText("Пожалуйста, введите корректную цену.");
                alert.showAndWait();
            }
        });

        // Кнопка отмены (закрывает модальное окно без сохранения)
        Button cancelButton = new Button("Отмена");
        cancelButton.setOnAction(e -> modalStage.close());

        // Добавляем все элементы на форму
        layout.getChildren().addAll(
                new Label("Название:"), nameField,
                new Label("Описание:"), descriptionField,
                new Label("Цена:"), priceField,
                new Label("Фото URL:"), photoUrlField,
                saveButton, cancelButton
        );

        // Создаем сцену и устанавливаем ее в окно
        Scene scene = new Scene(layout, 400, 300);
        modalStage.setScene(scene);
        modalStage.showAndWait(); // Ожидаем закрытия модального окна
    }

}
