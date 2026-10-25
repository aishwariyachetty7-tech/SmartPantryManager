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
        if (clean.equals("grams") || clean.equals("gram")) return "g";
        if (clean.equals("kilograms") || clean.equals("kilogram")) return "kg";
        if (clean.equals("milliliters") || clean.equals("milliliter")) return "ml";
        if (clean.equals("pieces") || clean.equals("piece")) return "pc";
        if (clean.equals("cups") || clean.equals("cup")) return "cup";
        return clean;
    }


    // SEED 15 RECIPES
    private void seedRecipes(SQLiteDatabase db) {
        // Recipe 1
        addSeedRecipe(db, "Oats Porridge", "1. Cook oats with milk in a pot. 2.  Wait for it to soften/get mush. 3. Add some honey and cinnamon.",
                new String[]{"oats", "milk", "honey", "cinnamon"}, new double[]{30, 0.5, 1, 1}, new String[]{"g", "cup", "tbsp", "tsp"});
        // Recipe 2
        addSeedRecipe(db, "Scrambled Eggs", "1. Beat eggs. 2. Fry in pan with butter.",
                new String[]{"egg", "butter"}, new double[]{2, 10}, new String[]{"pc", "g"});
        // Recipe 3
        addSeedRecipe(db, "Cheese Sandwich", "1. Put cheese between bread. 2. Toast.",
                new String[]{"bread", "cheese"}, new double[]{2, 1}, new String[]{"slice", "slice"});
        // Recipe 4
        addSeedRecipe(db, "Chicken Salad", "1. Cook chicken. 2. Chop lettuce. 3. Mix.",
                new String[]{"chicken", "lettuce", "oil"}, new double[]{200, 1, 1}, new String[]{"g", "head", "tbsp"});
        // Recipe 5
        addSeedRecipe(db, "Pancakes", "1. Mix flour, milk, egg. 2. Fry.",
                new String[]{"flour", "milk", "egg"}, new double[]{100, 200, 1}, new String[]{"g", "ml", "pc"});
        // Recipe 6
        addSeedRecipe(db, "Buttered Toast", "1. Toast bread. 2. Add butter.",
                new String[]{"bread", "butter"}, new double[]{1, 5}, new String[]{"slice", "g"});
        // Recipe 7
        addSeedRecipe(db, "Rice and Beans", "1. Boil rice. 2. Heat beans. 3. Mix.",
                new String[]{"rice", "beans"}, new double[]{100, 100}, new String[]{"g", "g"});
        // Recipe 8
        addSeedRecipe(db, "Beef Stir Fry", "1. Cut beef. 2. Fry with veggies.",
                new String[]{"beef", "oil", "onion"}, new double[]{150, 1, 1}, new String[]{"g", "tbsp", "pc"});
        // Recipe 9
        addSeedRecipe(db, "Potato Soup", "1. Boil potatoes. 2. Blend with milk.",
                new String[]{"potato", "milk", "butter"}, new double[]{3, 100, 10}, new String[]{"pc", "ml", "g"});
        // Recipe 10
        addSeedRecipe(db, "Fruit Salad", "1. Chop apples and bananas. 2. Mix.",
                new String[]{"apple", "banana"}, new double[]{1, 1}, new String[]{"pc", "pc"});
        // Recipe 11
        addSeedRecipe(db, "Mac and Cheese", "1. Boil pasta. 2. Melt cheese with milk.",
                new String[]{"pasta", "cheese", "milk"}, new double[]{100, 2, 50}, new String[]{"g", "slice", "ml"});
        // Recipe 12
        addSeedRecipe(db, "Omelette", "1. Beat eggs. 2. Fry with onions.",
                new String[]{"egg", "onion", "oil"}, new double[]{3, 1, 1}, new String[]{"pc", "pc", "tbsp"});
        // Recipe 13
        addSeedRecipe(db, "Garlic Bread", "1. Mix butter and garlic. 2. Spread on bread and bake.",
                new String[]{"bread", "butter", "garlic"}, new double[]{2, 20, 1}, new String[]{"slice", "g", "clove"});
        // Recipe 14
        addSeedRecipe(db, "Tomato Soup", "1. Boil tomatoes. 2. Blend.",
                new String[]{"tomato", "onion", "oil"}, new double[]{4, 1, 1}, new String[]{"pc", "pc", "tbsp"});
        // Recipe 15
        addSeedRecipe(db, "Apple Snack", "1. Slice apple.",
                new String[]{"apple"}, new double[]{1}, new String[]{"pc"});
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