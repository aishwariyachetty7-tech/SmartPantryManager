package com.example.smartpantrymanager;

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

        // Run the strict-matching logic to get a list of valid recipe IDs
        List<Integer> validRecipeIds = dbHelper.getSuggestedRecipeIds();

        if (validRecipeIds.isEmpty()) {
            // Show an empty state if nothing matches
            rvRecipes.setVisibility(View.GONE);
            tvNoRecipes.setVisibility(View.VISIBLE);
        } else {
            // Fetch the full details for those IDs
            Cursor cursor = dbHelper.getRecipesByIds(validRecipeIds);
            RecipeAdapter adapter = new RecipeAdapter(this, cursor);
            rvRecipes.setAdapter(adapter);
        }
    }
}