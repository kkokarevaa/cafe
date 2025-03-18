package com.example.cafe.cafe;

public class Ingredient {
    private int id;
    private String name;
    private double price;

    /**
     * Конструктор для создания нового ингредиента.
     *
     * @param id Уникальный идентификатор ингредиента.
     * @param name Название ингредиента.
     * @param price Стоимость ингредиента.
     */
    public Ingredient(int id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    /**
     * Получает уникальный идентификатор ингредиента.
     *
     * @return id ингредиента.
     */
    public int getId() { return id; }

    /**
     * Получает название ингредиента.
     *
     * @return Название ингредиента.
     */
    public String getName() { return name; }

    /**
     * Получает стоимость ингредиента.
     *
     * @return Цена ингредиента.
     */
    public double getPrice() { return price; }
}
