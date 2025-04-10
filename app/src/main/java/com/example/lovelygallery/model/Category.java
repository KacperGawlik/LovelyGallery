package com.example.lovelygallery.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

@Entity(tableName = "categories")
public class Category {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private String name;        // Nazwa kategorii (np. "Rocznica", "Wakacje")
    private String description;  // Opcjonalny opis
    private String color;        // Kolor dla wyświetlania (np. "#FF5733")
    private String iconName;     // Nazwa ikony

    // Konstruktory
    public Category() {
    }

    @Ignore
    public Category(String name, String description, String color, String iconName) {
        this.name = name;
        this.description = description;
        this.color = color;
        this.iconName = iconName;
    }

    // Gettery i settery
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }
}