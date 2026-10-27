package com.example.smartpantrymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "SmartPantry.db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    public static final String TABLE_PANTRY = "pantry_items";
    public static final String TABLE_RECIPES = "recipes";
    public static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Pantry Table
        db.execSQL("CREATE TABLE " + TABLE_PANTRY + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, " +
                "quantity REAL, " +
                "unit TEXT, " +
                "expiry_date TEXT)");

        // Recipes Table
        db.execSQL("CREATE TABLE " + TABLE_RECIPES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, " +
                "steps TEXT)");

        // Recipe Ingredients Table
        db.execSQL("CREATE TABLE " + TABLE_RECIPE_INGREDIENTS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "recipe_id INTEGER, " +
                "name TEXT, " +
                "quantity REAL, " +
                "unit TEXT)");

        seedRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENTS);
        onCreate(db);
    }


    // PANTRY CRUD OPERATIONS
    // CREATE
    public boolean addPantryItem(String name, double quantity, String unit, String expiryDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("quantity", quantity);
        values.put("unit", unit);
        values.put("expiry_date", expiryDate);

        long result = db.insert(TABLE_PANTRY, null, values);
        return result != -1; // returns true if successful
    }

    // READ
    public Cursor getAllPantryItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_PANTRY, null);
    }

    // UPDATE
    public boolean updatePantryItem(int id, String name, double quantity, String unit, String expiryDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("quantity", quantity);
        values.put("unit", unit);
        values.put("expiry_date", expiryDate);

        int result = db.update(TABLE_PANTRY, values, "id = ?", new String[]{String.valueOf(id)});
        return result > 0;
    }

    // DELETE
    public void deletePantryItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PANTRY, "id = ?", new String[]{String.valueOf(id)});
    }


    // RECIPE CRUD OPERATIONS & STRICT MATCHING

    public Cursor getAllRecipes() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_RECIPES, null);
    }

    public Cursor getRecipeById(int recipeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_RECIPES + " WHERE id = ?", new String[]{String.valueOf(recipeId)});
    }

    public Cursor getIngredientsForRecipe(int recipeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_RECIPE_INGREDIENTS + " WHERE recipe_id = ?", new String[]{String.valueOf(recipeId)});
    }

    // THE CORE STRICT-MATCHING ALGORITHM
    // Returns a list of recipe IDs that the user can make right now
    public List<Integer> getSuggestedRecipeIds() {
        List<Integer> suggestedIds = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Getting all recipes
        Cursor recipeCursor = db.rawQuery("SELECT id FROM " + TABLE_RECIPES, null);
        if (recipeCursor.moveToFirst()) {
            do {
                int recipeId = recipeCursor.getInt(0);

                // Checking if user has EVERY ingredient for this recipe
                if (canMakeRecipe(db, recipeId)) {
                    suggestedIds.add(recipeId);
                }
            } while (recipeCursor.moveToNext());
        }
        recipeCursor.close();
        return suggestedIds;
    }

    private boolean canMakeRecipe(SQLiteDatabase db, int recipeId) {
        // Getting required ingredients for the recipe
        Cursor reqCursor = db.rawQuery("SELECT name, quantity, unit FROM " + TABLE_RECIPE_INGREDIENTS + " WHERE recipe_id = ?", new String[]{String.valueOf(recipeId)});

        if (reqCursor.getCount() == 0) return false;

        boolean canMake = true;
        if (reqCursor.moveToFirst()) {
            do {
                String reqName = reqCursor.getString(0);
                double reqQty = reqCursor.getDouble(1);
                String reqUnit = reqCursor.getString(2);

                // Check pantry for this ingredient
                if (!isIngredientInPantry(db, reqName, reqQty, reqUnit)) {
                    canMake = false;
                    break;
                }
            } while (reqCursor.moveToNext());
        }
        reqCursor.close();
        return canMake;
    }

    private boolean isIngredientInPantry(SQLiteDatabase db, String reqName, double reqQty, String reqUnit) {
        // Handling plurals (e.g. "tomato" vs "tomatoes") - simple normalization: remove 's' at the end and make lowercase
        String normalizedReqName = normalizeName(reqName);
        String normalizedReqUnit = normalizeUnit(reqUnit);

        Cursor pantryCursor = db.rawQuery("SELECT name, quantity, unit FROM " + TABLE_PANTRY, null);
        boolean found = false;

        if (pantryCursor.moveToFirst()) {
            do {
                String pName = pantryCursor.getString(0);
                double pQty = pantryCursor.getDouble(1);
                String pUnit = pantryCursor.getString(2);

                // If names match (ignoring plurals), check quantity
                if (normalizeName(pName).equals(normalizedReqName)) {
                    if (normalizeUnit(pUnit).equals(normalizedReqUnit) && pQty >= reqQty) {
                        found = true;
                        break;
                    }
                }
            } while (pantryCursor.moveToNext());
        }
        pantryCursor.close();
        return found;
    }

    // Turning plural items into singular (e.g. "Tomatoes" into "tomato")
    private String normalizeName(String name) {
        String clean = name.trim().toLowerCase(Locale.ROOT);
        if (clean.endsWith("s") && clean.length() > 3) {
            clean = clean.substring(0, clean.length() - 1);
        }
        return clean;
    }

    // Turning plural measurements into singular (e.g. "Grams" into "g")
    private String normalizeUnit(String unit) {
        String clean = unit.trim().toLowerCase(Locale.ROOT);

        // NEW: Automatically remove the 's' from plurals like "slices" -> "slice"
        if (clean.endsWith("s") && clean.length() > 2) {
            clean = clean.substring(0, clean.length() - 1);
        }

        if (clean.equals("gram")) return "g";
        if (clean.equals("kilogram")) return "kg";
        if (clean.equals("milliliter")) return "ml";
        if (clean.equals("piece")) return "pc";
        if (clean.equals("tablespoon")) return "tbsp";
        if (clean.equals("teaspoon")) return "tsp";
        return clean;
    }


    // SEED 15 RECIPES
    private void seedRecipes(SQLiteDatabase db) {
        // Recipe 1 - Oats Porridge
        addSeedRecipe(db, "Oats Porridge", "1. Cook oats with milk in a pot. 2.  Wait for it to soften/get mush. 3. Drizzle some honey and sprinkle some cinnamon.",
                new String[]{"oats", "milk", "honey", "cinnamon"}, new double[]{30, 125, 10, 1}, new String[]{"g", "ml", "ml", "g"});
        // Recipe 2 - Scrambled Eggs
        addSeedRecipe(db, "Scrambled Eggs", "1. Beat eggs. 2. Fry in pan with butter.",
                new String[]{"egg", "salt", "butter"}, new double[]{2, 5, 10}, new String[]{"pc", "g", "g"});
        // Recipe 3 - Cheese Sandwich
        addSeedRecipe(db, "Cheese Sandwich", "1. Butter bread. 2. Put cheese on one slice of the bread. 3. Place the other slice on top.",
                new String[]{"bread", "butter", "cheese"}, new double[]{2, 2, 2}, new String[]{"slice", "g", "slice"});
        // Recipe 4 - Two Minute Noodles
        addSeedRecipe(db, "Two Minute Noodles", "1. Boil water in the kettle and then put it into the pot. 2. Place noodles from packet into the pot. 3. Once softened, add in the flavoring from the packet. 4. Mix well.",
                new String[]{"water", "noodles"}, new double[]{250, 1}, new String[]{"ml", "pc"});
        // Recipe 5 - Pancakes
        addSeedRecipe(db, "Pancakes", "1. Mix the flour, sugar, milk, egg and vanilla essence until all is incorporated. 2. Using a ladel, place a spoonful of the mixture into the pan. 3. Swirl the batter around the pan and flip it once bubbles form.",
                new String[]{"flour", "sugar", "milk", "egg", "vanilla essence"}, new double[]{250, 30, 200, 1, 5}, new String[]{"g", "g", "ml", "pc", "ml"});
        // Recipe 6 - Buttered Toast with Jam
        addSeedRecipe(db, "Buttered Toast with Jam", "1. Toast bread in the toaster. 2. Spread butter on toast. 3. Add some jam of your choice (preferably strawberry or raspberry) onto the toast as well.",
                new String[]{"bread", "butter", "jam"}, new double[]{1, 5, 5}, new String[]{"slice", "g", "g"});
        // Recipe 7 - Baked Beans Salad
        addSeedRecipe(db, "Baked Beans Salad", "1. On a chopping board, chop some onions and chillies. 2. Empty the can of baked beans into a bowl. 3. Add the chopped onions and chillies. 4. Add some salt to taste.",
                new String[]{"beans", "onions", "chillies", "salt"}, new double[]{400, 0.25, 1, 2}, new String[]{"g", "cup", "pc", "g"});
        // Recipe 8 - Stir Fried Chicken
        addSeedRecipe(db, "Stir Fried Chicken", "1. Cut chicken into small cubes. 2. Marinate with some salt, pepper and mixed herbs. 3. Fry the chicken in a pan with some oil, soy sauce and chopped onions.",
                new String[]{"chicken", "salt", "pepper", "oil", "soy sauce", "onion"}, new double[]{150, 5, 5, 15, 30, 1}, new String[]{"g", "g", "g", "ml", "ml", "pc"});
        // Recipe 9 - Butternut Soup
        addSeedRecipe(db, "Butternut Soup", "1. Chop butternut into smaller pieces. 2. Roast the butternut with garlic and butter. 3. Blend the butternut with milk and a dash of cream. 4. Add salt and freshly ground pepper.",
                new String[]{"butternut", "milk", "butter", "cream", "salt", "pepper"}, new double[]{3, 250, 10, 10, 5, 5}, new String[]{"pc", "ml", "g", "ml", "g", "g"});
        // Recipe 10 - Fruit Salad
        addSeedRecipe(db, "Fruit Salad", "1. Chop the fruit - preferably apples, bananas and oranges. 2. Add some honey. 3. Mix well.",
                new String[]{"apple", "banana", "orange", "honey"}, new double[]{1, 2, 1, 5}, new String[]{"pc", "pc", "pc", "ml"});
        // Recipe 11 - Mac 'n Cheese
        addSeedRecipe(db, "Mac 'n Cheese", "1. Boil pasta (preferably macaroni). 2. In a pot add butter, pepper and milk. 3. Whisk until it reaches a boil, then add the grated cheese. 4. Whisk until it thickens. 5. Add the drained pasta to the sauce and mix well.",
                new String[]{"pasta", "cheese", "butter", "pepper", "milk"}, new double[]{100, 2, 50, 10, 50}, new String[]{"g", "slice", "g", "g", "ml"});
        // Recipe 12 - Bread Pudding
        addSeedRecipe(db, "Bread Pudding", "1. Chop bread into smaller pieces. 2. Melt butter and add to the bread, until all the bread is soaked. 3. Place into an oven-safe dish. 4. In a bowl whisk egg, milk, sugar and vanilla essence until combined. 5. Pour this mixture over the butter-soaked bread. 6. Bake in the oven until firm and golden brown.",
                new String[]{"bread", "butter", "egg", "milk", "sugar", "vanilla essence"}, new double[]{10, 50, 1, 250, 60, 15}, new String[]{"slice", "g", "pc", "ml", "g", "ml"});
        // Recipe 13 - Cheesy Garlic Bread
        addSeedRecipe(db, "Cheesy Garlic Bread", "1. Mix softened butter and crushed garlic. 2. Spread it onto your preferred bread/roll. 3. Sprinkle some cheese on top and bake until the cheese is melted.",
                new String[]{"bread", "butter", "garlic", "cheese"}, new double[]{2, 20, 6, 5}, new String[]{"slice", "g", "g", "g"});
        // Recipe 14 - Creamed-Style Corn Toasties
        addSeedRecipe(db, "Creamed-Style Corn Toasties", "1. Toast your bread until crispy. 2. Chop onions. 3. In a bowl empty the can of pre-cooked cream-style corn and add the onions. 4. Place 3 spoons of the mixture onto the toast.",
                new String[]{"cream style corn", "bread", "onion"}, new double[]{400, 6, 0.5}, new String[]{"g", "slices", "pc"});
        // Recipe 15 - Peanut Butter and Jam Sandwich
        addSeedRecipe(db, "Peanut Butter and Jam Sandwich", "1. Spread peanut butter onto one slice of bread. 2. Spread jam onto the other slice of bread. 3. Put the 2 slices together and cut in half.",
                new String[]{"bread", "peanut butter", "jam"}, new double[]{2, 15, 15}, new String[]{"slice", "g", "g"});
    }

    private void addSeedRecipe(SQLiteDatabase db, String name, String steps, String[] ingNames, double[] ingQtys, String[] ingUnits) {
        ContentValues recipeValues = new ContentValues();
        recipeValues.put("name", name);
        recipeValues.put("steps", steps);
        long recipeId = db.insert(TABLE_RECIPES, null, recipeValues);

        if (recipeId != -1) {
            for (int i = 0; i < ingNames.length; i++) {
                ContentValues ingValues = new ContentValues();
                ingValues.put("recipe_id", recipeId);
                ingValues.put("name", ingNames[i]);
                ingValues.put("quantity", ingQtys[i]);
                ingValues.put("unit", ingUnits[i]);
                db.insert(TABLE_RECIPE_INGREDIENTS, null, ingValues);
            }
        }
    }

    // Getting full recipe details for the IDs that passed the strict match
    public Cursor getRecipesByIds(List<Integer> ids) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (ids.isEmpty()) {
            return db.rawQuery("SELECT * FROM " + TABLE_RECIPES + " WHERE 1=0", null);
        }

        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            placeholders.append(i == 0 ? "?" : ",?");
        }

        String[] args = new String[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            args[i] = String.valueOf(ids.get(i));
        }

        String query = "SELECT * FROM " + TABLE_RECIPES + " WHERE id IN (" + placeholders.toString() + ")";
        return db.rawQuery(query, args);
    }
}