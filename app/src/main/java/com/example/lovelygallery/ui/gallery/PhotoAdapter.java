package com.example.lovelygallery.ui.gallery;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lovelygallery.R;
import com.example.lovelygallery.model.Photo;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    private final Context context;
    private List<Photo> photos;
    private OnPhotoClickListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    public interface OnPhotoClickListener {
        void onPhotoClick(Photo photo, int position);
        void onFavoriteClick(Photo photo, int position);
    }

    public PhotoAdapter(Context context, List<Photo> photos) {
        this.context = context;
        this.photos = photos;
    }

    public void setOnPhotoClickListener(OnPhotoClickListener listener) {
        this.listener = listener;
    }

    public void updatePhotos(List<Photo> newPhotos) {
        this.photos = newPhotos;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        Photo photo = photos.get(position);

        // Ustawienie nazwy zdjęcia
        String displayName = photo.getName();
        if (photo.getDateTaken() != null) {
            displayName += " (" + dateFormat.format(photo.getDateTaken()) + ")";
        }
        holder.tvPhotoName.setText(displayName);

        // Wczytanie obrazu z URI
        if (photo.getUri() != null) {
            try {
                Uri uri = Uri.parse(photo.getUri());
                Glide.with(context)
                        .load(uri)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .centerCrop()
                        .into(holder.ivPhoto);
            } catch (Exception e) {
                // Obsługa błędu ładowania obrazu
                holder.ivPhoto.setImageResource(R.drawable.error_image);
            }
        } else {
            holder.ivPhoto.setImageResource(R.drawable.placeholder_image);
        }

        // Ustawienie ikony ulubionych
        if (photo.isFavorite()) {
            holder.ivFavorite.setImageResource(R.drawable.ic_favorite);
        } else {
            holder.ivFavorite.setImageResource(R.drawable.ic_favorite_border);
        }

        // Ustawienie click listenerów
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPhotoClick(photo, holder.getAdapterPosition());
            }
        });

        holder.ivFavorite.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFavoriteClick(photo, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return photos != null ? photos.size() : 0;
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto;
        TextView tvPhotoName;
        ImageView ivFavorite;

        PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivPhoto);
            tvPhotoName = itemView.findViewById(R.id.tvPhotoName);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);
        }
    }
}