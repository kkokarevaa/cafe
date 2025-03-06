package com.example.cafe.cafe;

import java.util.List;

public class Drink {
    private int id;
    private String name;
    private String description;
    private String photoUrl;
    private double price;
    private List<Ingredient> ingredients;
    private String recipe;

    public Drink(int id, String name, String description, String photoUrl, double price, List<Ingredient> ingredients, String recipe) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.photoUrl = photoUrl;
        this.price = price;
        this.ingredients = ingredients;
        this.recipe = recipe;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getPhotoUrl() { return photoUrl; }
    public double getPrice() { return price; }
    public List<Ingredient> getIngredients() { return ingredients; }
    public String getRecipe() { return recipe; }

    public void setName(String text) {
        this.name = text;
    }

    public void setDescription(String text) {
        this.description = text;
    }

    public void setPhotoUrl(String text) {
        this.photoUrl = text;
    }
}
