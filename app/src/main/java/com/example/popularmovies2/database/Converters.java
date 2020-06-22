package com.example.popularmovies2.database;

import androidx.room.TypeConverter;

import com.example.popularmovies2.data.Review;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class Converters {
    @TypeConverter
    public static Review[] fromString(String value) {
        Type reviewType = new TypeToken<Review[]>() {}.getType();
        return new Gson().fromJson(value, reviewType);
    }
    @TypeConverter
    public static String fromReview(Review[] reviews) {
        Gson gson = new Gson();
        return gson.toJson(reviews);
    }
}
