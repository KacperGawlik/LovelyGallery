package com.example.lovelygallery.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.security.crypto.EncryptedFile;
import androidx.security.crypto.MasterKeys;

import com.example.lovelygallery.model.Photo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PhotoManager {
    private static final String TAG = "PhotoManager";
    private final Context context;
    private String masterKeyAlias;

    public PhotoManager(Context context) {
        this.context = context;
        try {
            masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Error initializing encryption key", e);
        }
    }

    /**
     * Pobiera wszystkie zdjęcia z urządzenia
     */
    public List<Photo> getAllPhotosFromDevice() {
        List<Photo> photos = new ArrayList<>();

        ContentResolver resolver = context.getContentResolver();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.LATITUDE,
                MediaStore.Images.Media.LONGITUDE
        };

        String sortOrder = MediaStore.Images.Media.DATE_TAKEN + " DESC";

        try (Cursor cursor = resolver.query(uri, projection, null, null, sortOrder)) {
            if (cursor != null) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
                int dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN);
                int pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

                int latColumn = -1;
                int lonColumn = -1;

                if (cursor.getColumnIndex(MediaStore.Images.Media.LATITUDE) != -1) {
                    latColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.LATITUDE);
                    lonColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.LONGITUDE);
                }

                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idColumn);
                    String name = cursor.getString(nameColumn);
                    Date dateTaken = new Date(cursor.getLong(dateColumn));
                    String path = cursor.getString(pathColumn);

                    Uri contentUri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                    Photo photo = new Photo();
                    photo.setName(name);
                    photo.setUri(contentUri.toString());
                    photo.setDateTaken(dateTaken);

                    // Sprawdzenie, czy zdjęcie ma lokalizację
                    if (latColumn != -1 && lonColumn != -1) {
                        float latitude = cursor.getFloat(latColumn);
                        float longitude = cursor.getFloat(lonColumn);

                        if (latitude != 0 || longitude != 0) {
                            photo.setLatitude(latitude);
                            photo.setLongitude(longitude);
                            photo.setHasLocation(true);
                        }
                    }

                    photos.add(photo);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting photos", e);
        }

        return photos;
    }

    /**
     * Szyfruje plik zdjęcia
     */
    public String encryptPhoto(String sourceUri) {
        try {
            Uri uri = Uri.parse(sourceUri);
            InputStream inputStream = context.getContentResolver().openInputStream(uri);

            if (inputStream == null) {
                return null;
            }

            File directory = new File(context.getFilesDir(), "encrypted_photos");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String fileName = System.currentTimeMillis() + ".enc";
            File encryptedFile = new File(directory, fileName);

            EncryptedFile file = new EncryptedFile.Builder(
                    encryptedFile,
                    context,
                    masterKeyAlias,
                    EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build();

            try (OutputStream outputStream = file.openFileOutput()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            inputStream.close();
            return encryptedFile.getAbsolutePath();

        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Error encrypting photo", e);
            return null;
        }
    }

    /**
     * Deszyfruje plik zdjęcia
     */
    public InputStream decryptPhoto(String encryptedPath) {
        try {
            File encryptedFile = new File(encryptedPath);

            EncryptedFile file = new EncryptedFile.Builder(
                    encryptedFile,
                    context,
                    masterKeyAlias,
                    EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build();

            return file.openFileInput();

        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Error decrypting photo", e);
            return null;
        }
    }

    /**
     * Tworzy folder do eksportu zdjęć
     */
    public File createExportDirectory(String categoryName) {
        File baseDir;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            baseDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "LovelyGallery");
        } else {
            baseDir = new File(Environment.getExternalStorageDirectory(), "LovelyGallery");
        }

        File categoryDir = new File(baseDir, categoryName);
        if (!categoryDir.exists()) {
            categoryDir.mkdirs();
        }

        return categoryDir;
    }
}