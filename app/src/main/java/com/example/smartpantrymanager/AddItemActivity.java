package com.example.smartpantrymanager;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AddItemActivity extends AppCompatActivity {

    private EditText etName, etQuantity, etUnit, etExpiry;
    private DatabaseHelper dbHelper;
    private int editItemId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        dbHelper = new DatabaseHelper(this);

        TextView tvTitle = findViewById(R.id.tvScreenTitle);
        etName = findViewById(R.id.etName);
        etQuantity = findViewById(R.id.etQuantity);
        etUnit = findViewById(R.id.etUnit);
        etExpiry = findViewById(R.id.etExpiry);
        Button btnSave = findViewById(R.id.btnSave);

        if (getIntent().hasExtra("id")) {
            tvTitle.setText("Edit Pantry Item");
            editItemId = getIntent().getIntExtra("id", -1);

            etName.setText(getIntent().getStringExtra("name"));
            etQuantity.setText(String.valueOf(getIntent().getDoubleExtra("quantity", 0)));
            etUnit.setText(getIntent().getStringExtra("unit"));
            etExpiry.setText(getIntent().getStringExtra("expiry"));
        }

        btnSave.setOnClickListener(v -> saveItem());
    }

    private void saveItem() {
        // Getting the text from the input fields
        String name = etName.getText().toString().trim();
        String qtyString = etQuantity.getText().toString().trim();
        String unit = etUnit.getText().toString().trim();
        String expiry = etExpiry.getText().toString().trim();

        // Input Validation
        if (name.isEmpty()) {
            etName.setError("Name is required");
            return;
        }
        if (qtyString.isEmpty()) {
            etQuantity.setError("Quantity is required");
            return;
        }
        if (unit.isEmpty()) {
            etUnit.setError("Unit is required (e.g., g, pc)");
            return;
        }

        double quantity = Double.parseDouble(qtyString);

        // Saving to Database (Update or Insert)
        boolean success;
        if (editItemId == -1) {
            // Add new item
            success = dbHelper.addPantryItem(name, quantity, unit, expiry);
        } else {
            // Update existing item
            success = dbHelper.updatePantryItem(editItemId, name, quantity, unit, expiry);
        }

        if (success) {
            Toast.makeText(this, "Saved successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error saving item", Toast.LENGTH_SHORT).show();
        }
    }
}