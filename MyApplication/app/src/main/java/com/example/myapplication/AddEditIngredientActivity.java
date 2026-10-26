package com.example.myapplication;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class AddEditIngredientActivity extends AppCompatActivity {

    private static final String[] UNITS = {"g", "kg", "ml", "l", "pcs", "tsp", "tbsp", "cup"};

    private DatabaseHelper db;
    private EditText etName, etQuantity, etExpiry;
    private Spinner spinnerUnit;
    private int editId = -1; // -1 means "adding a new item"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return windowInsets;
        });

        db = new DatabaseHelper(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        etName = findViewById(R.id.etName);
        etQuantity = findViewById(R.id.etQuantity);
        etExpiry = findViewById(R.id.etExpiry);
        spinnerUnit = findViewById(R.id.spinnerUnit);
        Button btnSave = findViewById(R.id.btnSave);

        spinnerUnit.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, UNITS));

        // If the Intent carried an id, we are editing, so pre-fill the form
        editId = getIntent().getIntExtra("id", -1);
        if (editId != -1) {
            setTitle("Edit Ingredient");
            etName.setText(getIntent().getStringExtra("name"));
            double q = getIntent().getDoubleExtra("quantity", 0);
            etQuantity.setText(q == Math.floor(q) ? String.valueOf((int) q) : String.valueOf(q));
            etExpiry.setText(getIntent().getStringExtra("expiry"));
            String unit = getIntent().getStringExtra("unit");
            for (int i = 0; i < UNITS.length; i++) {
                if (UNITS[i].equals(unit)) {
                    spinnerUnit.setSelection(i);
                    break;
                }
            }
        } else {
            setTitle("Add Ingredient");
        }

        btnSave.setOnClickListener(v -> saveIngredient());
    }

    private void saveIngredient() {
        String name = etName.getText().toString().trim();
        String qtyText = etQuantity.getText().toString().trim();
        String expiry = etExpiry.getText().toString().trim();
        String unit = spinnerUnit.getSelectedItem().toString();

        // Validation
        if (name.isEmpty()) {
            etName.setError("Please enter an ingredient name");
            return;
        }
        if (qtyText.isEmpty()) {
            etQuantity.setError("Please enter a quantity");
            return;
        }
        double quantity;
        try {
            quantity = Double.parseDouble(qtyText);
        } catch (NumberFormatException e) {
            etQuantity.setError("Enter a valid number");
            return;
        }
        if (quantity <= 0) {
            etQuantity.setError("Quantity must be greater than 0");
            return;
        }
        if (!expiry.isEmpty() && !isValidDate(expiry)) {
            etExpiry.setError("Use the format yyyy-MM-dd, e.g. 2026-12-31");
            return;
        }

        if (editId == -1) {
            db.addPantryItem(name, quantity, unit, expiry);
            Toast.makeText(this, "Ingredient added", Toast.LENGTH_SHORT).show();
        } else {
            db.updatePantryItem(editId, name, quantity, unit, expiry);
            Toast.makeText(this, "Ingredient updated", Toast.LENGTH_SHORT).show();
        }
        finish(); // go back to the list
    }

    // Strict date check (rejects things like 2026-13-45)
    private boolean isValidDate(String text) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        format.setLenient(false);
        try {
            format.parse(text);
            return true;
        } catch (java.text.ParseException e) {
            return false;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
