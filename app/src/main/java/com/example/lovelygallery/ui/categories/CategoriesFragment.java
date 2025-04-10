package com.example.lovelygallery.ui.categories;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lovelygallery.R;
import com.example.lovelygallery.database.AppDatabase;
import com.example.lovelygallery.model.Category;
import com.example.lovelygallery.utils.CategoryUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class CategoriesFragment extends Fragment implements CategoryAdapter.OnCategoryClickListener {

    private RecyclerView recyclerView;
    private CategoryAdapter adapter;
    private List<Category> categoryList = new ArrayList<>();
    private TextView tvEmptyView;
    private FloatingActionButton fabAddCategory;
    private AppDatabase database;

    // Kolory i ikony dla nowych kategorii
    private final String[] colors = {
            "#F44336", "#E91E63", "#9C27B0", "#673AB7", "#3F51B5",
            "#2196F3", "#03A9F4", "#00BCD4", "#009688", "#4CAF50",
            "#8BC34A", "#CDDC39", "#FFEB3B", "#FFC107", "#FF9800", "#FF5722"
    };

    private final String[] iconNames = {
            "ic_category", "ic_photo", "ic_beach", "ic_travel",
            "ic_food", "ic_party", "ic_love", "ic_family", "ic_friends"
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_categories, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        tvEmptyView = view.findViewById(R.id.tvEmptyView);
        fabAddCategory = view.findViewById(R.id.fabAddCategory);

        database = AppDatabase.getInstance(requireContext());

        setupRecyclerView();
        setupFab();

        loadCategories();

        return view;
    }

    private void setupRecyclerView() {
        adapter = new CategoryAdapter(requireContext(), categoryList);
        adapter.setOnCategoryClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupFab() {
        fabAddCategory.setOnClickListener(v -> showCategoryDialog(null));
    }

    private void loadCategories() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Category> categories = database.categoryDao().getAllCategoriesSync();

            // Jeśli nie ma kategorii, dodaj domyślne
            if (categories.isEmpty()) {
                List<Category> defaultCategories = CategoryUtils.createDefaultCategories();
                for (Category category : defaultCategories) {
                    long id = database.categoryDao().insert(category);
                    category.setId(id);
                }
                categories = defaultCategories;
            }

            // Kopiowanie listy kategorii
            final List<Category> finalCategories = new ArrayList<>(categories);

            // Aktualizacja UI na głównym wątku
            requireActivity().runOnUiThread(() -> {
                categoryList.clear();
                categoryList.addAll(finalCategories);
                adapter.notifyDataSetChanged();

                updateEmptyView();
            });
        });
    }

    private void updateEmptyView() {
        if (categoryList.isEmpty()) {
            tvEmptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCategoryClick(Category category, int position) {
        // Przejście do widoku zdjęć w tej kategorii
        Toast.makeText(requireContext(), "Wybrano kategorię: " + category.getName(), Toast.LENGTH_SHORT).show();
        // TODO: Implementacja widoku zdjęć w kategorii
    }

    @Override
    public void onCategoryLongClick(Category category, int position) {
        // Pokazanie menu kontekstowego z opcjami edycji/usunięcia
        showCategoryOptionsDialog(category, position);
    }

    private void showCategoryOptionsDialog(Category category, int position) {
        String[] options = {"Edytuj", "Usuń"};

        new AlertDialog.Builder(requireContext())
                .setTitle(category.getName())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Edytuj
                        showCategoryDialog(category);
                    } else if (which == 1) {
                        // Usuń
                        confirmDeleteCategory(category, position);
                    }
                })
                .setNegativeButton("Anuluj", null)
                .show();
    }

    private void confirmDeleteCategory(Category category, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Usuń kategorię")
                .setMessage("Czy na pewno chcesz usunąć kategorię: " + category.getName() + "?")
                .setPositiveButton("Usuń", (dialog, which) -> {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        database.categoryDao().delete(category);

                        requireActivity().runOnUiThread(() -> {
                            categoryList.remove(position);
                            adapter.notifyItemRemoved(position);
                            updateEmptyView();
                            Toast.makeText(requireContext(), "Kategoria usunięta", Toast.LENGTH_SHORT).show();
                        });
                    });
                })
                .setNegativeButton("Anuluj", null)
                .show();
    }

    private void showCategoryDialog(Category existingCategory) {
        boolean isEdit = existingCategory != null;
        String title = isEdit ? getString(R.string.edit_category_dialog_title) :
                getString(R.string.add_category_dialog_title);

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_edit_category, null);

        EditText etName = dialogView.findViewById(R.id.etCategoryName);
        EditText etDescription = dialogView.findViewById(R.id.etCategoryDescription);
        LinearLayout colorPalette = dialogView.findViewById(R.id.colorPalette);
        LinearLayout iconPalette = dialogView.findViewById(R.id.iconPalette);

        // Użyj tablic referencyjnych dla kolorów i ikon
        final String[] selectedColorRef = {isEdit ? existingCategory.getColor() : colors[0]};
        final String[] selectedIconRef = {isEdit ? existingCategory.getIconName() : iconNames[0]};

        if (isEdit) {
            etName.setText(existingCategory.getName());
            etDescription.setText(existingCategory.getDescription());
        }

        // Tworzenie palety kolorów
        for (String color : colors) {
            ImageView colorView = createColorView(requireContext(), color);
            if (color.equals(selectedColorRef[0])) {
                colorView.setSelected(true);
            }
            colorView.setOnClickListener(v -> {
                // Usuń zaznaczenie ze wszystkich
                for (int i = 0; i < colorPalette.getChildCount(); i++) {
                    colorPalette.getChildAt(i).setSelected(false);
                }
                // Zaznacz wybrany
                v.setSelected(true);
                selectedColorRef[0] = color;
            });
            colorPalette.addView(colorView);
        }

        // Tworzenie palety ikon
        for (String iconName : iconNames) {
            ImageView iconView = createIconView(requireContext(), iconName);
            if (iconName.equals(selectedIconRef[0])) {
                iconView.setSelected(true);
            }
            iconView.setOnClickListener(v -> {
                // Usuń zaznaczenie ze wszystkich
                for (int i = 0; i < iconPalette.getChildCount(); i++) {
                    iconPalette.getChildAt(i).setSelected(false);
                }
                // Zaznacz wybrany
                v.setSelected(true);
                selectedIconRef[0] = iconName;
            });
            iconPalette.addView(iconView);
        }

        // Budowanie dialogu
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setView(dialogView)
                .setPositiveButton(R.string.save, null) // Ustawione jako null, żeby nie zamykać automatycznie
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.show();

        // Zmiana zachowania przycisku "Zapisz", żeby sprawdzić walidację
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            if (name.isEmpty()) {
                etName.setError(getString(R.string.category_name_empty));
                return;
            }

            final Category category;
            if (isEdit) {
                category = existingCategory;
                category.setName(name);
                category.setDescription(description);
                category.setColor(selectedColorRef[0]);
                category.setIconName(selectedIconRef[0]);
            } else {
                category = new Category(name, description, selectedColorRef[0], selectedIconRef[0]);
            }

            Executors.newSingleThreadExecutor().execute(() -> {
                if (isEdit) {
                    database.categoryDao().update(category);
                } else {
                    long id = database.categoryDao().insert(category);
                    category.setId(id);
                }

                requireActivity().runOnUiThread(() -> {
                    if (isEdit) {
                        int index = categoryList.indexOf(existingCategory);
                        if (index != -1) {
                            categoryList.set(index, category);
                            adapter.notifyItemChanged(index);
                        }
                        Toast.makeText(requireContext(), R.string.category_updated, Toast.LENGTH_SHORT).show();
                    } else {
                        categoryList.add(0, category);
                        adapter.notifyItemInserted(0);
                        Toast.makeText(requireContext(), R.string.category_added, Toast.LENGTH_SHORT).show();
                    }
                    updateEmptyView();
                    dialog.dismiss();
                });
            });
        });
    }

    // Metody pomocnicze do tworzenia widoków kolorów i ikon
    private ImageView createColorView(Context context, String colorHex) {
        ImageView view = new ImageView(context);

        // Konfiguracja parametrów layoutu
        int size = getResources().getDimensionPixelSize(R.dimen.color_picker_size);
        int margin = getResources().getDimensionPixelSize(R.dimen.color_picker_margin);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        params.setMargins(margin, margin, margin, margin);
        view.setLayoutParams(params);

        // Tworzenie kształtu kółka
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setColor(Color.parseColor(colorHex));
        shape.setStroke(4, Color.GRAY);

        view.setBackground(shape);

        return view;
    }

    private ImageView createIconView(Context context, String iconName) {
        ImageView view = new ImageView(context);

        // Konfiguracja parametrów layoutu
        int size = getResources().getDimensionPixelSize(R.dimen.icon_picker_size);
        int margin = getResources().getDimensionPixelSize(R.dimen.icon_picker_margin);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        params.setMargins(margin, margin, margin, margin);
        view.setLayoutParams(params);

        // Ustawienie ikony
        try {
            int resId = getResources().getIdentifier(iconName, "drawable", context.getPackageName());
            if (resId != 0) {
                view.setImageResource(resId);
            } else {
                view.setImageResource(R.drawable.ic_category);
            }
        } catch (Exception e) {
            view.setImageResource(R.drawable.ic_category);
        }

        return view;
    }
}