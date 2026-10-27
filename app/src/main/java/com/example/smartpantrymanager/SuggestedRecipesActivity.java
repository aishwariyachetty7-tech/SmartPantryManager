package com.example.smartpantrymanager;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SuggestedRecipesActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        // Set up the toolbar with a back arrow
        Toolbar toolbar = findViewById(R.id.toolbarSuggested);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dbHelper = new DatabaseHelper(this);
        RecyclerView rvRecipes = findViewById(R.id.rvRecipes);
        TextView tvNoRecipes = findViewById(R.id.tvNoRecipes);

        rvRecipes.setLayoutManager(new LinearLayoutManager(this));

        // Run strict-matching logic
        List<Integer> validRecipeIds = dbHelper.getSuggestedRecipeIds();

        if (validRecipeIds.isEmpty()) {
            rvRecipes.setVisibility(View.GONE);
            tvNoRecipes.setVisibility(View.VISIBLE);
        } else {
            Cursor cursor = dbHelper.getRecipesByIds(validRecipeIds);

            RecipeAdapter adapter = new RecipeAdapter(this, cursor, recipeId -> {
                Intent intent = new Intent(SuggestedRecipesActivity.this, RecipeDetailActivity.class);
                intent.putExtra("recipe_id", recipeId);
                startActivity(intent);
            });

            rvRecipes.setAdapter(adapter);
        }
    }

    // This makes the back arrow actually go back to the main screen
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}