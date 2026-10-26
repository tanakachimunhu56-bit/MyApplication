package com.example.myapplication;

import java.util.ArrayList;
import java.util.List;

public class Recipe {
    private final int id;
    private final String name;
    private final String steps;
    private final List<Ingredient> ingredients = new ArrayList<>();

    public Recipe(int id, String name, String steps) {
        this.id = id;
        this.name = name;
        this.steps = steps;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getSteps() { return steps; }
    public List<Ingredient> getIngredients() { return ingredients; }

    // One ingredient a recipe needs
    public static class Ingredient {
        public final String name;
        public final double quantity;
        public final String unit;

        public Ingredient(String name, double quantity, String unit) {
            this.name = name;
            this.quantity = quantity;
            this.unit = unit;
        }
    }
}
