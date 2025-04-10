package com.example.lovelygallery.ui.map;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.lovelygallery.R;
import com.example.lovelygallery.database.AppDatabase;
import com.example.lovelygallery.model.Photo;
import com.example.lovelygallery.utils.PhotoManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class OsmMapActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 2;

    private MapView mapView;
    private IMapController mapController;
    private MyLocationNewOverlay myLocationOverlay;
    private List<Photo> photosWithLocation = new ArrayList<>();
    private final Executor executor = Executors.newSingleThreadExecutor();
    private PhotoManager photoManager;
    private long categoryId = -1;
    private boolean locationPermissionGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Konfiguracja osmdroid
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        setContentView(R.layout.activity_osm_map);

        photoManager = new PhotoManager(this);

        // Pobieranie ID kategorii z intencji (jeśli istnieje)
        if (getIntent().hasExtra("CATEGORY_ID")) {
            categoryId = getIntent().getLongExtra("CATEGORY_ID", -1);
        }

        setupToolbar();

        // Inicjalizacja mapy
        mapView = findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        mapController = mapView.getController();
        mapController.setZoom(14.0);

        // Przycisk Moja Lokalizacja
        FloatingActionButton fabMyLocation = findViewById(R.id.fabMyLocation);
        fabMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMyLocation();
            }
        });

        // Sprawdź uprawnienia do lokalizacji i pamięci
        checkPermissions();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.mapToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

            if (categoryId != -1) {
                // Ustaw domyślny tytuł bez pobierania nazwy kategorii
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Mapa zdjęć");
                }
            }
                                }
                            }

    private void checkPermissions() {
        // Sprawdź uprawnienia do lokalizacji
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
            setupLocationOverlay();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        // Sprawdź uprawnienia do zapisu w pamięci (wymagane przez osmdroid dla Android < 10)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_REQUEST_CODE);
            } else {
                // Jeśli mamy uprawnienia do pamięci i lokalizacji, załaduj zdjęcia
                if (locationPermissionGranted) {
                    loadPhotosWithLocation();
                }
            }
        } else {
            // Dla Androida 10+ nie potrzebujemy uprawnienia do pamięci
            if (locationPermissionGranted) {
                loadPhotosWithLocation();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
                setupLocationOverlay();

                // Sprawdź, czy mamy też uprawnienia do pamięci
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED) {
                        loadPhotosWithLocation();
                    }
                } else {
                    loadPhotosWithLocation();
                }
            } else {
                Toast.makeText(this, "Uprawnienie do lokalizacji jest wymagane", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (locationPermissionGranted) {
                    loadPhotosWithLocation();
                }
            } else {
                Toast.makeText(this, "Uprawnienie do pamięci jest wymagane dla map offline", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setupLocationOverlay() {
        // Nakładka "Moja lokalizacja"
        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mapView);
        myLocationOverlay.enableMyLocation();
        mapView.getOverlays().add(myLocationOverlay);
    }

    private void showMyLocation() {
        if (!locationPermissionGranted) {
            checkPermissions();
            return;
        }

        if (myLocationOverlay != null && myLocationOverlay.getMyLocation() != null) {
            mapController.animateTo(myLocationOverlay.getMyLocation());
            mapController.setZoom(16.0);
        } else {
            // Jeśli nie można uzyskać lokalizacji z nakładki, spróbuj LocationManager
            try {
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (lastKnownLocation != null) {
                        GeoPoint myLocation = new GeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                        mapController.animateTo(myLocation);
                        mapController.setZoom(16.0);
                    } else {
                        Toast.makeText(this, "Nie można określić bieżącej lokalizacji", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception e) {
                Toast.makeText(this, "Błąd przy określaniu lokalizacji", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadPhotosWithLocation() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                // Pobierz wszystkie zdjęcia, niezależnie od kategorii
                List<Photo> allPhotos = photoManager.getAllPhotosFromDevice();

                // Filtruj zdjęcia z lokalizacją
                for (Photo photo : allPhotos) {
                    if (photo.hasLocation() && photo.getLatitude() != 0 && photo.getLongitude() != 0) {
                        photosWithLocation.add(photo);
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        addMarkersToMap();
                    }
                });
            }
        });
    }

    private void addMarkersToMap() {
        if (photosWithLocation.isEmpty()) {
            Toast.makeText(this, "Brak zdjęć z informacją o lokalizacji", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<OverlayItem> items = new ArrayList<>();
        ArrayList<GeoPoint> points = new ArrayList<>();

        for (Photo photo : photosWithLocation) {
            GeoPoint point = new GeoPoint(photo.getLatitude(), photo.getLongitude());
            points.add(point);

            OverlayItem item = new OverlayItem(photo.getName(), "Kliknij, aby zobaczyć zdjęcie", point);
            item.setMarker(ContextCompat.getDrawable(this, R.drawable.ic_marker));
            items.add(item);
        }

        // Nakładka z markerami
        ItemizedOverlayWithFocus<OverlayItem> itemOverlay = new ItemizedOverlayWithFocus<>(this, items,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(int index, OverlayItem item) {
                        // Po kliknięciu w marker, znajdź odpowiadające zdjęcie
                        for (Photo photo : photosWithLocation) {
                            GeoPoint point = new GeoPoint(photo.getLatitude(), photo.getLongitude());
                            if (item.getPoint().equals(point)) {
                                // Otwórz szczegóły zdjęcia
                                Toast.makeText(OsmMapActivity.this, "Otwieranie zdjęcia: " + photo.getName(), Toast.LENGTH_SHORT).show();

                                // TODO: Dodaj kod do otwierania szczegółów zdjęcia
                                // Intent intent = new Intent(OsmMapActivity.this, PhotoDetailActivity.class);
                                // intent.putExtra("PHOTO_ID", photo.getId());
                                // startActivity(intent);

                                break;
                            }
                        }
                        return true;
                    }

                    @Override
                    public boolean onItemLongPress(int index, OverlayItem item) {
                        return false;
                    }
                });

        itemOverlay.setFocusItemsOnTap(true);
        mapView.getOverlays().add(itemOverlay);

        // Dopasuj mapę do wszystkich punktów
        if (!points.isEmpty()) {
            BoundingBox boundingBox = BoundingBox.fromGeoPoints(points);
            mapView.zoomToBoundingBox(boundingBox, true, 100);
        }

        mapView.invalidate();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Zakończ aktywność po kliknięciu przycisku powrotu
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myLocationOverlay != null) {
            myLocationOverlay.disableMyLocation();
        }
    }
}