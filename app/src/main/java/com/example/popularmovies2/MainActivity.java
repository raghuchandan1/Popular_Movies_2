package com.example.popularmovies2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.example.popularmovies2.database.AppDatabase;
import com.example.popularmovies2.database.Movie;
import com.example.popularmovies2.utilities.NetworkUtils;

import static com.example.popularmovies2.utilities.NetworkUtils.buildUrl;

public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterOnClickHandler, LoaderManager.LoaderCallbacks<String> {
    private static final String TAG = "MainActivity";
    private static final String LIFECYCLE_CALLBACKS_TEXT_KEY = "lifecycle_callbacks";
    private static final int MOVIES_SEARCH_LOADER = 1;
    private static final String FETCH_QUERY_URL_EXTRA = "fetch_url";

    private RecyclerView movieTitlesRecyclerView;
    private MovieAdapter movieAdapter;
    private TextView mErrorMessageDisplay;
    //private MovieAdapter movieAdapter2;
    private ProgressBar mLoadingIndicator;
    private String movieDBSearchResults;
    private AppDatabase mDb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        movieTitlesRecyclerView=(RecyclerView)findViewById(R.id.recycler_view);

        /*if ( savedInstanceState != null ){
            if(savedInstanceState.containsKey(LIFECYCLE_CALLBACKS_TEXT_KEY)){

            }
        }*/

        mLoadingIndicator=(ProgressBar)findViewById(R.id.pb_loading_indicator);
        mErrorMessageDisplay=(TextView)findViewById(R.id.tv_error_message_display);
        movieAdapter=new MovieAdapter(this);
        //movieAdapter2=new MovieAdapter(moviesByRating);
        movieTitlesRecyclerView.setAdapter(movieAdapter);
        GridLayoutManager layoutManager=new GridLayoutManager(this,2);
        movieTitlesRecyclerView.setLayoutManager(layoutManager);
        mDb = AppDatabase.getInstance (getApplicationContext ());
        /*if(movieDBSearchResults.equals("")) {
            loadMoviesData();
        }
        else{
            showMoviesDataView();
            movieDBSearchResults=savedInstanceState.getString("MOVIE_SEARCH_RESULTS");
            movieAdapter.setMoviesData(movieDBSearchResults);
            Log.i(TAG,"Loaded from Saved State");
        }*/
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if(hasInternetConnection(this)){
                loadMoviesData();
            }
            else{
                Toast.makeText(this,"Internet not found so Favourites loaded",Toast.LENGTH_LONG).show();
                retrieveFavs();
            }
        }
        else{
            if(hasInternetAccess(this)){
                loadMoviesData();
            }
            else{
                Toast.makeText(this,"No internet found so Favourites loaded",Toast.LENGTH_LONG).show();
                retrieveFavs();
            }
        }

    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean hasInternetConnection(final Context context) {
        final ConnectivityManager connectivityManager = (ConnectivityManager)context.
                getSystemService(Context.CONNECTIVITY_SERVICE);

        final Network network = connectivityManager.getActiveNetwork();
        final NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);

        return capabilities != null
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }
    public boolean hasInternetAccess(Context context) {
        if (isNetworkAvailable()) {
            try {
                HttpURLConnection urlc = (HttpURLConnection)
                        (new URL("http://clients3.google.com/generate_204")
                                .openConnection());
                urlc.setRequestProperty("User-Agent", "Android");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1500);
                urlc.connect();
                return (urlc.getResponseCode() == 204 &&
                        urlc.getContentLength() == 0);
            } catch (IOException e) {
                Log.e(TAG, "Error checking internet connection", e);
            }
        } else {
            Log.d(TAG, "No network available!");
        }
        return false;
    }
    private void loadMoviesData(String sortBy) {
        showMoviesDataView();
        URL url=buildUrl(sortBy);
        Log.i(TAG,url.toString());

        //new MovieDBQueryTask().execute(url);
        Bundle queryBundle = new Bundle();
        queryBundle.putString(FETCH_QUERY_URL_EXTRA,url.toString());

        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> moviesDBLoader = loaderManager.getLoader(MOVIES_SEARCH_LOADER);

        if(moviesDBLoader == null){
            loaderManager.initLoader(MOVIES_SEARCH_LOADER, queryBundle, this);
        }
        else{
            loaderManager.restartLoader(MOVIES_SEARCH_LOADER, queryBundle, this).forceLoad();
        }
    }
    private void loadMoviesData() {
        showMoviesDataView();
        URL url=buildUrl();
        Log.i(TAG,url.toString());
        Bundle queryBundle = new Bundle();
        queryBundle.putString(FETCH_QUERY_URL_EXTRA,url.toString());
        //new MovieDBQueryTask().execute(url);

        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> moviesDBLoader = loaderManager.getLoader(MOVIES_SEARCH_LOADER);

        if(moviesDBLoader == null){
            loaderManager.initLoader(MOVIES_SEARCH_LOADER, queryBundle, this);
        }
        else{
            loaderManager.restartLoader(MOVIES_SEARCH_LOADER, queryBundle, this);
        }
    }
    private void showMoviesDataView() {
        /* First, make sure the error is invisible */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        /* Then, make sure the weather database is visible */
        movieTitlesRecyclerView.setVisibility(View.VISIBLE);
    }
    private void showErrorMessage() {
        /* First, hide the currently visible database */
        movieTitlesRecyclerView.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setText(R.string.error_message);
        mErrorMessageDisplay.setVisibility(View.VISIBLE);

    }
    private void showErrorMessage(String msg) {
        /* First, hide the currently visible database */
        movieTitlesRecyclerView.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setText(msg);
        mErrorMessageDisplay.setVisibility(View.VISIBLE);

    }

    @Override
    public void onClick(Movie movie) {
        Context context=this;
        Class destinationClass=DetailActivity.class;
        Intent intentToStart=new Intent(context,destinationClass);
        intentToStart.putExtra("Movie",movie);
        startActivity(intentToStart);
    }
    public void retrieveFavs(){
        MainViewModel viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication())).get(MainViewModel.class);
        //MainViewModel viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        viewModel.getMovies ().observe (this, (Movie[] favMovies) -> {
            movieAdapter.notifyDataSetChanged ();
            showMoviesDataView();
            //Log.i("FavMovies",favMovies.length+"");
            if(favMovies.length==0){
                showErrorMessage("You have not favourited any movies yet!!");
            }

            movieAdapter.setMoviesData (favMovies);
        });
        /*LiveData<Movie[]> movies = mDb.movieDao().loadAllMovies();
        movies.observe(this, new Observer<Movie[]>() {
            @Override
            public void onChanged(Movie[] movies) {
                movieAdapter.setMoviesData(movies);
            }
        });*/

    }
    @NonNull
    @Override
    public Loader<String> onCreateLoader(int id, @Nullable final Bundle args) {
        return new AsyncTaskLoader<String>(this) {
            @Nullable
            @Override
            public String loadInBackground() {
                assert args != null;
                String fetchQuery = args.getString(FETCH_QUERY_URL_EXTRA);
                if(fetchQuery == null || fetchQuery .equals("") ){
                    return null;
                }
                URL searchUrl = null;
                try {
                    searchUrl = new URL(fetchQuery);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                //String movieDBSearchResults = null;
                try {
                    assert searchUrl != null;
                    movieDBSearchResults = NetworkUtils.getResponseFromHttpUrl(searchUrl);
                    return movieDBSearchResults;
                    //return NetworkUtils.getResponseFromHttpUrl(searchUrl);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
                //return movieDBSearchResults;
            }

            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                if(args==null){
                    return;
                }

                if(movieDBSearchResults!=null && !movieDBSearchResults.equals("")){
                    Log.i(TAG,"Reloaded");
                    deliverResult(movieDBSearchResults);
                }
                else {
                    mLoadingIndicator.setVisibility(View.VISIBLE);
                    forceLoad();
                }
            }

            @Override
            public void deliverResult(@Nullable String data) {
                movieDBSearchResults = data;
                super.deliverResult(data);

            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String data) {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        if (data != null && !data.equals("")) {
            showMoviesDataView();
            movieAdapter.setMoviesData(data);
        }
        else {
            showErrorMessage();
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {

    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemSelected=item.getItemId();
        if(itemSelected==R.id.action_sort_by_popularity){
            //movieAdapter.setMoviesData(null);
            loadMoviesData("popular");
            return true;
        }
        if(itemSelected==R.id.action_sort_by_rating){
            //movieAdapter.setMoviesData(null);
            loadMoviesData("top_rated");
            return true;
        }
        if(itemSelected==R.id.action_favourites){
            retrieveFavs();
        }
        return super.onOptionsItemSelected(item);
    }

    /*@Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

    }*/
    /*public class MovieDBQueryTask extends AsyncTask<URL, Void, String> {

        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }
        // COMPLETED (2) Override the doInBackground method to perform the query. Return the results. (Hint: You've already written the code to perform the query)
        @Override
        protected String doInBackground(URL... params) {
            URL searchUrl = params[0];
            String movieDBSearchResults = null;
            try {
                movieDBSearchResults = NetworkUtils.getResponseFromHttpUrl(searchUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return movieDBSearchResults;
        }

        // COMPLETED (3) Override onPostExecute to display the results in the TextView
        @Override
        protected void onPostExecute(String movieDBSearchResults) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            if (movieDBSearchResults != null && !movieDBSearchResults.equals("")) {
                showMoviesDataView();
                movieAdapter.setMoviesData(movieDBSearchResults);
            }
            else {
                showErrorMessage();
            }
        }
    }*/
}

