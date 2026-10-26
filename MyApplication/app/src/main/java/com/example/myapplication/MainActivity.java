package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private PantryAdapter adapter;
    private ListView listPantry;
    private TextView tvEmpty;
    private EditText etSearch;
    private Button btnSort;

    private final List<PantryItem> allItems = new ArrayList<>();
    private boolean sortByExpiry = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);
        listPantry = findViewById(R.id.listPantry);
        tvEmpty = findViewById(R.id.tvEmpty);
        etSearch = findViewById(R.id.etSearch);
        btnSort = findViewById(R.id.btnSort);
        Button btnAdd = findViewById(R.id.btnAdd);

        adapter = new PantryAdapter(this, new ArrayList<PantryItem>(), item -> confirmDelete(item));
        listPantry.setAdapter(adapter);

        // Tap a row to edit it: the Intent carries the item's data to the edit screen
        listPantry.setOnItemClickListener((parent, view, position, id) -> {
            PantryItem item = (PantryItem) adapter.getItem(position);
            Intent intent = new Intent(MainActivity.this, AddEditIngredientActivity.class);
            intent.putExtra("id", item.getId());
            intent.putExtra("name", item.getName());
            intent.putExtra("quantity", item.getQuantity());
            intent.putExtra("unit", item.getUnit());
            intent.putExtra("expiry", item.getExpiry());
            startActivity(intent);
        });

        btnAdd.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddEditIngredientActivity.class)));

        // Search: filter the list every time the text changes
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilter();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        // Sort button switches between A-Z and expiring first
        btnSort.setOnClickListener(v -> {
            sortByExpiry = !sortByExpiry;
            btnSort.setText(sortByExpiry ? "Sort: Expiring" : "Sort: A-Z");
            applyFilter();
        });

        NavHelper.setup(this, R.id.nav_pantry);
    }

    // Runs every time the screen comes back into view, so the list is always fresh
    @Override
    protected void onResume() {
        super.onResume();
        loadItems();
    }

    private void loadItems() {
        allItems.clear();
        allItems.addAll(db.getAllPantryItems());
        applyFilter();
    }

    // Applies the search text and the chosen sort order, then shows the result
    private void applyFilter() {
        String query = etSearch.getText().toString().trim().toLowerCase();
        List<PantryItem> shown = new ArrayList<>();
        for (PantryItem item : allItems) {
            if (query.isEmpty() || item.getName().toLowerCase().contains(query)) {
                shown.add(item);
            }
        }

        if (sortByExpiry) {
            Collections.sort(shown, (a, b) -> {
                String ea = a.getExpiry() == null ? "" : a.getExpiry();
                String eb = b.getExpiry() == null ? "" : b.getExpiry();
                if (ea.isEmpty() && eb.isEmpty()) return 0;
                if (ea.isEmpty()) return 1;   // no date goes last
                if (eb.isEmpty()) return -1;
                return ea.compareTo(eb);      // yyyy-MM-dd sorts correctly as text
            });
        }

        adapter.setItems(shown);

        if (shown.isEmpty()) {
            tvEmpty.setText(allItems.isEmpty()
                    ? "Your pantry is empty. Tap + Add to start."
                    : "No ingredients match your search.");
            tvEmpty.setVisibility(View.VISIBLE);
            listPantry.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            listPantry.setVisibility(View.VISIBLE);
        }
    }

    private void confirmDelete(PantryItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete ingredient")
                .setMessage("Remove " + item.getName() + " from your pantry?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.deletePantryItem(item.getId());
                    loadItems();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}