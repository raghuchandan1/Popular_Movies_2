package com.example.popularmovies2.database;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.example.popularmovies2.data.Review;

// Movie details layout contains title, release date, movie poster, vote average, and plot synopsis.
@Entity
public class Movie implements Parcelable {
    @PrimaryKey
    private int id;

    private String title;
    private String release_date;
    private String poster_path;
    private double vote_average;
    private String overview;
    private int runtime;
    private Review[] reviews;
    //private String trailer_path;

    @Ignore
    protected Movie(Parcel in) {
        id = in.readInt();
        title = in.readString();
        release_date = in.readString();
        poster_path = in.readString();
        vote_average = in.readDouble();
        overview = in.readString();
        runtime = in.readInt();
    }

    public Movie(int id, String title, String release_date, String poster_path, double vote_average, String overview, int runtime, Review[] reviews){
        this.id = id;
        this.title = title;
        this.release_date = release_date;
        this.poster_path = poster_path;
        this.vote_average = vote_average;
        this.overview = overview;
        this.runtime = runtime;
        this.reviews = reviews;
        //this.trailer_path = trailer_path;
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    public int getId(){
        return id;
    }
    public void setId(int id){
        this.id = id;
    }
    public void setOriginalTile(String original_title) {
        this.title = original_title;
    }
    public String getTitle() {
        return title;
    }
    public void setReleaseDate(String release_date) {
        this.release_date = release_date;
    }
    public String getRelease_date() {
        return release_date;
    }
    public String getPoster_path() {
        return poster_path;
    }
    public void setPosterPath(String poster_path) {
        this.poster_path = poster_path;
    }
    public double getVote_average() {
        return vote_average;
    }
    public void setVoteAverage(double vote_average) {
        this.vote_average = vote_average;
    }
    public String getOverview() {
        return overview;
    }
    public void setOverview(String overview) {
        this.overview = overview;
    }
    public int getRuntime(){
        return runtime;
    }
    public void setRuntime(int runtime){
        this.runtime = runtime;
    }

    public Review[] getReviews() {
        return reviews;
    }

    public void setReviews(Review[] reviews) {
        this.reviews = reviews;
    }

    /*public String getTrailer_path() {
        return trailer_path;
    }

    public void setTrailer_path(String trailer_path) {
        this.trailer_path = trailer_path;
    }*/

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(release_date);
        dest.writeString(poster_path);
        dest.writeDouble(vote_average);
        dest.writeString(overview);
        dest.writeInt(runtime);
    }
}
