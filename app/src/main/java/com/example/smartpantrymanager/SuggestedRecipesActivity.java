package com.example.smartpantrymanager;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SuggestedRecipesActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        dbHelper = new DatabaseHelper(this);
        RecyclerView rvRecipes = findViewById(R.id.rvRecipes);
        TextView tvNoRecipes = findViewById(R.id.tvNoRecipes);

        rvRecipes.setLayoutManager(new LinearLayoutManager(this));

        // Run strict-matching logic
        List<Integer> validRecipeIds = dbHelper.getSuggestedRecipeIds();

        if (validRecipeIds.isEmpty()) {
            // Shows empty state if nothing matches
            rvRecipes.setVisibility(View.GONE);
            tvNoRecipes.setVisibility(View.VISIBLE);
        } else {
            Cursor cursor = dbHelper.getRecipesByIds(validRecipeIds);

            // Sets up adapter and opens detail screen on click
            RecipeAdapter adapter = new RecipeAdapter(this, cursor, recipeId -> {
                Intent intent = new Intent(SuggestedRecipesActivity.this, RecipeDetailActivity.class);
                intent.putExtra("recipe_id", recipeId);
                startActivity(intent);
            });

            rvRecipes.setAdapter(adapter);
        }
    }
}