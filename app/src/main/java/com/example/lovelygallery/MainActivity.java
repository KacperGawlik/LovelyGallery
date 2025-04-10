package com.example.lovelygallery;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.example.lovelygallery.ui.gallery.GalleryPagerAdapter;
import com.example.lovelygallery.ui.categories.CategoriesFragment;
import com.example.lovelygallery.ui.camera.CameraFragment;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Sprawdzenie i żądanie uprawnień
        if (!checkPermissions()) {
            requestPermissions();
        }

        // Inicjalizacja komponentów UI
        setupUI();
    }

    private void setupUI() {
        // Inicjalizacja dolnej nawigacji
        bottomNav = findViewById(R.id.bottomNavigation);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_gallery) {
                viewPager.setCurrentItem(0);
                return true;
            } else if (itemId == R.id.navigation_categories) {
                // Przejście do widoku kategorii
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new CategoriesFragment())
                        .addToBackStack(null)
                        .commit();
                return true;
            } else if (itemId == R.id.navigation_camera) {
                // Otwórz aparat
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new CameraFragment())
                        .addToBackStack(null)
                        .commit();
                return true;
            } else if (itemId == R.id.navigation_map) {
                // Obsługa mapy
                return true;
            }
            return false;
        });
        // Konfiguracja paska górnego
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Konfiguracja zakładek
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        // Inicjalizacja adaptera dla ViewPager2
        GalleryPagerAdapter pagerAdapter = new GalleryPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Połączenie TabLayout z ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(R.string.all_photos);
                    break;
                case 1:
                    tab.setText(R.string.photos);
                    break;
                case 2:
                    tab.setText(R.string.videos);
                    break;
                case 3:
                    tab.setText(R.string.favorites);
                    break;
            }
        }).attach();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            // Obsługa wyszukiwania
            return true;
        } else if (id == R.id.action_map) {
            // Przejście do widoku mapy
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean checkPermissions() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) !=
                    PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (!allGranted) {
                Toast.makeText(this, "Potrzebujemy tych uprawnień do działania aplikacji",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
