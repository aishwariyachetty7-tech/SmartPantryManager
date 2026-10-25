package com.example.smartpantrymanager;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private PantryAdapter adapter;
    private TextView tvEmptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.recyclerView);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fabAdd.setOnClickListener(v -> {
            Toast.makeText(this, "Add screen coming next!", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPantryData();
    }

    private void loadPantryData() {
        Cursor cursor = dbHelper.getAllPantryItems();

        // Shows an empty state if no items exist
        if (cursor.getCount() == 0) {
            tvEmptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        // Sets up the adapter with click listeners
        adapter = new PantryAdapter(cursor, new PantryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int id, String name, double qty, String unit, String expiry) {
                Toast.makeText(MainActivity.this, "Edit " + name + " (coming soon)", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemLongClick(int id) {
                dbHelper.deletePantryItem(id);
                loadPantryData(); // Refreshes the list
                Toast.makeText(MainActivity.this, "Item deleted", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setAdapter(adapter);
    }
}