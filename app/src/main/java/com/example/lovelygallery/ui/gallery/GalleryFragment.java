package com.example.lovelygallery.ui.gallery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lovelygallery.R;
import com.example.lovelygallery.database.AppDatabase;
import com.example.lovelygallery.model.Photo;
import com.example.lovelygallery.utils.PhotoManager;
import com.example.lovelygallery.ui.map.OsmMapActivity;
import com.example.lovelygallery.ui.slideshow.SlideshowActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import android.content.Intent;

public class GalleryFragment extends Fragment implements PhotoAdapter.OnPhotoClickListener {

    private RecyclerView recyclerView;
    private PhotoAdapter adapter;
    private List<Photo> photoList = new ArrayList<>();
    private PhotoManager photoManager;
    private AppDatabase database;
    private long currentCategoryId = -1L; // Dodane pole do śledzenia aktualnej kategorii

    // Typ galerii (wszystkie, tylko zdjęcia, tylko wideo, ulubione)
    private int galleryType = 0;

    // Stałe dla typów galerii
    public static final int GALLERY_TYPE_ALL = 0;
    public static final int GALLERY_TYPE_PHOTOS = 1;
    public static final int GALLERY_TYPE_VIDEOS = 2;
    public static final int GALLERY_TYPE_FAVORITES = 3;

    @Override
    public void onPhotoClick(Photo photo, int position) {
        // Wyświetl toast i uruchom aktywność szczegółów zdjęcia
        Toast.makeText(requireContext(), "Kliknięto: " + photo.getName(), Toast.LENGTH_SHORT).show();

        // Uruchom aktywność szczegółów zdjęcia
        Intent intent = new Intent(getActivity(), PhotoDetailActivity.class);
        intent.putExtra("photo_id", photo.getId());
        startActivity(intent);
    }

    public static GalleryFragment newInstance(int galleryType) {
        GalleryFragment fragment = new GalleryFragment();
        Bundle args = new Bundle();
        args.putInt("gallery_type", galleryType);
        fragment.setArguments(args);
        return fragment;
    }

    public static GalleryFragment newInstance(int galleryType, long categoryId) {
        GalleryFragment fragment = new GalleryFragment();
        Bundle args = new Bundle();
        args.putInt("gallery_type", galleryType);
        args.putLong("category_id", categoryId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            galleryType = getArguments().getInt("gallery_type", GALLERY_TYPE_ALL);
            // Pobierz ID kategorii, jeśli istnieje
            if (getArguments().containsKey("category_id")) {
                currentCategoryId = getArguments().getLong("category_id", -1L);
            }
        }

        photoManager = new PhotoManager(requireContext());
        database = AppDatabase.getInstance(requireContext());

        // Włącz menu w tym fragmencie
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        adapter = new PhotoAdapter(requireContext(), photoList);
        adapter.setOnPhotoClickListener(this);
        recyclerView.setAdapter(adapter);

        loadPhotos();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.gallery_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_map) {
            openMap();
            return true;
        } else if (id == R.id.action_slideshow) {
            startSlideshow();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openMap() {
        Intent intent = new Intent(getContext(), OsmMapActivity.class);
        if (currentCategoryId != -1L) {
            intent.putExtra("CATEGORY_ID", currentCategoryId);
        }
        startActivity(intent);
    }

    private void startSlideshow() {
        Intent intent = new Intent(getContext(), SlideshowActivity.class);
        if (currentCategoryId != -1L) {
            intent.putExtra("CATEGORY_ID", currentCategoryId);
        }
        startActivity(intent);
    }

    private void loadPhotos() {
        // Na początek pobierzmy zdjęcia z urządzenia (w produkcyjnej aplikacji
        // powinniśmy to zrobić w tle i obserwować wyniki przez LiveData)
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Photo> devicePhotos;

            // Jeśli mamy wybraną kategorię, pobierz tylko zdjęcia z tej kategorii
            if (currentCategoryId != -1L) {
                devicePhotos = database.photoDao().getPhotosByCategoryDirectly(currentCategoryId);
            } else {
                devicePhotos = photoManager.getAllPhotosFromDevice();
            }

            // Przefiltruj zdjęcia zgodnie z wybranym typem galerii
            List<Photo> filteredPhotos = new ArrayList<>();
            for (Photo photo : devicePhotos) {
                boolean add = false;

                switch (galleryType) {
                    case GALLERY_TYPE_ALL:
                        add = true;
                        break;
                    case GALLERY_TYPE_PHOTOS:
                        add = !photo.isVideo();
                        break;
                    case GALLERY_TYPE_VIDEOS:
                        add = photo.isVideo();
                        break;
                    case GALLERY_TYPE_FAVORITES:
                        add = photo.isFavorite();
                        break;
                }

                if (add) {
                    filteredPhotos.add(photo);
                }
            }

            // Aktualizuj UI na głównym wątku
            requireActivity().runOnUiThread(() -> {
                photoList.clear();
                photoList.addAll(filteredPhotos);
                adapter.notifyDataSetChanged();

                // Pokaż komunikat, jeśli nie ma zdjęć
                if (photoList.isEmpty()) {
                    Toast.makeText(requireContext(), R.string.no_photos, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public void onFavoriteClick(Photo photo, int position) {
        // Zmień status ulubionego
        photo.setFavorite(!photo.isFavorite());
        adapter.notifyItemChanged(position);

        // Zapisz zmieniony stan w bazie danych
        Executors.newSingleThreadExecutor().execute(() -> {
            database.photoDao().update(photo);
        });
    }
}