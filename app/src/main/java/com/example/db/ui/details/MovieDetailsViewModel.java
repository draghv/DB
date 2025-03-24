package com.example.db.ui.details;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.db.data.model.Movie;
import com.example.db.data.repository.MovieRepository;
import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import javax.inject.Inject;

@HiltViewModel
public class MovieDetailsViewModel extends ViewModel {
    private final MovieRepository repository;
    private final CompositeDisposable disposables = new CompositeDisposable();
    
    private final MutableLiveData<Movie> movie = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    @Inject
    public MovieDetailsViewModel(MovieRepository repository) {
        this.repository = repository;
    }

    public void loadMovieDetails(int movieId) {
        isLoading.setValue(true);
        disposables.add(repository.getMovieById(movieId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                movieData -> {
                    movie.setValue(movieData);
                    isLoading.setValue(false);
                },
                throwable -> {
                    error.setValue(throwable.getMessage());
                    isLoading.setValue(false);
                }
            ));
    }

    public void bookmarkMovie(Movie movieData) {
        disposables.add(repository.bookmarkMovie(movieData)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                () -> {
                    movieData.setBookmarked(true);
                    movie.setValue(movieData);
                },
                throwable -> error.setValue(throwable.getMessage())
            ));
    }

    public void unbookmarkMovie(Movie movieData) {
        disposables.add(repository.unbookmarkMovie(movieData)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                () -> {
                    movieData.setBookmarked(false);
                    movie.setValue(movieData);
                },
                throwable -> error.setValue(throwable.getMessage())
            ));
    }

    public LiveData<Movie> getMovie() {
        return movie;
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