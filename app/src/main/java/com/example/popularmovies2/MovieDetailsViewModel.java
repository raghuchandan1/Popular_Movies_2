package com.example.popularmovies2;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.popularmovies2.database.AppDatabase;
import com.example.popularmovies2.database.Movie;

public class MovieDetailsViewModel extends ViewModel {

    private LiveData<Movie> movie;

    public MovieDetailsViewModel(AppDatabase database, int movieId) {
        movie = database.movieDao ().loadMovieById (movieId);
    }

    public LiveData<Movie> getMovie() {
        return movie;
    }
}