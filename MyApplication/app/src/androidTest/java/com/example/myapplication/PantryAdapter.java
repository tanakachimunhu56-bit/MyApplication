package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// Custom adapter: turns each PantryItem into a row on screen
public class PantryAdapter extends BaseAdapter {

    public interface OnDeleteListener {
        void onDelete(PantryItem item);
    }

    private final Context context;
    private List<PantryItem> items;
    private final OnDeleteListener deleteListener;

    public PantryAdapter(Context context, List<PantryItem> items, OnDeleteListener deleteListener) {
        this.context = context;
        this.items = items;
        this.deleteListener = deleteListener;
    }

    // Swap in new data and redraw the list
    public void setItems(List<PantryItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getId();
    }

    // Called for every visible row
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            row = LayoutInflater.from(context).inflate(R.layout.item_pantry, parent, false);
        }
        final PantryItem item = items.get(position);

        TextView tvName = row.findViewById(R.id.tvName);
        TextView tvQuantity = row.findViewById(R.id.tvQuantity);
        TextView tvExpiry = row.findViewById(R.id.tvExpiry);
        ImageButton btnDelete = row.findViewById(R.id.btnDelete);

        tvName.setText(item.getName());
        tvQuantity.setText(formatQuantity(item.getQuantity()) + " " + item.getUnit());

        // Settings screen switch: highlight items that expire soon
        SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        boolean alertsOn = prefs.getBoolean("expiry_alerts", true);

        String expiry = item.getExpiry();
        if (expiry == null || expiry.isEmpty()) {
            tvExpiry.setText("No expiry date");
            tvExpiry.setTextColor(Color.parseColor("#555555"));
        } else {
            long days = daysUntil(expiry);
            if (alertsOn && days < 0) {
                tvExpiry.setText("Expired: " + expiry);
                tvExpiry.setTextColor(Color.parseColor("#D32F2F"));
            } else if (alertsOn && days <= 3) {
                tvExpiry.setText("Expires soon: " + expiry);
                tvExpiry.setTextColor(Color.parseColor("#D32F2F"));
            } else {
                tvExpiry.setText("Expires: " + expiry);
                tvExpiry.setTextColor(Color.parseColor("#555555"));
            }
        }

        btnDelete.setOnClickListener(v -> deleteListener.onDelete(item));
        return row;
    }

    // Shows 2 instead of 2.0
    private String formatQuantity(double q) {
        if (q == Math.floor(q)) {
            return String.valueOf((int) q);
        }
        return String.valueOf(q);
    }

    // Days from now until the expiry date (negative = already expired)
    private long daysUntil(String expiry) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        format.setLenient(false);
        try {
            Date date = format.parse(expiry);
            double diff = date.getTime() - System.currentTimeMillis();
            return (long) Math.ceil(diff / (24.0 * 60 * 60 * 1000));
        } catch (ParseException e) {
            return Long.MAX_VALUE; // unreadable date: treat as not expiring
        }
    }
}