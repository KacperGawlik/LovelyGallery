package com.example.lovelygallery.ui.slideshow;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lovelygallery.R;
import com.example.lovelygallery.model.Photo;

import java.io.File;
import java.util.List;

public class SlideshowAdapter extends RecyclerView.Adapter<SlideshowAdapter.SlideshowViewHolder> {

    private List<Photo> photos;

    public SlideshowAdapter(List<Photo> photos) {
        this.photos = photos;
    }

    @NonNull
    @Override
    public SlideshowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_slideshow, parent, false);
        return new SlideshowViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SlideshowViewHolder holder, int position) {
        Photo photo = photos.get(position);

        // Ładowanie zdjęcia z URI lub ścieżki pliku
        try {
            String photoPath = photo.getUri();

            // Sprawdzamy, czy mamy do czynienia z zaszyfrowanym plikiem
            if (photo.isEncrypted() && photo.getEncryptedPath() != null) {
                // Dla zaszyfrowanych zdjęć użyjemy specjalnego widoku lub informacji
                Glide.with(holder.itemView.getContext())
                        .load(R.drawable.ic_lock) // Ikona kłódki dla zaszyfrowanych zdjęć
                        .into(holder.imageView);
            } else if (photoPath != null) {
                // Sprawdzamy czy mamy URI czy ścieżkę pliku
                Uri photoUri;
                if (photoPath.startsWith("content://") || photoPath.startsWith("file://")) {
                    photoUri = Uri.parse(photoPath);
                } else {
                    photoUri = Uri.fromFile(new File(photoPath));
                }

                Glide.with(holder.itemView.getContext())
                        .load(photoUri)
                        .error(R.drawable.error_image)
                        .into(holder.imageView);
            }
        } catch (Exception e) {
            // W przypadku błędu, wyświetl domyślny obraz błędu
            Glide.with(holder.itemView.getContext())
                    .load(R.drawable.error_image)
                    .into(holder.imageView);
        }
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    public static class SlideshowViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public SlideshowViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.slideshowImageView);
        }
    }
}