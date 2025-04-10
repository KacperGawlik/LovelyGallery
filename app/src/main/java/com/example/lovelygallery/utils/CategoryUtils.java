package com.example.lovelygallery.utils;

import android.content.Context;
import android.graphics.Color;

import com.example.lovelygallery.model.Category;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CategoryUtils {

    // Domyślne kolory dla kategorii
    private static final String[] DEFAULT_COLORS = {
            "#F44336", // Red
            "#E91E63", // Pink
            "#9C27B0", // Purple
            "#673AB7", // Deep Purple
            "#3F51B5", // Indigo
            "#2196F3", // Blue
            "#03A9F4", // Light Blue
            "#00BCD4", // Cyan
            "#009688", // Teal
            "#4CAF50", // Green
            "#8BC34A", // Light Green
            "#CDDC39", // Lime
            "#FFEB3B", // Yellow
            "#FFC107", // Amber
            "#FF9800", // Orange
            "#FF5722"  // Deep Orange
    };

    // Domyślne ikony dla kategorii
    private static final String[] DEFAULT_ICONS = {
            "ic_category",
            "ic_photo",
            "ic_camera",
            "ic_favorite",
            "ic_beach",
            "ic_food",
            "ic_travel",
            "ic_holiday",
            "ic_party",
            "ic_family",
            "ic_friends",
            "ic_love"
    };

    // Tworzy domyślne kategorie dla nowej instalacji
    public static List<Category> createDefaultCategories() {
        List<Category> categories = new ArrayList<>();

        // Domyślne kategorie
        categories.add(new Category("Rocznica", "Zdjęcia z rocznic", DEFAULT_COLORS[0], "ic_love"));
        categories.add(new Category("Wakacje", "Zdjęcia z wakacji", DEFAULT_COLORS[5], "ic_beach"));
        categories.add(new Category("Restauracje", "Zdjęcia z restauracji i kawiarni", DEFAULT_COLORS[10], "ic_food"));
        categories.add(new Category("Podróże", "Zdjęcia z wyjazdów i podróży", DEFAULT_COLORS[15], "ic_travel"));
        categories.add(new Category("Święta", "Zdjęcia ze świąt i uroczystości", DEFAULT_COLORS[8], "ic_holiday"));

        return categories;
    }

    // Losowy kolor dla nowej kategorii
    public static String getRandomColor() {
        Random random = new Random();
        return DEFAULT_COLORS[random.nextInt(DEFAULT_COLORS.length)];
    }

    // Losowa ikona dla nowej kategorii
    public static String getRandomIcon() {
        Random random = new Random();
        return DEFAULT_ICONS[random.nextInt(DEFAULT_ICONS.length)];
    }

    // Konwertuje kolor z formatu hex na int
    public static int parseColor(String colorString) {
        try {
            return Color.parseColor(colorString);
        } catch (Exception e) {
            return Color.GRAY; // Domyślny kolor w przypadku błędu
        }
    }
}