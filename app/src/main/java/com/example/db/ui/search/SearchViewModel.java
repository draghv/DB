package com.example.db.ui.search;

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
public class SearchViewModel extends ViewModel {
    private final MovieRepository repository;
    private final CompositeDisposable disposables = new CompositeDisposable();
    
    private final MutableLiveData<List<Movie>> searchResults = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    @Inject
    public SearchViewModel(MovieRepository repository) {
        this.repository = repository;
    }

    public void searchMovies(String query) {
        if (query == null || query.trim().isEmpty()) {
            searchResults.setValue(null);
            return;
        }

        // Clear previous error
        error.setValue(null);
        isLoading.setValue(true);
        
        disposables.add(repository.searchMovies(query)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                movies -> {
                    searchResults.setValue(movies);
                    isLoading.setValue(false);
                    if (movies.isEmpty()) {
                        error.setValue("No movies found for '" + query + "'");
                    }
                },
                throwable -> {
                    error.setValue("Search failed: " + throwable.getMessage());
                    isLoading.setValue(false);
                }
            ));
    }

    public void bookmarkMovie(Movie movie) {
        disposables.add(repository.bookmarkMovie(movie)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                () -> {
                    movie.setBookmarked(true);
                    List<Movie> currentList = searchResults.getValue();
                    if (currentList != null) {
                        searchResults.setValue(currentList);
                    }
                },
                throwable -> error.setValue(throwable.getMessage())
            ));
    }

    public LiveData<List<Movie>> getSearchResults() {
        return searchResults;
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