package com.example.db.ui.bookmarks;

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

@HiltViewModel
public class BookmarksViewModel extends ViewModel {
    private final MovieRepository movieRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<List<Movie>> bookmarkedMovies = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isEmpty = new MutableLiveData<>(true);

    @Inject
    public BookmarksViewModel(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
        loadBookmarkedMovies();
    }

    private void loadBookmarkedMovies() {
        isLoading.setValue(true);
        disposables.add(movieRepository.getBookmarkedMovies()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                movies -> {
                    bookmarkedMovies.setValue(movies);
                    isEmpty.setValue(movies.isEmpty());
                    isLoading.setValue(false);
                },
                throwable -> {
                    error.setValue("Error loading bookmarked movies: " + throwable.getMessage());
                    isLoading.setValue(false);
                }
            ));
    }

    public void unbookmarkMovie(Movie movie) {
        disposables.add(movieRepository.unbookmarkMovie(movie)
            .andThen(movieRepository.getBookmarkedMovies())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                updatedMovies -> bookmarkedMovies.setValue(updatedMovies),
                throwable -> error.setValue("Failed to unbookmark movie: " + throwable.getMessage())
            ));
    }

    public LiveData<List<Movie>> getBookmarkedMovies() {
        return bookmarkedMovies;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> getIsEmpty() {
        return isEmpty;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
} 