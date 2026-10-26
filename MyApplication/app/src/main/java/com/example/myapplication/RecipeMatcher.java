package com.example.myapplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeMatcher {

    // A recipe that is missing exactly one ingredient (bonus "Almost There" list)
    public static class AlmostRecipe {
        public final Recipe recipe;
        public final String missingName;

        public AlmostRecipe(Recipe recipe, String missingName) {
            this.recipe = recipe;
            this.missingName = missingName;
        }
    }

    // STRICT RULE: a recipe is returned only if NOTHING is missing.
    // Every ingredient must be in the pantry in at least the required quantity.
    public static List<Recipe> findMakeableRecipes(List<Recipe> recipes, List<PantryItem> pantry) {
        Map<String, Double> totals = buildTotals(pantry);
        List<Recipe> makeable = new ArrayList<>();
        for (Recipe recipe : recipes) {
            if (missingIngredients(recipe, totals).isEmpty()) {
                makeable.add(recipe);
            }
        }
        return makeable;
    }

    // BONUS: recipes missing exactly one ingredient. Shown separately from the strict list.
    public static List<AlmostRecipe> findAlmostThere(List<Recipe> recipes, List<PantryItem> pantry) {
        Map<String, Double> totals = buildTotals(pantry);
        List<AlmostRecipe> almost = new ArrayList<>();
        for (Recipe recipe : recipes) {
            List<String> missing = missingIngredients(recipe, totals);
            if (missing.size() == 1) {
                almost.add(new AlmostRecipe(recipe, missing.get(0)));
            }
        }
        return almost;
    }

    // Totals up the pantry, grouped by clean name + unit type
    private static Map<String, Double> buildTotals(List<PantryItem> pantry) {
        Map<String, Double> totals = new HashMap<>();
        for (PantryItem item : pantry) {
            String key = normalizeName(item.getName()) + "#" + unitType(item.getUnit());
            double amount = toBaseUnit(item.getQuantity(), item.getUnit());
            Double current = totals.get(key);
            totals.put(key, (current == null ? 0 : current) + amount);
        }
        return totals;
    }

    // Names of the ingredients that are missing or not enough
    private static List<String> missingIngredients(Recipe recipe, Map<String, Double> totals) {
        List<String> missing = new ArrayList<>();
        for (Recipe.Ingredient ing : recipe.getIngredients()) {
            String key = normalizeName(ing.name) + "#" + unitType(ing.unit);
            Double have = totals.get(key);
            double need = toBaseUnit(ing.quantity, ing.unit);
            if (have == null || have + 0.0001 < need) {
                missing.add(ing.name);
            }
        }
        return missing;
    }

    // Makes "Tomatoes", " tomato " and "tomato" all equal
    public static String normalizeName(String name) {
        String n = name.trim().toLowerCase().replaceAll("\\s+", " ");
        if (n.endsWith("ies") && n.length() > 4) {
            return n.substring(0, n.length() - 3) + "y";   // berries -> berry
        }
        if (n.endsWith("oes")) {
            return n.substring(0, n.length() - 2);          // tomatoes -> tomato
        }
        if (n.endsWith("s") && !n.endsWith("ss")) {
            return n.substring(0, n.length() - 1);          // eggs -> egg
        }
        return n;
    }

    // Units are only compared with units of the same kind
    public static String unitType(String unit) {
        switch (unit) {
            case "g":
            case "kg":
                return "mass";
            case "ml":
            case "l":
            case "tsp":
            case "tbsp":
            case "cup":
                return "volume";
            default:
                return "count"; // pcs
        }
    }

    // Converts to a common unit: grams for mass, millilitres for volume
    public static double toBaseUnit(double quantity, String unit) {
        switch (unit) {
            case "kg":   return quantity * 1000;
            case "l":    return quantity * 1000;
            case "tsp":  return quantity * 5;
            case "tbsp": return quantity * 15;
            case "cup":  return quantity * 240;
            default:     return quantity; // g, ml, pcs
        }
    }

    // The reverse of toBaseUnit
    public static double fromBaseUnit(double base, String unit) {
        switch (unit) {
            case "kg":   return base / 1000;
            case "l":    return base / 1000;
            case "tsp":  return base / 5;
            case "tbsp": return base / 15;
            case "cup":  return base / 240;
            default:     return base;
        }
    }
}