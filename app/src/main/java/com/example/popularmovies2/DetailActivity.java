package com.example.popularmovies2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.popularmovies2.data.Review;
import com.example.popularmovies2.data.Video;
import com.example.popularmovies2.database.AppDatabase;
import com.example.popularmovies2.database.Movie;
import com.example.popularmovies2.utilities.NetworkUtils;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String> {
    private RecyclerView reviewsRecyclerView;
    private ReviewAdapter reviewAdapter;
    private Movie movie;
    private AppDatabase mDb;
    private static final int VIDEO_LOADER=2;
    private  static final int REVIEW_LOADER=3;
    private Button trailerButton;
    private ToggleButton favouriteButton;
    private Review [] reviews;
    private Video[] videos;
    //private String trailer_path;
    boolean entryGranted = false;
    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Intent intent = getIntent();
        final Movie movie = intent.getParcelableExtra("Movie");
        this.movie = movie;
        reviewsRecyclerView = (RecyclerView)findViewById(R.id.tv_review_recycler_view);
        reviewAdapter=new ReviewAdapter();
        reviewsRecyclerView.setAdapter(reviewAdapter);
        GridLayoutManager layoutManager=new GridLayoutManager(this,1);
        reviewsRecyclerView.setLayoutManager(layoutManager);
        assert movie != null;
        getReviews(movie);
        //setTitle(movie.getOriginalTile());
        TextView movieTitleView = (TextView) findViewById(R.id.tv_movie_title_detail);
        ImageView moviePosterView = (ImageView) findViewById(R.id.tv_movie_poster_detail);
        TextView movieReleaseDateView = (TextView) findViewById(R.id.tv_release_date_detail);
        //TextView movieLengthView = (TextView) findViewById(R.id.tv_length_detail);
        TextView movieVoteAverageView = (TextView) findViewById(R.id.tv_vote_average_detail);
        TextView moviePlotSynopsisView = (TextView) findViewById(R.id.tv_plot_synopsis_detail);

        mDb = AppDatabase.getInstance(getApplicationContext());

        if (!getIntent().hasExtra("Movie")) {
            closeOnError();
            return;
        }

        String movieTitle = movie.getTitle();
        //Log.i("DetailActivity",movieTitle);
        movieTitleView.setText(movie.getTitle());
        movieReleaseDateView.setText(movie.getRelease_date());
        //movieLengthView.setText(String.format("%d min", movie.getRuntime()));
        movieVoteAverageView.setText(String.format("%s/10", movie.getVote_average()));
        moviePlotSynopsisView.setText(movie.getOverview());
        int width  = Resources.getSystem().getDisplayMetrics().widthPixels;
        //int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w342/";
        Picasso.get() .load(IMAGE_BASE_URL +movie.getPoster_path()).placeholder(R.drawable.ic_movie_icon).resize((int)(width),0).into(moviePosterView);
        String MOVIE_VIDEOS_BASE = "https://api.themoviedb.org/3/movie";

        trailerButton = (Button) findViewById(R.id.tv_trailer_button);
        trailerButton.setOnClickListener(v -> {
            entryGranted = true;
            getVideos(movie);

        });
        //LiveData<Movie> findMovie = mDb.movieDao().loadMovieById(movie.getId());
        favouriteButton = (ToggleButton) findViewById(R.id.tv_favourite_toggle);
        //Log.i("Find Movie",findMovie.getId()+"");
        setUpFavoriteButton();

        favouriteButton.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            if (isChecked) {
                // Toggle is Enabled
                favouriteButton.getTextOn ();
                onFavoriteButtonClicked ();
            } else {
                // Toggle is disabled
                favouriteButton.setTextColor (Color.parseColor("#FFFFFF"));
                favouriteButton.getTextOff();

                AppExecutors.getInstance().diskIO().execute(() -> mDb.movieDao().deleteMovie(movie));
            }
        });


        //finish();
    }
    /*public void onFavButtonClicked(){
        String state = getState();
        final Movie favMovie = new Movie(movie.getId(),movie.getTitle(),movie.getRelease_date(),movie.getPoster_path(),movie.getVote_average(),movie.getOverview(),movie.getRuntime());
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDb.movieDao().insertMovie(favMovie);
            }
        });
    }*/
    /*public void deleteFavMovie(Movie favMovie){
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDb.movieDao().deleteMovie(favMovie);
            }
        });
    }*/
    private void closeOnError() {
        finish();
        Toast.makeText(this, R.string.detail_error_message, Toast.LENGTH_SHORT).show();
    }
    public void playVideo(String key){

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + key));

        // Check if the youtube app exists on the device
        if (intent.resolveActivity(getPackageManager()) == null) {
            // If the youtube app doesn't exist, then use the browser
            intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://www.youtube.com/watch?v=" + key));
        }

        startActivity(intent);
    }


    public Loader<String> onCreateLoader(final int id, @Nullable final Bundle args) {
        return new AsyncTaskLoader<String>(this) {
            @Nullable
            @Override
            public String loadInBackground() {
                if(id == VIDEO_LOADER) {
                    assert args != null;
                    String videoQuery = args.getString("video_query");
                    if (videoQuery == null || videoQuery.equals("")) {
                        return null;
                    }
                    URL videoUrl = null;
                    try {
                        videoUrl = new URL(videoQuery);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    String videoResults;
                    try {
                        assert videoUrl != null;
                        videoResults = NetworkUtils.getResponseFromHttpUrl(videoUrl);
                        return videoResults;
                        //return NetworkUtils.getResponseFromHttpUrl(searchUrl);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
                else if(id == REVIEW_LOADER) {
                    Log.i("OnCreateLoader", "Entered");
                    assert args != null;
                    String reviewQuery = args.getString("reviews_query");
                    assert reviewQuery != null;
                    Log.i("ReviewQuery",reviewQuery);
                    if (reviewQuery.equals("")) {
                        return null;
                    }
                    URL reviewUrl = null;
                    try {
                        reviewUrl = new URL(reviewQuery);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    String reviewResults;
                    try {
                        assert reviewUrl != null;
                        reviewResults = NetworkUtils.getResponseFromHttpUrl(reviewUrl);
                        assert reviewResults != null;
                        Log.i("InBackground",reviewResults);
                        return reviewResults;
                        //return NetworkUtils.getResponseFromHttpUrl(searchUrl);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
                else{
                    return null;
                }
            }

            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                if(args==null){
                    return;
                }
                forceLoad();
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String data) {
        if(loader.getId() == VIDEO_LOADER) {
            if (entryGranted) {
                Log.i("In Loader", "onLoadFinishedCalled");
                if (data != null && !data.equals("")) {
                    Gson gson = new Gson();
                    //JSONObject json=new JSONObject(jsonData);
                    JsonObject jsonObject = new Gson().fromJson(data, JsonObject.class);
                    JsonElement results = jsonObject.get("results");
                    //Log.i("MovieAdapter",results.toString());

                    videos = gson.fromJson(results.toString(), Video[].class);
                    Log.i("Results", results.toString());
                    //notifyDataSetChanged();
                    //videos[0].setKey(null);
                    if (videos[0].getKey() == null || videos[0].getKey().equals("")) {
                        String msg = "No Trailers Found!";
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                    } else {
                        //movie.setTrailer_path(videos[0].getKey());
                        playVideo(videos[0].getKey());
                        //finish();
                    }
                    entryGranted = false;
                }
            }
        }
        else if(loader.getId() == REVIEW_LOADER){
            Log.i("onLoadFinished","Entered");
            if (data != null && !data.equals("")) {
                Log.i("ReviewLoader","Finished loading reviews");
                Gson gson = new Gson();
                //JSONObject json=new JSONObject(jsonData);
                JsonObject jsonObject = new Gson().fromJson(data, JsonObject.class);
                JsonElement results = jsonObject.get("results");
                Log.i("ReviewAdapter",results.toString());

                reviews = gson.fromJson(results.toString(), Review[].class);
                movie.setReviews(reviews);
                reviewAdapter.setReviewsData(data);
            }
        }
        LoaderManager.getInstance( this ).destroyLoader( loader.getId() );
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {

    }
    public void getVideos(Movie movie){
        URL videosURL = NetworkUtils.getVideosURL(movie.getId());
        Log.i("DetailActivity",videosURL.toString());

        Bundle queryBundle = new Bundle();
        queryBundle.putString("video_query",videosURL.toString());

        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> videoLoader = loaderManager.getLoader(VIDEO_LOADER);

        if(videoLoader == null){
            loaderManager.initLoader(VIDEO_LOADER, queryBundle, this);
        }
        else{
            loaderManager.restartLoader(VIDEO_LOADER, queryBundle, this);
        }
    }
    public void getReviews(Movie movie){
        URL reviewsURL = NetworkUtils.getReviewsURL(movie.getId());
        Log.i("Reviews URL",reviewsURL.toString());
        //Log.i("DetailActivity",reviewsURL.toString());

        Bundle queryBundle = new Bundle();
        queryBundle.putString("reviews_query",reviewsURL.toString());

        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> reviewLoader = loaderManager.getLoader(REVIEW_LOADER);

        if(reviewLoader == null){
            Log.i("ReviewLoader","Calling Loader");
            loaderManager.initLoader(REVIEW_LOADER, queryBundle, this);
        }
        else{
            loaderManager.restartLoader(REVIEW_LOADER, queryBundle, this);
        }
    }
    private void setUpFavoriteButton () {
        MovieDetailsViewModelFactory factory =
                new MovieDetailsViewModelFactory (mDb, movie.getId ());
        final MovieDetailsViewModel viewModel =
                new ViewModelProvider(this, factory).get(MovieDetailsViewModel.class);

        viewModel.getMovie ().observe (this, new Observer<Movie>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onChanged(@Nullable Movie movieInDb) {
                viewModel.getMovie ().removeObserver (this);

                if (movieInDb == null) {
                    favouriteButton.setTextColor (Color.parseColor("#FFFFFF"));
                    favouriteButton.setChecked (false);
                    favouriteButton.getTextOff();
                } else if ((movie.getId () == movieInDb.getId ()) && !favouriteButton.isChecked ()){
                    Log.i("Insertion","Inserting again");
                    movie = movieInDb;
                    movie.setReviews(reviews);
                    favouriteButton.setChecked (true);
                    favouriteButton.setText("Favourited!");
                    favouriteButton.setTextColor (Color.parseColor("#FFFF00"));
                } else {
                    favouriteButton.setTextColor (Color.parseColor("#FFFFFF"));
                    favouriteButton.setChecked (false);
                    favouriteButton.getTextOff();
                }
            }
        });
    }
    public void onFavoriteButtonClicked() {
        //final Movie movie = getIntent().getExtras().getParcelable ( "movie");
        Movie favMovie = new Movie(movie.getId(),movie.getTitle(),movie.getRelease_date(),movie.getPoster_path(),movie.getVote_average(),movie.getOverview(),movie.getRuntime(),movie.getReviews());
        //mDb.movieDao().insertMovie(favMovie);
        AppExecutors.getInstance ().diskIO ().execute (() -> mDb.movieDao ().insertMovie (favMovie));
    }
}
