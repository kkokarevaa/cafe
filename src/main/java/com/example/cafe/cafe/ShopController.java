package com.example.cafe.cafe;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;


public class ShopController {

    @FXML
    private FlowPane cardsContainer;
    @FXML
    private TextField searchField;

    // Данные о напитках (название, описание, цена, путь к изображению)
    private final String[][] coffeeData = {
            {"Капучино", "Классический кофе с молочной пеной", "250", "coffee1.jpeg"},
            {"Латте", "Нежный вкус с большим количеством молока", "270", "coffee2.jpeg"},
            {"Американо", "Черный кофе с мягким вкусом", "200", "coffee3.jpeg"}
    };

    public void setUserId(int userId, String userRole) {
    }

    @FXML
    public void initialize() {
        loadCards();
    }

    private void loadCards() {
        cardsContainer.getChildren().clear();
        for (String[] coffee : coffeeData) {
            cardsContainer.getChildren().add(createCard(coffee));
        }
    }

    private VBox createCard(String[] coffee) {
        VBox card = new VBox(5);
        card.setStyle("-fx-border-color: #ddd; -fx-padding: 10; -fx-background-color: white; -fx-alignment: center;");

        String imageUrl = coffee[3];

        ImageView imageView = new ImageView();
        try {
            // Проверка, если imageUrl начинается с "http"
            if (imageUrl.startsWith("http")) {
                // Загружаем изображение с URL
                Image image = new Image(imageUrl);
                imageView.setImage(image);
            } else {
                // Загружаем изображение из ресурсов
                Image image = new Image(getClass().getResourceAsStream("images/" + imageUrl));
                imageView.setImage(image);
            }

            // Устанавливаем параметры изображения
            imageView.setFitWidth(600);
            imageView.setStyle("-fx-border-radius: 20px");
            imageView.setPreserveRatio(true);

        } catch (Exception e) {
            System.out.println("Could not load image: " + e.getMessage());
        }




        imageView.setFitWidth(120);
        imageView.setFitHeight(120);

        Label name = new Label(coffee[0]);
        name.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        Label description = new Label(coffee[1]);
        Label price = new Label("Цена: " + coffee[2] + " руб.");

        ComboBox<String> ingredientBox = new ComboBox<>();
        ingredientBox.getItems().addAll("Шоколад", "Ваниль", "Корица", "Карамель");

        Button recipeButton = new Button("Рецепт");
        recipeButton.setOnAction(e -> showRecipe(coffee[0]));

        card.getChildren().addAll(imageView, name, description, price, ingredientBox, recipeButton);
        return card;
    }


    private void showRecipe(String coffeeName) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Рецепт напитка");
        alert.setHeaderText(coffeeName);
        alert.setContentText("Описание рецепта для " + coffeeName);
        alert.showAndWait();
    }

    @FXML
    private void onSearch() {
        String query = searchField.getText().toLowerCase();
        cardsContainer.getChildren().clear();
        for (String[] coffee : coffeeData) {
            if (coffee[0].toLowerCase().contains(query)) {
                cardsContainer.getChildren().add(createCard(coffee));
            }
        }
    }
}
