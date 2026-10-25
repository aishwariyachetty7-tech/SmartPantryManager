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
import android.content.Intent;

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
            startActivity(new Intent(MainActivity.this, AddItemActivity.class));
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
                Intent intent = new Intent(MainActivity.this, AddItemActivity.class);
                intent.putExtra("id", id);
                intent.putExtra("name", name);
                intent.putExtra("quantity", qty);
                intent.putExtra("unit", unit);
                intent.putExtra("expiry", expiry);
                startActivity(intent);
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