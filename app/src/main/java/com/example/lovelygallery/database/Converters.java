package com.example.lovelygallery.database;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Converters {
    // Konwersja Date <-> Long
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    // Konwersja List<String> <-> String
    @TypeConverter
    public static List<String> fromString(String value) {
        Type listType = new TypeToken<ArrayList<String>>() {}.getType();
        return value == null ? new ArrayList<>() : new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromStringList(List<String> list) {
        return list == null ? null : new Gson().toJson(list);
    }

    // Konwersja List<Long> <-> String
    @TypeConverter
    public static List<Long> fromLongString(String value) {
        Type listType = new TypeToken<ArrayList<Long>>() {}.getType();
        return value == null ? new ArrayList<>() : new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromLongList(List<Long> list) {
        return list == null ? null : new Gson().toJson(list);
    }
}