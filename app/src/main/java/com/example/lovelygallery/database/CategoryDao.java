package com.example.lovelygallery.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.lovelygallery.model.Category;

import java.util.List;

@Dao
public interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Category category);

    @Update
    void update(Category category);

    @Delete
    void delete(Category category);

    @Query("SELECT * FROM categories ORDER BY name")
    LiveData<List<Category>> getAllCategories();

    @Query("SELECT * FROM categories WHERE id = :categoryId")
    LiveData<Category> getCategoryById(long categoryId);

    @Query("SELECT * FROM categories WHERE name LIKE '%' || :query || '%'")
    LiveData<List<Category>> searchCategories(String query);

    @Query("SELECT * FROM categories ORDER BY name")
    List<Category> getAllCategoriesSync();

}