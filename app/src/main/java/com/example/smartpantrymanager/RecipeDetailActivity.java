package com.example.smartpantrymanager;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class RecipeDetailActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private int recipeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        dbHelper = new DatabaseHelper(this);

        TextView tvTitle = findViewById(R.id.tvRecipeTitle);
        TextView tvIngredients = findViewById(R.id.tvRecipeIngredients);
        TextView tvSteps = findViewById(R.id.tvRecipeSteps);

        // Getting the recipe ID passed from the previous screen
        recipeId = getIntent().getIntExtra("recipe_id", -1);

        if (recipeId != -1) {
            // Loading Recipe Name and Steps
            Cursor recipeCursor = dbHelper.getRecipeById(recipeId);
            if (recipeCursor.moveToFirst()) {
                String name = recipeCursor.getString(recipeCursor.getColumnIndexOrThrow("name"));
                String steps = recipeCursor.getString(recipeCursor.getColumnIndexOrThrow("steps"));

                tvTitle.setText(name);
                tvSteps.setText(steps);
            }
            recipeCursor.close();

            // Loading Ingredients
            Cursor ingCursor = dbHelper.getIngredientsForRecipe(recipeId);
            StringBuilder ingredientsText = new StringBuilder();

            if (ingCursor.moveToFirst()) {
                do {
                    String ingName = ingCursor.getString(ingCursor.getColumnIndexOrThrow("name"));
                    double ingQty = ingCursor.getDouble(ingCursor.getColumnIndexOrThrow("quantity"));
                    String ingUnit = ingCursor.getString(ingCursor.getColumnIndexOrThrow("unit"));

                    ingredientsText.append("• ").append(ingQty).append(" ").append(ingUnit).append(" ").append(ingName).append("\n");
                } while (ingCursor.moveToNext());
            }
            ingCursor.close();

            tvIngredients.setText(ingredientsText.toString());
        }
    }
}