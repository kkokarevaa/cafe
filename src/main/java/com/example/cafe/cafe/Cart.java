package com.example.cafe.cafe;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.ArrayList;
import java.util.List;

public class Cart {
    private ObservableList<CartItem> items; // Список товаров в корзине

    public Cart() {
        items = FXCollections.observableArrayList(); // Инициализация списка товаров
    }

    /**
     * Метод добавления напитка в корзину.
     */
    public void addItem(Drink drink, List<Ingredient> selectedIngredients) {
        CartItem item = new CartItem(drink, selectedIngredients);
        items.add(item);
    }

    /**
     * Получить список товаров в корзине.
     */
    public ObservableList<CartItem> getItems() {
        return items;
    }

    /**
     * Очистить корзину.
     */
    public void clear() {
        items.clear();
    }

    /**
     * Рассчитать общую стоимость товаров в корзине с учетом ингредиентов.
     */
    public double getTotalPrice() {
        double totalPrice = 0.0;
        for (CartItem item : items) {
            totalPrice += item.getDrink().getPrice(); // Добавляем цену напитка
            for (Ingredient ingredient : item.getIngredients()) {
                totalPrice += ingredient.getPrice(); // Добавляем стоимость каждого ингредиента
            }
        }
        return totalPrice;
    }

    /**
     * Вложенный класс, представляющий элемент корзины (напиток с ингредиентами).
     */
    public static class CartItem {
        private Drink drink; // Напиток
        private List<Ingredient> ingredients; // Список ингредиентов

        /**
         * Конструктор элемента корзины.
         */
        public CartItem(Drink drink, List<Ingredient> ingredients) {
            this.drink = drink;
            this.ingredients = new ArrayList<>(ingredients);
        }

        /**
         * Получить напиток.
         */
        public Drink getDrink() {
            return drink;
        }

        /**
         * Получить список ингредиентов.
         */
        public List<Ingredient> getIngredients() {
            return ingredients;
        }
    }
}