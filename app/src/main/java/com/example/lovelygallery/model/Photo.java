package com.example.lovelygallery.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.lovelygallery.database.Converters;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity(tableName = "photos")
@TypeConverters(Converters.class)
public class Photo {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private String uri;           // Ścieżka do pliku
    private String thumbnailUri;  // Ścieżka do miniatury
    private String name;          // Nazwa pliku
    private String description;   // Opis dodany przez użytkownika
    private Date dateTaken;       // Data wykonania zdjęcia
    private boolean isFavorite;   // Czy jest ulubione
    private boolean isVideo;      // Czy to wideo
    private boolean isEncrypted;  // Czy jest zaszyfrowane
    private String encryptedPath; // Ścieżka do zaszyfrowanej wersji

    // Lokalizacja
    private double latitude;
    private double longitude;
    private boolean hasLocation;

    // Relacje z kategoriami i tagi
    private List<Long> categoryIds = new ArrayList<>();
    private List<String> personTags = new ArrayList<>(); // "On", "Ona", "Razem"
    private List<String> tags = new ArrayList<>();

    // Konstruktor
    public Photo() {
    }

    // Gettery i settery
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getThumbnailUri() {
        return thumbnailUri;
    }

    public void setThumbnailUri(String thumbnailUri) {
        this.thumbnailUri = thumbnailUri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDateTaken() {
        return dateTaken;
    }

    public void setDateTaken(Date dateTaken) {
        this.dateTaken = dateTaken;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public boolean isVideo() {
        return isVideo;
    }

    public void setVideo(boolean video) {
        isVideo = video;
    }

    public boolean isEncrypted() {
        return isEncrypted;
    }

    public void setEncrypted(boolean encrypted) {
        isEncrypted = encrypted;
    }

    public String getEncryptedPath() {
        return encryptedPath;
    }

    public void setEncryptedPath(String encryptedPath) {
        this.encryptedPath = encryptedPath;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
        this.hasLocation = true;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
        this.hasLocation = true;
    }

    public boolean hasLocation() {
        return hasLocation;
    }

    public void setHasLocation(boolean hasLocation) {
        this.hasLocation = hasLocation;
    }

    public List<Long> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(List<Long> categoryIds) {
        this.categoryIds = categoryIds;
    }

    public List<String> getPersonTags() {
        return personTags;
    }

    public void setPersonTags(List<String> personTags) {
        this.personTags = personTags;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}