package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class RecipeDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        TextView tvTitle = findViewById(R.id.tvRecipeTitle);
        TextView tvIngredients = findViewById(R.id.tvIngredients);
        TextView tvSteps = findViewById(R.id.tvSteps);
        Button btnCook = findViewById(R.id.btnCook);
        Button btnBack = findViewById(R.id.btnBack);

        // Read the values that the previous screen sent in the Intent
        int recipeId = getIntent().getIntExtra("recipe_id", -1);
        boolean canCook = getIntent().getBooleanExtra("can_cook", true);
        final Recipe recipe = new DatabaseHelper(this).getRecipeById(recipeId);

        if (recipe == null) {
            Toast.makeText(this, "Recipe not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvTitle.setText(recipe.getName());

        StringBuilder sb = new StringBuilder();
        for (Recipe.Ingredient ing : recipe.getIngredients()) {
            sb.append("\u2022 ")
                    .append(formatQuantity(ing.quantity))
                    .append(" ")
                    .append(ing.unit)
                    .append("  ")
                    .append(ing.name)
                    .append("\n");
        }
        tvIngredients.setText(sb.toString().trim());
        tvSteps.setText(recipe.getSteps());

        // The cook button only appears for recipes where nothing is missing
        btnCook.setVisibility(canCook ? View.VISIBLE : View.GONE);
        btnCook.setOnClickListener(v -> confirmCook(recipe));

        btnBack.setOnClickListener(v -> finish());
    }

    private void confirmCook(final Recipe recipe) {
        new AlertDialog.Builder(this)
                .setTitle("I cooked this")
                .setMessage("Remove the ingredients for " + recipe.getName() + " from your pantry?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    CookHelper.cook(new DatabaseHelper(this), recipe);
                    Toast.makeText(this, "Pantry updated", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String formatQuantity(double q) {
        if (q == Math.floor(q)) {
            return String.valueOf((int) q);
        }
        return String.valueOf(q);
    }
}