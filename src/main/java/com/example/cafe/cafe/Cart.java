package com.example.cafe.cafe;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.ArrayList;
import java.util.List;

public class Cart {
    private ObservableList<CartItem> items;

    public Cart() {
        items = FXCollections.observableArrayList();
    }

    public void addItem(Drink drink, List<Ingredient> selectedIngredients) {
        CartItem item = new CartItem(drink, selectedIngredients);
        items.add(item);
    }

    public ObservableList<CartItem> getItems() {
        return items;
    }

    public void clear() {
        items.clear();
    }

    public static class CartItem {
        private Drink drink;
        private List<Ingredient> ingredients;

        public CartItem(Drink drink, List<Ingredient> ingredients) {
            this.drink = drink;
            this.ingredients = new ArrayList<>(ingredients);
        }

        public Drink getDrink() {
            return drink;
        }

        public List<Ingredient> getIngredients() {
            return ingredients;
        }
    }
}
