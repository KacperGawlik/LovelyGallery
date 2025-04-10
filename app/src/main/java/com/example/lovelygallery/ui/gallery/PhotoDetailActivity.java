package com.example.lovelygallery.ui.gallery;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.lovelygallery.R;
import com.example.lovelygallery.database.AppDatabase;
import com.example.lovelygallery.model.Photo;
import com.example.lovelygallery.utils.PhotoManager;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.Executors;

public class PhotoDetailActivity extends AppCompatActivity {

    private AppDatabase database;
    private PhotoManager photoManager;
    private Photo currentPhoto;
    private long photoId;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault());

    private PhotoView photoView;
    private TextView tvPhotoName, tvPhotoDate, tvPhotoLocation, tvCategories;
    private BottomAppBar bottomAppBar;
    private FloatingActionButton fabShare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_detail);

        // Inicjalizacja bazy danych i managera zdjęć
        database = AppDatabase.getInstance(this);
        photoManager = new PhotoManager(this);

        // Inicjalizacja komponentów UI
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        photoView = findViewById(R.id.photoView);
        tvPhotoName = findViewById(R.id.tvPhotoName);
        tvPhotoDate = findViewById(R.id.tvPhotoDate);
        tvPhotoLocation = findViewById(R.id.tvPhotoLocation);
        tvCategories = findViewById(R.id.tvCategories);
        bottomAppBar = findViewById(R.id.bottomAppBar);
        fabShare = findViewById(R.id.fabShare);

        // Konfiguracja bottom app bar
        bottomAppBar.setNavigationOnClickListener(v -> finish());
        bottomAppBar.setOnMenuItemClickListener(this::onOptionsItemSelected);

        // Konfiguracja FAB
        fabShare.setOnClickListener(v -> sharePhoto());

        // Pobranie ID zdjęcia z intencji
        photoId = getIntent().getLongExtra("photo_id", -1);
        if (photoId == -1) {
            Toast.makeText(this, "Błąd: Nie można otworzyć zdjęcia", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Załadowanie danych zdjęcia
        loadPhotoDetails();
    }

    private void loadPhotoDetails() {
        Executors.newSingleThreadExecutor().execute(() -> {
            Photo photo = database.photoDao().getPhotoById(photoId);
            if (photo != null) {
                runOnUiThread(() -> displayPhotoDetails(photo));
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Błąd: Nie znaleziono zdjęcia", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private void displayPhotoDetails(Photo photo) {
        currentPhoto = photo;

        // Ustawienie nazwy zdjęcia
        tvPhotoName.setText(photo.getName());

        // Ustawienie daty
        if (photo.getDateTaken() != null) {
            tvPhotoDate.setText("Data: " + dateFormat.format(photo.getDateTaken()));
            tvPhotoDate.setVisibility(View.VISIBLE);
        } else {
            tvPhotoDate.setVisibility(View.GONE);
        }

        // Ustawienie lokalizacji
        if (photo.hasLocation()) {
            tvPhotoLocation.setText("Lokalizacja: " + photo.getLatitude() + ", " + photo.getLongitude());
            tvPhotoLocation.setVisibility(View.VISIBLE);
        } else {
            tvPhotoLocation.setVisibility(View.GONE);
        }

        // Załadowanie zdjęcia z URI
        if (photo.getUri() != null) {
            try {
                Uri uri = Uri.parse(photo.getUri());
                Glide.with(this)
                        .load(uri)
                        .error(R.drawable.error_image)
                        .into(photoView);
            } catch (Exception e) {
                Toast.makeText(this, "Błąd ładowania zdjęcia", Toast.LENGTH_SHORT).show();
            }
        }

        // Kategorie - w pełnej aplikacji załadowalibyśmy je z bazy danych
        tvCategories.setText("Kategorie: Brak");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_photo_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_encrypt) {
            encryptPhoto();
            return true;
        } else if (id == R.id.action_add_to_category) {
            addToCategory();
            return true;
        } else if (id == R.id.action_delete) {
            deletePhoto();
            return true;
        } else if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sharePhoto() {
        if (currentPhoto != null && currentPhoto.getUri() != null) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(currentPhoto.getUri()));
            startActivity(Intent.createChooser(shareIntent, "Udostępnij zdjęcie"));
        }
    }

    private void encryptPhoto() {
        // Implementacja szyfrowania zdjęcia
        Toast.makeText(this, "Funkcja szyfrowania będzie dostępna wkrótce", Toast.LENGTH_SHORT).show();
    }

    private void addToCategory() {
        // Implementacja dodawania do kategorii
        Toast.makeText(this, "Funkcja dodawania do kategorii będzie dostępna wkrótce", Toast.LENGTH_SHORT).show();
    }

    private void deletePhoto() {
        // Implementacja usuwania zdjęcia
        Toast.makeText(this, "Funkcja usuwania będzie dostępna wkrótce", Toast.LENGTH_SHORT).show();
    }
}