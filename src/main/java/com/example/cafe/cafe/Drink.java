package com.example.cafe.cafe;

import java.util.List;

/**
 * Класс, представляющий напиток в кафе.
 */
public class Drink {
    private int id; // Уникальный идентификатор напитка
    private String name; // Название напитка
    private String description; // Описание напитка
    private String photoUrl; // URL изображения напитка
    private double price; // Цена напитка
    private List<Ingredient> ingredients; // Список ингредиентов напитка
    private String recipe; // Рецепт приготовления напитка

    /**
     * Конструктор для создания нового объекта Drink.
     * @param id Уникальный идентификатор напитка
     * @param name Название напитка
     * @param description Описание напитка
     * @param photoUrl URL изображения напитка
     * @param price Цена напитка
     * @param ingredients Список ингредиентов
     * @param recipe Рецепт приготовления
     */
    public Drink(int id, String name, String description, String photoUrl, double price, List<Ingredient> ingredients, String recipe) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.photoUrl = photoUrl;
        this.price = price;
        this.ingredients = ingredients;
        this.recipe = recipe;
    }

    // Геттеры
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getPhotoUrl() { return photoUrl; }
    public double getPrice() { return price; }
    public List<Ingredient> getIngredients() { return ingredients; }
    public String getRecipe() { return recipe; }

    // Сеттеры
    public void setName(String text) {
        this.name = text;
    }

    public void setDescription(String text) {
        this.description = text;
    }

    public void setPhotoUrl(String text) {
        this.photoUrl = text;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}