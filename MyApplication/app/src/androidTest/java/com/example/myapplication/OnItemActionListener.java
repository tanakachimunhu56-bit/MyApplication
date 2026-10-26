package com.example.myapplication;

// Lets MainActivity decide what happens on tap/delete
public interface OnItemActionListener {
    void onEdit(PantryItem item);

    void onDelete(PantryItem item);
}
