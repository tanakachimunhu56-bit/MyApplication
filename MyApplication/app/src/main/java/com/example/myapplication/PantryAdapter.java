package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

public class PantryAdapter extends BaseAdapter {

    private final Context context;
    private List<PantryItem> items;
    private final OnItemActionListener listener;

    public PantryAdapter(Context context, List<PantryItem> items, OnItemActionListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setItems(List<PantryItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public PantryItem getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getId();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_pantry, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        PantryItem item = items.get(position);
        holder.tvName.setText(item.getName());
        holder.tvQuantity.setText(formatQuantity(item.getQuantity()) + " " + item.getUnit());

        if (item.getExpiry() == null || item.getExpiry().isEmpty()) {
            holder.tvExpiry.setText("No expiry date");
        } else {
            holder.tvExpiry.setText("Expires: " + item.getExpiry());
        }

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(item);
            }
        });

        return convertView;
    }

    private String formatQuantity(double q) {
        if (q == Math.floor(q)) {
            return String.valueOf((int) q);
        }
        return String.valueOf(q);
    }

    static class ViewHolder {
        TextView tvName, tvQuantity, tvExpiry;
        ImageButton btnDelete;

        ViewHolder(View view) {
            tvName = view.findViewById(R.id.tvName);
            tvQuantity = view.findViewById(R.id.tvQuantity);
            tvExpiry = view.findViewById(R.id.tvExpiry);
            btnDelete = view.findViewById(R.id.btnDelete);
        }
    }
}
