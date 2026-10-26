package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "pantry.db";
    private static final int DB_VERSION = 2; // raised from 1 so the new tables get created

    public static final String TABLE_PANTRY = "pantry";
    public static final String TABLE_RECIPES = "recipes";
    public static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // Runs once, the first time the database is created
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_PANTRY + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "quantity REAL NOT NULL, "
                + "unit TEXT NOT NULL, "
                + "expiry TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPES + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "steps TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPE_INGREDIENTS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "recipe_id INTEGER NOT NULL, "
                + "name TEXT NOT NULL, "
                + "quantity REAL NOT NULL, "
                + "unit TEXT NOT NULL, "
                + "FOREIGN KEY(recipe_id) REFERENCES " + TABLE_RECIPES + "(id))");

        seedRecipes(db); // pre-load the recipes on first run
    }

    // Runs when DB_VERSION is increased (this clears old data, fine during development)
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        onCreate(db);
    }

    // ---------- PANTRY CRUD ----------

    // CREATE
    public long addPantryItem(String name, double quantity, String unit, String expiry) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("quantity", quantity);
        values.put("unit", unit);
        values.put("expiry", expiry);
        return db.insert(TABLE_PANTRY, null, values);
    }

    // READ (all)
    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_PANTRY, null, null, null, null, null, "name ASC");
        while (c.moveToNext()) {
            items.add(new PantryItem(
                    c.getInt(c.getColumnIndexOrThrow("id")),
                    c.getString(c.getColumnIndexOrThrow("name")),
                    c.getDouble(c.getColumnIndexOrThrow("quantity")),
                    c.getString(c.getColumnIndexOrThrow("unit")),
                    c.getString(c.getColumnIndexOrThrow("expiry"))));
        }
        c.close();
        return items;
    }

    // UPDATE
    public int updatePantryItem(int id, String name, double quantity, String unit, String expiry) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("quantity", quantity);
        values.put("unit", unit);
        values.put("expiry", expiry);
        return db.update(TABLE_PANTRY, values, "id = ?", new String[]{String.valueOf(id)});
    }

    // DELETE
    public int deletePantryItem(int id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_PANTRY, "id = ?", new String[]{String.valueOf(id)});
    }

    // ---------- RECIPES ----------

    // Loads every recipe together with its ingredients
    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_RECIPES, null, null, null, null, null, "name ASC");
        while (c.moveToNext()) {
            Recipe recipe = new Recipe(
                    c.getInt(c.getColumnIndexOrThrow("id")),
                    c.getString(c.getColumnIndexOrThrow("name")),
                    c.getString(c.getColumnIndexOrThrow("steps")));
            loadIngredients(db, recipe);
            recipes.add(recipe);
        }
        c.close();
        return recipes;
    }

    // Loads one recipe by id (used by the detail screen later)
    public Recipe getRecipeById(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_RECIPES, null, "id = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        Recipe recipe = null;
        if (c.moveToFirst()) {
            recipe = new Recipe(
                    c.getInt(c.getColumnIndexOrThrow("id")),
                    c.getString(c.getColumnIndexOrThrow("name")),
                    c.getString(c.getColumnIndexOrThrow("steps")));
            loadIngredients(db, recipe);
        }
        c.close();
        return recipe;
    }

    private void loadIngredients(SQLiteDatabase db, Recipe recipe) {
        Cursor c = db.query(TABLE_RECIPE_INGREDIENTS, null, "recipe_id = ?",
                new String[]{String.valueOf(recipe.getId())}, null, null, null);
        while (c.moveToNext()) {
            recipe.getIngredients().add(new Recipe.Ingredient(
                    c.getString(c.getColumnIndexOrThrow("name")),
                    c.getDouble(c.getColumnIndexOrThrow("quantity")),
                    c.getString(c.getColumnIndexOrThrow("unit"))));
        }
        c.close();
    }

    // ---------- SEED DATA ----------

    // Each ingredient is written as "name|quantity|unit"
    private void addRecipe(SQLiteDatabase db, String name, String steps, String... ingredients) {
        ContentValues rv = new ContentValues();
        rv.put("name", name);
        rv.put("steps", steps);
        long recipeId = db.insert(TABLE_RECIPES, null, rv);

        for (String ing : ingredients) {
            String[] p = ing.split("\\|");
            ContentValues iv = new ContentValues();
            iv.put("recipe_id", recipeId);
            iv.put("name", p[0]);
            iv.put("quantity", Double.parseDouble(p[1]));
            iv.put("unit", p[2]);
            db.insert(TABLE_RECIPE_INGREDIENTS, null, iv);
        }
    }

    private void seedRecipes(SQLiteDatabase db) {
        addRecipe(db, "Tomato Pasta",
                "1. Boil the pasta until soft.\n2. Fry chopped garlic in oil, add chopped tomatoes and salt.\n3. Simmer 10 minutes, then mix with the pasta.",
                "pasta|200|g", "tomato|3|pcs", "garlic|2|pcs", "oil|1|tbsp", "salt|1|tsp");

        addRecipe(db, "Scrambled Eggs on Toast",
                "1. Toast the bread.\n2. Beat the eggs with salt.\n3. Cook in melted butter, stirring, then serve on the toast.",
                "egg|3|pcs", "bread|2|pcs", "butter|10|g", "salt|1|tsp");

        addRecipe(db, "Cheese Omelette",
                "1. Beat the eggs with salt.\n2. Pour into a pan with melted butter.\n3. Add grated cheese, fold and serve.",
                "egg|3|pcs", "cheese|50|g", "butter|10|g", "salt|1|tsp");

        addRecipe(db, "Egg Fried Rice",
                "1. Fry chopped onion in oil.\n2. Add scrambled egg and cooked rice.\n3. Stir in soy sauce and serve hot.",
                "rice|200|g", "egg|2|pcs", "onion|1|pcs", "soy sauce|2|tbsp", "oil|1|tbsp");

        addRecipe(db, "Pancakes",
                "1. Mix flour, sugar, egg and milk into a batter.\n2. Fry spoonfuls in melted butter.\n3. Flip when bubbles appear.",
                "flour|200|g", "milk|300|ml", "egg|1|pcs", "sugar|2|tbsp", "butter|20|g");

        addRecipe(db, "Mashed Potatoes",
                "1. Peel and boil the potatoes until soft.\n2. Drain and mash.\n3. Mix in butter, milk and salt.",
                "potato|4|pcs", "butter|30|g", "milk|100|ml", "salt|1|tsp");

        addRecipe(db, "Garlic Bread",
                "1. Mix crushed garlic into soft butter.\n2. Spread on the bread.\n3. Bake at 180C for 10 minutes.",
                "bread|4|pcs", "butter|40|g", "garlic|2|pcs");

        addRecipe(db, "Vegetable Soup",
                "1. Chop the carrots, potatoes and onion.\n2. Simmer in the stock for 25 minutes.\n3. Season with salt and serve.",
                "carrot|2|pcs", "potato|2|pcs", "onion|1|pcs", "vegetable stock|500|ml", "salt|1|tsp");

        addRecipe(db, "Cheese Toastie",
                "1. Butter the outside of the bread.\n2. Put cheese between the slices.\n3. Fry both sides until golden.",
                "bread|2|pcs", "cheese|60|g", "butter|10|g");

        addRecipe(db, "Banana Pancakes",
                "1. Mash the bananas.\n2. Mix in eggs and flour.\n3. Fry small pancakes on both sides.",
                "banana|2|pcs", "egg|2|pcs", "flour|50|g");

        addRecipe(db, "Tuna Salad",
                "1. Drain the tuna.\n2. Mix with mayonnaise and chopped onion.\n3. Serve on its own or on bread.",
                "tuna|1|pcs", "mayonnaise|2|tbsp", "onion|1|pcs");

        addRecipe(db, "Chicken and Rice",
                "1. Fry the chicken pieces with onion and salt.\n2. Add the rice and enough water to cover.\n3. Cook until the rice is soft.",
                "chicken|300|g", "rice|200|g", "onion|1|pcs", "salt|1|tsp");

        addRecipe(db, "Baked Potatoes",
                "1. Prick the potatoes and bake at 200C for 1 hour.\n2. Cut open and add butter and salt.\n3. Top with grated cheese.",
                "potato|4|pcs", "butter|20|g", "salt|1|tsp", "cheese|50|g");

        addRecipe(db, "Porridge",
                "1. Heat the milk gently.\n2. Stir in the oats and cook 5 minutes.\n3. Sweeten with sugar.",
                "oats|80|g", "milk|300|ml", "sugar|1|tbsp");

        addRecipe(db, "Spaghetti Bolognese",
                "1. Boil the spaghetti.\n2. Fry onion, garlic and mince, then add chopped tomatoes.\n3. Simmer 20 minutes and serve over the spaghetti.",
                "spaghetti|200|g", "beef mince|250|g", "tomato|3|pcs", "onion|1|pcs", "garlic|2|pcs");

        addRecipe(db, "French Toast",
                "1. Beat the eggs with milk and sugar.\n2. Dip the bread slices.\n3. Fry in butter until golden.",
                "bread|4|pcs", "egg|2|pcs", "milk|100|ml", "sugar|1|tbsp", "butter|10|g");

        addRecipe(db, "Egg Fried Noodles",
                "1. Boil the noodles and drain.\n2. Scramble the eggs in oil.\n3. Add noodles and soy sauce, stir for 2 minutes.",
                "noodles|200|g", "egg|2|pcs", "soy sauce|2|tbsp", "oil|1|tbsp");

        addRecipe(db, "Cheesy Pasta",
                "1. Boil the pasta.\n2. Melt butter, milk and cheese into a sauce.\n3. Mix with the pasta.",
                "pasta|200|g", "cheese|100|g", "milk|100|ml", "butter|20|g");
    }
}
