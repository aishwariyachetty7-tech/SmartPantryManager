package com.example.smartpantrymanager;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private PantryAdapter adapter;
    private TextView tvEmptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(toolbar);

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

        if (cursor.getCount() == 0) {
            tvEmptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

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
                loadPantryData();
                Toast.makeText(MainActivity.this, "Item deleted", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setAdapter(adapter);
    }

    // Creates the "Suggested Recipes" text in the top right
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    // Handles the click event when you tap it
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_recipes) {
            startActivity(new Intent(MainActivity.this, SuggestedRecipesActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}