package com.example.smartpantrymanager;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private Cursor cursor;
    private Context context;
    private OnRecipeClickListener listener;

    // Interface to handle clicks
    public interface OnRecipeClickListener {
        void onRecipeClick(int recipeId);
    }

    public RecipeAdapter(Context context, Cursor cursor, OnRecipeClickListener listener) {
        this.context = context;
        this.cursor = cursor;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        if (cursor.moveToPosition(position)) {
            int recipeId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            String recipeName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            holder.textView.setText(recipeName);

            // Handle Click
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRecipeClick(recipeId);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return cursor == null ? 0 : cursor.getCount();
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}