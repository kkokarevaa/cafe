package com.example.cafe.cafe;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ShopController {
    @FXML
    private FlowPane cardsContainer;
    @FXML
    private TextField searchField;

    private List<Drink> drinks;
    private final DatabaseConnection databaseConnection = new DatabaseConnection();

    @FXML
    public void initialize() {
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
            String sql = "SELECT i.ingredient_id, i.name, i.price " +
                    "FROM ingredients i " +
                    "JOIN drinkingredients di ON i.ingredient_id = di.ingredient_id " +
                    "WHERE di.drink_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, drinkId);
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

        card.getChildren().addAll(imageView, name, description, price, recipeButton);
        return card;
    }

    private void showRecipe(String name, String recipe) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Рецепт");
        alert.setHeaderText(name);
        alert.setContentText(recipe);
        alert.showAndWait();
    }

    public void setUserId(int userId, String userRole) {
    }
}
