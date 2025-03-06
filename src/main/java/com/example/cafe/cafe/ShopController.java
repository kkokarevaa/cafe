package com.example.cafe.cafe;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ShopController {
    @FXML
    private FlowPane cardsContainer;
    @FXML
    private TextField searchField;
    @FXML
    private Button settingsButton;
    @FXML
    private Button cartButton;
    @FXML
    private Button editButton;

    private List<Drink> drinks;
    private final DatabaseConnection databaseConnection = new DatabaseConnection();
    private ObservableList<ScheduleItem> scheduleItems = FXCollections.observableArrayList();

    private int userId;
    private String userRole;

    public void setUserId(int userId, String userRole) {
        this.userId = userId;
        this.userRole = userRole;

        if ("Сотрудник".equals(userRole)) {
            settingsButton.setVisible(true);
            cartButton.setVisible(true);
        } else {
            settingsButton.setVisible(false);
            cartButton.setVisible(false);
        }

        try (Connection conn = databaseConnection.connect()) {
            drinks = getDrinks(conn);
        } catch (Exception e) {
            System.err.println("Ошибка при подключении к базе данных: " + e.getMessage());
            drinks = new ArrayList<>();
        }
        cardsContainer.setAlignment(Pos.CENTER);
        loadCards("");

        // Динамический поиск: обновление при каждом вводе символа
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            loadCards(newValue.toLowerCase());
        });

        settingsButton.setOnAction(e -> openScheduleSettings());
        editButton.setOnAction(e -> openDrinkEditor()); // Set action for editButton
    }

    @FXML
    public void initialize() {

    }

    private List<Drink> getDrinks(Connection conn) {
        List<Drink> drinks = new ArrayList<>();
        if (conn == null) return drinks;

        try {
            String sql = "SELECT d.drink_id, d.name, d.description, d.photo_url, d.price, r.recipe_text " +
                    "FROM drinks d LEFT JOIN recipes r ON d.drink_id = r.drink_id";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("drink_id");
                String name = rs.getString("name");
                String description = rs.getString("description");
                String photoUrl = rs.getString("photo_url");
                double price = rs.getDouble("price");
                String recipe = rs.getString("recipe_text");
                List<Ingredient> ingredients = getIngredientsForDrink(id, conn);
                drinks.add(new Drink(id, name, description, photoUrl, price, ingredients, recipe));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при загрузке напитков: " + e.getMessage());
        }
        return drinks;
    }

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

    private void loadCards(String query) {
        cardsContainer.getChildren().clear();
        for (Drink drink : drinks) {
            if (drink.getName().toLowerCase().contains(query)) {
                cardsContainer.getChildren().add(createCard(drink));
            }
        }
    }

    private VBox createCard(Drink drink) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setMinWidth(1113);
        card.setStyle("-fx-border-color: #ddd; -fx-padding: 15; -fx-background-color: white; " +
                "-fx-border-radius: 10px; -fx-background-radius: 10px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 2);");

        ImageView imageView = new ImageView();
        try {
            if (drink.getPhotoUrl().startsWith("http")) {
                Image image = new Image(drink.getPhotoUrl());
                imageView.setImage(image);
            } else {
                Image image = new Image(getClass().getResourceAsStream("images/" + drink.getPhotoUrl()));
                imageView.setImage(image);
            }
            imageView.setFitWidth(400);
            imageView.setStyle("-fx-border-radius: 20px");
            imageView.setPreserveRatio(true);
        } catch (Exception e) {
            System.out.println("Could not load image: " + e.getMessage());
        }

        Label name = new Label(drink.getName());
        name.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Label description = new Label(drink.getDescription());
        Label price = new Label("Цена: " + drink.getPrice() + " руб.");

        Button recipeButton = new Button("Рецепт");
        recipeButton.setOnAction(e -> showRecipe(drink.getName(), drink.getRecipe()));

        Button addButton = new Button("Добавить");
        addButton.setOnAction(e -> openAddToCartModal(drink));
        card.getChildren().addAll(imageView, name, description, price, recipeButton, addButton);


        return card;
    }

    private void openAddToCartModal(Drink drink) {
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setTitle("Добавить в корзину");

        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        Label title = new Label("Выберите ингредиенты для " + drink.getName());

        List<CheckBox> ingredientCheckboxes = new ArrayList<>();
        for (Ingredient ingredient : drink.getIngredients()) {
            CheckBox checkBox = new CheckBox(ingredient.getName() + " (+" + ingredient.getPrice() + " руб.)");
            ingredientCheckboxes.add(checkBox);
        }

        Button addToCartButton = new Button("Добавить в корзину");
        addToCartButton.setOnAction(e -> {
            List<Ingredient> selectedIngredients = new ArrayList<>();
            for (int i = 0; i < ingredientCheckboxes.size(); i++) {
                if (ingredientCheckboxes.get(i).isSelected()) {
                    selectedIngredients.add(drink.getIngredients().get(i));
                }
            }
            addToCart(drink, selectedIngredients);
            modalStage.close();
        });

        layout.getChildren().add(title);
        layout.getChildren().addAll(ingredientCheckboxes);
        layout.getChildren().add(addToCartButton);

        Scene scene = new Scene(layout, 300, 400);
        modalStage.setScene(scene);
        modalStage.showAndWait();
    }

    private void addToCart(Drink drink, List<Ingredient> selectedIngredients) {
        System.out.println("Добавлено в корзину: " + drink.getName() + " с " + selectedIngredients.size() + " ингредиентами");
    }

    private void showRecipe(String name, String recipe) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Рецепт");
        alert.setHeaderText(name);
        alert.setContentText(recipe);
        alert.showAndWait();
    }

    private void openScheduleSettings() {
        Stage scheduleStage = new Stage();
        scheduleStage.initModality(Modality.APPLICATION_MODAL);
        scheduleStage.setTitle("Настройки расписания");

        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);

        ListView<HBox> scheduleList = new ListView<>();
        Button saveButton = new Button("Сохранить");
        saveButton.setVisible(false);
        saveButton.setOnAction(e -> saveSchedule(scheduleList, saveButton));

        Button closeButton = new Button("Закрыть");
        closeButton.setOnAction(e -> scheduleStage.close());

        loadSchedule(scheduleList, saveButton); // Pass saveButton to loadSchedule

        layout.getChildren().addAll(scheduleList, saveButton, closeButton);
        Scene scene = new Scene(layout, 400, 300);
        scheduleStage.setScene(scene);
        scheduleStage.showAndWait();
    }

    private void loadSchedule(ListView<HBox> scheduleList, Button saveButton) {
        try (Connection conn = databaseConnection.connect()) {
            String sql = "SELECT day_of_week, opening_time, closing_time FROM schedule ORDER BY day_of_week";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String day = rs.getString("day_of_week");
                String openTime = rs.getString("opening_time");
                String closeTime = rs.getString("closing_time");

                Label dayLabel = new Label(day);

                // Create ComboBox for opening and closing times
                ComboBox<String> openTimeComboBox = new ComboBox<>();
                ComboBox<String> closeTimeComboBox = new ComboBox<>();

                // Populate ComboBox with time options
                List<String> timeOptions = generateTimeOptions();
                openTimeComboBox.getItems().addAll(timeOptions);
                closeTimeComboBox.getItems().addAll(timeOptions);

                // Set the current times as the selected values
                openTimeComboBox.setValue(openTime);
                closeTimeComboBox.setValue(closeTime);

                HBox scheduleItem = new HBox(10, dayLabel, openTimeComboBox, closeTimeComboBox);
                scheduleItem.setAlignment(Pos.CENTER_LEFT);

                scheduleItems.add(new ScheduleItem(day, openTimeComboBox, closeTimeComboBox));
                scheduleList.getItems().add(scheduleItem);

                // Show save button when a change is made
                openTimeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
                    saveButton.setVisible(true);
                });

                closeTimeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
                    saveButton.setVisible(true);
                });
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при загрузке расписания: " + e.getMessage());
        }
    }

    private void saveSchedule(ListView<HBox> scheduleList, Button saveButton) {
        try (Connection conn = databaseConnection.connect()) {
            for (ScheduleItem item : scheduleItems) {
                String sql = "UPDATE schedule SET opening_time = ?, closing_time = ? WHERE day_of_week = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, item.getOpenTimeComboBox().getValue());
                stmt.setString(2, item.getCloseTimeComboBox().getValue());
                stmt.setString(3, item.getDay());
                stmt.executeUpdate();
            }

            saveButton.setVisible(false);

            // Show success notification
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Успех");
            alert.setHeaderText(null);
            alert.setContentText("Расписание успешно обновлено!");
            alert.showAndWait();

        } catch (SQLException e) {
            System.err.println("Ошибка при сохранении расписания: " + e.getMessage());

            // Optionally, show an error notification
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Ошибка");
            errorAlert.setHeaderText(null);
            errorAlert.setContentText("Произошла ошибка при обновлении расписания. Пожалуйста, попробуйте снова.");
            errorAlert.showAndWait();
        }
    }

    private List<String> generateTimeOptions() {
        List<String> timeOptions = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            for (int minute = 0; minute < 60; minute += 15) {
                timeOptions.add(String.format("%02d:%02d", hour, minute));
            }
        }
        return timeOptions;
    }


    private void openDrinkEditor() {
        Stage editStage = new Stage();
        editStage.initModality(Modality.APPLICATION_MODAL);
        editStage.setTitle("Редактор напитков");

        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);

        ListView<Drink> drinkList = new ListView<>();
        drinkList.getItems().addAll(drinks);

        drinkList.setCellFactory(param -> new ListCell<Drink>() {
            @Override
            protected void updateItem(Drink drink, boolean empty) {
                super.updateItem(drink, empty);
                if (empty || drink == null) {
                    setText(null);
                } else {
                    setText(drink.getName());
                }
            }
        });

        // Create a context menu
        ContextMenu contextMenu = new ContextMenu();

        MenuItem editMenuItem = new MenuItem("Редактировать");
        editMenuItem.setOnAction(e -> {
            Drink selectedDrink = drinkList.getSelectionModel().getSelectedItem();
            if (selectedDrink != null) {
                openEditDrinkModal(selectedDrink);
            }
        });
        contextMenu.getItems().add(editMenuItem);

        MenuItem deleteMenuItem = new MenuItem("Удалить");
        deleteMenuItem.setOnAction(e -> {
            Drink selectedDrink = drinkList.getSelectionModel().getSelectedItem();
            if (selectedDrink != null) {
                // Remove the drink from the list
                drinkList.getItems().remove(selectedDrink);

                // Optionally, delete the drink from the database
                deleteDrinkFromDatabase(selectedDrink.getId());
            }
        });
        contextMenu.getItems().add(deleteMenuItem);

        // Set the context menu for the ListView
        drinkList.setContextMenu(contextMenu);

        Button addDrinkButton = new Button("Добавить напиток");
        addDrinkButton.setOnAction(e -> openAddDrinkModal());

        Button closeButton = new Button("Закрыть");
        closeButton.setOnAction(e -> editStage.close());

        layout.getChildren().addAll(drinkList, closeButton, addDrinkButton);
        Scene scene = new Scene(layout, 400, 300);
        editStage.setScene(scene);
        editStage.showAndWait();
    }


    private void openEditDrinkModal(Drink drink) {
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setTitle("Редактировать напиток");
        modalStage.setWidth(400);
        modalStage.setHeight(500);

        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);

        TextField nameField = new TextField(drink.getName());
        TextArea descriptionField = new TextArea(drink.getDescription());
        descriptionField.setWrapText(true);
        TextField photoUrlField = new TextField(drink.getPhotoUrl());

        // Disable photoUrlField if the URL does not start with "http"
        if (!drink.getPhotoUrl().toLowerCase().startsWith("http")) {
            photoUrlField.setDisable(true);
        }

        Button saveButton = new Button("Сохранить");
        saveButton.setOnAction(e -> {
            drink.setName(nameField.getText());
            drink.setDescription(descriptionField.getText());
            drink.setPhotoUrl(photoUrlField.getText());

            try (Connection conn = databaseConnection.connect()) {
                String sql = "UPDATE drinks SET name = ?, description = ?, photo_url = ? WHERE drink_id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, drink.getName());
                stmt.setString(2, drink.getDescription());
                stmt.setString(3, drink.getPhotoUrl());
                stmt.setInt(4, drink.getId());
                stmt.executeUpdate();

                // Show success notification
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Успех");
                alert.setHeaderText(null);
                alert.setContentText("Напиток успешно обновлен!");
                alert.showAndWait();

                modalStage.close();

                loadCards("");
            } catch (SQLException ex) {
                System.err.println("Ошибка при обновлении напитка: " + ex.getMessage());
            }
        });

        Button cancelButton = new Button("Отмена");
        cancelButton.setOnAction(e -> modalStage.close());

        layout.getChildren().addAll(
                new Label("Название:"), nameField,
                new Label("Описание:"), descriptionField,
                new Label("Фото URL:"), photoUrlField,
                saveButton, cancelButton
        );

        Scene scene = new Scene(layout, 400, 300);
        modalStage.setScene(scene);
        modalStage.showAndWait();
    }

    private void deleteDrinkFromDatabase(int drinkId) {
        // Establish a connection to the database
        try (Connection conn = databaseConnection.connect()) {
            // Define the SQL query to delete a drink
            String sql = "DELETE FROM drinks WHERE drink_id = ?";

            // Prepare the statement with the SQL query
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                // Set the drink ID parameter
                stmt.setInt(1, drinkId);

                // Execute the update
                stmt.executeUpdate();

                // Refresh the drinks list from the database
                drinks = getDrinks(conn);

                // Update the UI
                loadCards("");

                // Show success notification
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Успех");
                alert.setHeaderText(null);
                alert.setContentText("Напиток успешно удален!");
                alert.showAndWait();
            }
        } catch (SQLException ex) {
            // Handle any SQL exceptions
            System.err.println("Ошибка при удалении напитка: " + ex.getMessage());
        }
    }

    private void openAddDrinkModal() {
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setTitle("Добавить новый напиток");
        modalStage.setWidth(400);
        modalStage.setHeight(500);
        modalStage.setResizable(false);

        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);

        TextField nameField = new TextField();
        nameField.setPromptText("Введите название");
        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Введите описание");
        descriptionField.setWrapText(true);
        TextField priceField = new TextField();
        priceField.setPromptText("Введите цену");
        TextField photoUrlField = new TextField();
        photoUrlField.setPromptText("Введите URL фото (начинается с http)");

        Button saveButton = new Button("Сохранить");
        saveButton.setOnAction(e -> {
            String name = nameField.getText();
            String description = descriptionField.getText();
            String photoUrl = photoUrlField.getText();
            String priceText = priceField.getText();

            if (name.isEmpty() || description.isEmpty() || photoUrl.isEmpty() || priceText.isEmpty() || !photoUrl.startsWith("http")) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка");
                alert.setHeaderText(null);
                alert.setContentText("Пожалуйста, заполните все поля корректно.");
                alert.showAndWait();
                return;
            }

            try {
                double price = Double.parseDouble(priceText);

                try (Connection conn = databaseConnection.connect()) {
                    String sql = "INSERT INTO drinks (name, description, photo_url, price) VALUES (?, ?, ?, ?)";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, name);
                    stmt.setString(2, description);
                    stmt.setString(3, photoUrl);
                    stmt.setDouble(4, price);
                    stmt.executeUpdate();

                    // Show success notification
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Успех");
                    alert.setHeaderText(null);
                    alert.setContentText("Новый напиток успешно добавлен!");
                    alert.showAndWait();

                    modalStage.close();

                    // Refresh the drink list
                    drinks = getDrinks(conn);
                    loadCards("");
                } catch (SQLException ex) {
                    System.err.println("Ошибка при добавлении нового напитка: " + ex.getMessage());
                }
            } catch (NumberFormatException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка");
                alert.setHeaderText(null);
                alert.setContentText("Пожалуйста, введите корректную цену.");
                alert.showAndWait();
            }
        });

        Button cancelButton = new Button("Отмена");
        cancelButton.setOnAction(e -> modalStage.close());

        layout.getChildren().addAll(
                new Label("Название:"), nameField,
                new Label("Описание:"), descriptionField,
                new Label("Цена:"), priceField,
                new Label("Фото URL:"), photoUrlField,
                saveButton, cancelButton
        );

        Scene scene = new Scene(layout, 400, 300);
        modalStage.setScene(scene);
        modalStage.showAndWait();
    }

}
