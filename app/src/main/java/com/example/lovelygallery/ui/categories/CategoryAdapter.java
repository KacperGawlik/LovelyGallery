package com.example.lovelygallery.ui.categories;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lovelygallery.R;
import com.example.lovelygallery.model.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private final Context context;
    private List<Category> categories;
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category, int position);
        void onCategoryLongClick(Category category, int position);
    }

    public CategoryAdapter(Context context, List<Category> categories) {
        this.context = context;
        this.categories = categories;
    }

    public void setOnCategoryClickListener(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    public void updateCategories(List<Category> newCategories) {
        this.categories = newCategories;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);

        // Ustawienie nazwy i opisu
        holder.tvCategoryName.setText(category.getName());
        holder.tvCategoryDescription.setText(category.getDescription());

        // Tymczasowo ustawiamy licznik zdjęć na 0
        holder.tvPhotoCount.setText(context.getString(R.string.photos_count, 0));

        // Ustawienie koloru tła ikony
        try {
            GradientDrawable background = (GradientDrawable) holder.ivCategoryIcon.getBackground();
            if (background != null && category.getColor() != null) {
                background.setColor(Color.parseColor(category.getColor()));
            }
        } catch (Exception e) {
            // Obsługa błędu koloru
        }

        // Ustawienie ikony kategorii
        try {
            Resources resources = context.getResources();
            String iconName = category.getIconName();
            if (iconName != null && !iconName.isEmpty()) {
                int resId = resources.getIdentifier(iconName, "drawable", context.getPackageName());
                if (resId != 0) {
                    holder.ivCategoryIcon.setImageResource(resId);
                }
            }
        } catch (Exception e) {
            // Obsługa błędu ikony
            holder.ivCategoryIcon.setImageResource(R.drawable.ic_category);
        }

        // Click listenery
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(category, holder.getAdapterPosition());
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onCategoryLongClick(category, holder.getAdapterPosition());
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return categories != null ? categories.size() : 0;
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCategoryIcon;
        TextView tvCategoryName;
        TextView tvCategoryDescription;
        TextView tvPhotoCount;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvCategoryDescription = itemView.findViewById(R.id.tvCategoryDescription);
            tvPhotoCount = itemView.findViewById(R.id.tvPhotoCount);
        }
    }
}