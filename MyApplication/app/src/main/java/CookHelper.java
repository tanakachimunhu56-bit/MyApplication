package com.example.myapplication;

import java.util.List;

// "I cooked this": takes the recipe's ingredients out of the pantry
public class CookHelper {

    public static void cook(DatabaseHelper db, Recipe recipe) {
        List<PantryItem> pantry = db.getAllPantryItems();

        for (Recipe.Ingredient ing : recipe.getIngredients()) {
            double remaining = RecipeMatcher.toBaseUnit(ing.quantity, ing.unit);
            String key = RecipeMatcher.normalizeName(ing.name) + "#" + RecipeMatcher.unitType(ing.unit);

            for (PantryItem item : pantry) {
                if (remaining <= 0.0001) {
                    break;
                }
                String itemKey = RecipeMatcher.normalizeName(item.getName())
                        + "#" + RecipeMatcher.unitType(item.getUnit());
                if (!itemKey.equals(key)) {
                    continue;
                }

                double have = RecipeMatcher.toBaseUnit(item.getQuantity(), item.getUnit());
                double used = Math.min(have, remaining);
                double left = have - used;
                remaining -= used;

                if (left <= 0.0001) {
                    db.deletePantryItem(item.getId());          // used up completely
                } else {
                    double leftInItemUnit = RecipeMatcher.fromBaseUnit(left, item.getUnit());
                    leftInItemUnit = Math.round(leftInItemUnit * 100.0) / 100.0;
                    db.updatePantryItem(item.getId(), item.getName(),
                            leftInItemUnit, item.getUnit(), item.getExpiry());
                }
            }
        }
    }
}