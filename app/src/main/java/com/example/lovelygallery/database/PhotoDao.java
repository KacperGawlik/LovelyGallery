package com.example.lovelygallery.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.lovelygallery.model.Photo;

import java.util.Date;
import java.util.List;

@Dao
public interface PhotoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Photo photo);

    @Update
    void update(Photo photo);

    @Delete
    void delete(Photo photo);

    @Query("SELECT * FROM photos ORDER BY dateTaken DESC")
    LiveData<List<Photo>> getAllPhotos();

    @Query("SELECT * FROM photos WHERE isVideo = 0 ORDER BY dateTaken DESC")
    LiveData<List<Photo>> getAllImages();

    @Query("SELECT * FROM photos WHERE isVideo = 1 ORDER BY dateTaken DESC")
    LiveData<List<Photo>> getAllVideos();

    @Query("SELECT * FROM photos WHERE isFavorite = 1 ORDER BY dateTaken DESC")
    LiveData<List<Photo>> getFavoritePhotos();

    @Query("SELECT * FROM photos WHERE categoryIds LIKE '%' || :categoryId || '%'")
    LiveData<List<Photo>> getPhotosByCategory(long categoryId);

    @Query("SELECT * FROM photos WHERE id = :photoId")
    Photo getPhotoById(long photoId);

    @Query("SELECT * FROM photos WHERE dateTaken BETWEEN :startDate AND :endDate ORDER BY dateTaken DESC")
    LiveData<List<Photo>> getPhotosByDateRange(Date startDate, Date endDate);

    @Query("SELECT * FROM photos WHERE hasLocation = 1")
    LiveData<List<Photo>> getPhotosWithLocation();

    @Query("SELECT * FROM photos WHERE isEncrypted = 1")
    LiveData<List<Photo>> getEncryptedPhotos();

    @Query("SELECT * FROM photos WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    LiveData<List<Photo>> searchPhotos(String query);

    @Query("SELECT * FROM photos WHERE categoryIds LIKE '%' || :categoryId || '%'")
    List<Photo> getPhotosByCategoryDirectly(long categoryId);

}