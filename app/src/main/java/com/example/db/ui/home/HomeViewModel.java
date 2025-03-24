package com.example.db.ui.home;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.db.data.model.Movie;
import com.example.db.data.repository.MovieRepository;
import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.util.List;
import javax.inject.Inject;
import io.reactivex.rxjava3.core.Completable;

@HiltViewModel
public class HomeViewModel extends ViewModel {
    private static final String TAG = "HomeViewModel";
    private final MovieRepository movieRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<List<Movie>> trendingMovies = new MutableLiveData<>();
    private final MutableLiveData<List<Movie>> nowPlayingMovies = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    private boolean forceRefresh = false;

    @Inject
    public HomeViewModel(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
        Log.d(TAG, "HomeViewModel created");
        loadMovies();
    }

    private void loadMovies() {
        isLoading.setValue(true);
        error.setValue(null);
        
        // If forcing refresh, reset the flag
        boolean refreshing = forceRefresh;
        forceRefresh = false;
        
        Log.d(TAG, "Loading movies, forceRefresh=" + refreshing);
        
        disposables.add(movieRepository.getTrendingMovies(refreshing)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                movies -> {
                    Log.d(TAG, "Trending movies loaded successfully: " + movies.size());
                    trendingMovies.setValue(movies);
                    loadNowPlayingMovies(refreshing);
                },
                throwable -> {
                    Log.e(TAG, "Error loading trending movies", throwable);
                    error.setValue("Failed to load trending movies: " + throwable.getMessage());
                    isLoading.setValue(false);
                    // Try to load now playing even if trending fails
                    loadNowPlayingMovies(refreshing);
                }
            ));
    }

    private void loadNowPlayingMovies(boolean forceRefresh) {
        Log.d(TAG, "Loading now playing movies, forceRefresh=" + forceRefresh);
        
        disposables.add(movieRepository.getNowPlayingMovies(forceRefresh)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                movies -> {
                    Log.d(TAG, "Now playing movies loaded successfully: " + movies.size());
                    nowPlayingMovies.setValue(movies);
                    isLoading.setValue(false);
                },
                throwable -> {
                    Log.e(TAG, "Error loading now playing movies", throwable);
                    error.setValue("Failed to load now playing movies: " + throwable.getMessage());
                    isLoading.setValue(false);
                }
            ));
    }

    public void bookmarkMovie(Movie movie) {
        disposables.add((movie.isBookmarked() ? 
            movieRepository.unbookmarkMovie(movie) :
            movieRepository.bookmarkMovie(movie))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                () -> refreshMovies(),
                throwable -> error.setValue("Failed to update bookmark: " + throwable.getMessage())
            ));
    }

    public void refreshMovies() {
        Log.d(TAG, "Refresh movies requested");
        forceRefresh = true;
        loadMovies();
    }

    public LiveData<List<Movie>> getTrendingMovies() {
        return trendingMovies;
    }

    public LiveData<List<Movie>> getNowPlayingMovies() {
        return nowPlayingMovies;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
} 
