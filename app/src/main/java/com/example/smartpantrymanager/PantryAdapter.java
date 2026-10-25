package com.example.smartpantrymanager;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.ViewHolder> {

    private Cursor cursor;
    private OnItemClickListener listener;

    // Interface for click listeners
    public interface OnItemClickListener {
        void onItemClick(int id, String name, double qty, String unit, String expiry);
        void onItemLongClick(int id);
    }

    public PantryAdapter(Cursor cursor, OnItemClickListener listener) {
        this.cursor = cursor;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (!cursor.moveToPosition(position)) return;

        // To get data from the database columns
        int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
        String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
        double qty = cursor.getDouble(cursor.getColumnIndexOrThrow("quantity"));
        String unit = cursor.getString(cursor.getColumnIndexOrThrow("unit"));
        String expiry = cursor.getString(cursor.getColumnIndexOrThrow("expiry_date"));

        // To set the text on our layout
        holder.tvName.setText(name);
        String details = "Qty: " + qty + " " + unit;
        if (expiry != null && !expiry.isEmpty()) {
            details += " | Expires: " + expiry;
        }
        holder.tvDetails.setText(details);

        // Handle clicks
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(id, name, qty, unit, expiry);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onItemLongClick(id);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return cursor == null ? 0 : cursor.getCount();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDetails;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvItemName);
            tvDetails = itemView.findViewById(R.id.tvItemDetails);
        }
    }
}