package com.example.lovelygallery.ui.slideshow;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.viewpager2.widget.ViewPager2;

import com.example.lovelygallery.R;
import com.example.lovelygallery.database.AppDatabase;
import com.example.lovelygallery.database.PhotoDao;
import com.example.lovelygallery.model.Photo;
import com.example.lovelygallery.utils.PhotoManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SlideshowActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private List<Photo> photos;
    private SlideshowAdapter adapter;
    private boolean isPlaying = true;
    private final Handler slideshowHandler = new Handler(Looper.getMainLooper());
    private final Runnable slideshowRunnable = new Runnable() {
        @Override
        public void run() {
            moveToNextSlide();
        }
    };
    private final long slideshowDelay = 3000L; // 3 sekundy między zdjęciami

    private long categoryId = -1;
    private ImageButton playPauseButton;
    private PhotoManager photoManager;
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slideshow);

        photoManager = new PhotoManager(this);

        // Pobieranie ID kategorii z intencji (jeśli istnieje)
        if (getIntent().hasExtra("CATEGORY_ID")) {
            categoryId = getIntent().getLongExtra("CATEGORY_ID", -1);
        }

        setupToolbar();
        setupViewPager();
        setupControls();

        // Ładujemy zdjęcia
        loadPhotos();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.slideshowToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void loadPhotos() {
        // Inicjalizujemy pustą listę, która będzie zaktualizowana po załadowaniu
        photos = new ArrayList<>();
        adapter = new SlideshowAdapter(photos);
        viewPager.setAdapter(adapter);

        if (categoryId != -1L) {
            // Pobierz zdjęcia z konkretnej kategorii z bazy danych używając LiveData
            PhotoDao photoDao = AppDatabase.getInstance(this).photoDao();
            photoDao.getPhotosByCategory(categoryId).observe(this, new Observer<List<Photo>>() {
                @Override
                public void onChanged(List<Photo> photoList) {
                    photos.clear();
                    if (photoList != null) {
                        photos.addAll(photoList);
                    }
                    adapter.notifyDataSetChanged();

                    if (!photos.isEmpty() && isPlaying) {
                        startSlideshow();
                    }
                }
            });
        } else {
            // Pobierz wszystkie zdjęcia z urządzenia za pomocą PhotoManager
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    final List<Photo> devicePhotos = photoManager.getAllPhotosFromDevice();

                    // Aktualizujemy UI w głównym wątku
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            photos.clear();
                            if (devicePhotos != null) {
                                photos.addAll(devicePhotos);
                            }
                            adapter.notifyDataSetChanged();

                            if (!photos.isEmpty() && isPlaying) {
                                startSlideshow();
                            }
                        }
                    });
                }
            });
        }
    }

    private void setupViewPager() {
        viewPager = findViewById(R.id.slideshowViewPager);

        // Opcjonalne: dodaj transformację stron dla efektu przejścia
        viewPager.setPageTransformer(new ZoomOutPageTransformer());
    }

    private void setupControls() {
        // Przycisk Poprzedni
        ImageButton previousButton = findViewById(R.id.btnPrevious);
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToPreviousSlide();
            }
        });

        // Przycisk Odtwórz/Pauza
        playPauseButton = findViewById(R.id.btnPlayPause);
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying) {
                    pauseSlideshow();
                    playPauseButton.setImageResource(R.drawable.ic_play); // Zmień na ikonę odtwarzania
                } else {
                    startSlideshow();
                    playPauseButton.setImageResource(R.drawable.ic_pause); // Zmień na ikonę pauzy
                }
                isPlaying = !isPlaying;
            }
        });

        // Przycisk Następny
        ImageButton nextButton = findViewById(R.id.btnNext);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToNextSlide();
            }
        });
    }

    private void moveToNextSlide() {
        if (photos != null && !photos.isEmpty()) {
            int nextPosition = viewPager.getCurrentItem() + 1;
            if (nextPosition >= photos.size()) {
                nextPosition = 0; // Wróć do początku, jeśli to ostatnie zdjęcie
            }
            viewPager.setCurrentItem(nextPosition, true);
        }

        // Zaplanuj następne przejście, jeśli pokaz slajdów jest aktywny
        if (isPlaying) {
            scheduleSlideshowTransition();
        }
    }

    private void moveToPreviousSlide() {
        if (photos != null && !photos.isEmpty()) {
            int prevPosition = viewPager.getCurrentItem() - 1;
            if (prevPosition < 0) {
                prevPosition = photos.size() - 1; // Przejdź do ostatniego zdjęcia, jeśli to pierwsze
            }
            viewPager.setCurrentItem(prevPosition, true);
        }

        // Zresetuj timer, jeśli pokaz slajdów jest aktywny
        if (isPlaying) {
            resetSlideshowTimer();
        }
    }

    private void startSlideshow() {
        isPlaying = true;
        scheduleSlideshowTransition();
    }

    private void pauseSlideshow() {
        isPlaying = false;
        slideshowHandler.removeCallbacks(slideshowRunnable);
    }

    private void scheduleSlideshowTransition() {
        slideshowHandler.removeCallbacks(slideshowRunnable);
        slideshowHandler.postDelayed(slideshowRunnable, slideshowDelay);
    }

    private void resetSlideshowTimer() {
        slideshowHandler.removeCallbacks(slideshowRunnable);
        slideshowHandler.postDelayed(slideshowRunnable, slideshowDelay);
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseSlideshow(); // Zatrzymaj pokaz slajdów, gdy aktywność jest w tle
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isPlaying) {
            startSlideshow(); // Wznów pokaz slajdów, gdy aktywność wraca na pierwszy plan
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Zakończ aktywność po kliknięciu przycisku powrotu
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Klasa transformacji dla efektu przejścia między slajdami
    private class ZoomOutPageTransformer implements ViewPager2.PageTransformer {
        private static final float MIN_SCALE = 0.85f;
        private static final float MIN_ALPHA = 0.5f;

        @Override
        public void transformPage(@NonNull View view, float position) {
            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();

            if (position < -1) { // Strona jest daleko po lewej
                view.setAlpha(0f);
            } else if (position <= 1) { // Strona jest w zakresie [-1,1]
                // Zmniejszanie strony proporcjonalnie do pozycji
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float vertMargin = pageHeight * (1 - scaleFactor) / 2;
                float horzMargin = pageWidth * (1 - scaleFactor) / 2;

                if (position < 0) {
                    view.setTranslationX(horzMargin - vertMargin / 2);
                } else {
                    view.setTranslationX(horzMargin + vertMargin / 2);
                }

                // Skalowanie strony
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

                // Zmniejszanie przezroczystości
                view.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA));
            } else { // Strona jest daleko po prawej
                view.setAlpha(0f);
            }
        }
    }
}