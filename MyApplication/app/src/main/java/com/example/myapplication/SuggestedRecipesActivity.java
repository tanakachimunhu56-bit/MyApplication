package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class SuggestedRecipesActivity extends AppCompatActivity {

    private LinearLayout llStrict, llAlmost;
    private TextView tvNoMatch, tvAlmostEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested);

        llStrict = findViewById(R.id.llStrict);
        llAlmost = findViewById(R.id.llAlmost);
        tvNoMatch = findViewById(R.id.tvNoMatch);
        tvAlmostEmpty = findViewById(R.id.tvAlmostEmpty);

        NavHelper.setup(this, R.id.nav_suggested);
    }

    // Re-run the matching every time the screen appears (the pantry may have changed)
    @Override
    protected void onResume() {
        super.onResume();
        loadSuggestions();
    }

    private void loadSuggestions() {
        DatabaseHelper db = new DatabaseHelper(this);
        List<Recipe> recipes = db.getAllRecipes();
        List<PantryItem> pantry = db.getAllPantryItems();

        llStrict.removeAllViews();
        llAlmost.removeAllViews();

        // STRICT list: only recipes where nothing is missing
        List<Recipe> strict = RecipeMatcher.findMakeableRecipes(recipes, pantry);
        for (Recipe recipe : strict) {
            addRow(llStrict, recipe.getName(), recipe.getId(), false);
        }
        // Feedback when nothing matches, instead of a blank screen
        tvNoMatch.setVisibility(strict.isEmpty() ? View.VISIBLE : View.GONE);

        // BONUS list: missing exactly one ingredient, clearly separate from the strict list
        List<RecipeMatcher.AlmostRecipe> almost = RecipeMatcher.findAlmostThere(recipes, pantry);
        for (RecipeMatcher.AlmostRecipe a : almost) {
            addRow(llAlmost, a.recipe.getName() + "\nMissing: " + a.missingName,
                    a.recipe.getId(), true);
        }
        tvAlmostEmpty.setVisibility(almost.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // Adds one recipe card to a list container
    private void addRow(LinearLayout container, String text, final int recipeId, final boolean isAlmost) {
        TextView row = (TextView) getLayoutInflater().inflate(R.layout.item_recipe, container, false);
        row.setText(text);
        if (isAlmost) {
            row.setBackgroundResource(R.drawable.bg_card_orange);
        }

        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) row.getLayoutParams();
        int margin = (int) (8 * getResources().getDisplayMetrics().density);
        lp.setMargins(0, 0, 0, margin);
        row.setLayoutParams(lp);

        // Tap a recipe: pass its id to the detail screen through an Intent
        row.setOnClickListener(v -> {
            Intent intent = new Intent(SuggestedRecipesActivity.this, RecipeDetailActivity.class);
            intent.putExtra("recipe_id", recipeId);
            intent.putExtra("can_cook", !isAlmost); // you can only cook it if nothing is missing
            startActivity(intent);
        });
        container.addView(row);
    }
}