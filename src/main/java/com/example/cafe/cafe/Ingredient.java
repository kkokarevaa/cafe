package com.example.cafe.cafe;

public class Ingredient {
    private int id;
    private String name;
    private double price;

    public Ingredient(int id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
}
