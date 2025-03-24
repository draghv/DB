package com.example.db.data.repository;

import android.util.Log;
import com.example.db.data.api.MovieApiService;
import com.example.db.data.local.MovieDao;
import com.example.db.data.local.MovieEntity;
import com.example.db.data.local.MovieMapper;
import com.example.db.data.model.Movie;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class MovieRepository {
    private static final String TAG = "MovieRepo";
    private static final String DEFAULT_LANGUAGE = "en-US";

    private final MovieApiService movieApiService;
    private final MovieDao movieDao;
    private final MovieMapper movieMapper;
    private final String apiKey;

    @Inject
    public MovieRepository(
        MovieApiService movieApiService,
        MovieDao movieDao,
        MovieMapper movieMapper,
        String apiKey
    ) {
        this.movieApiService = movieApiService;
        this.movieDao = movieDao;
        this.movieMapper = movieMapper;
        this.apiKey = apiKey;
        
        Log.d(TAG, "Repository initialized with API key: " + (apiKey != null ? apiKey.substring(0, 5) + "..." : "null"));
    }

    public Single<List<Movie>> getTrendingMovies() {
        Log.d(TAG, "Getting trending movies");
        return movieDao.getTrendingMovies()
                .map(entities -> {
                    Log.d(TAG, "Found " + entities.size() + " trending movies in local DB");
                    return movieMapper.toDomainList(entities);
                })
                .onErrorResumeNext(throwable -> {
                    Log.e(TAG, "Error getting trending movies from DB: " + throwable.getMessage());
                    return movieApiService.getTrendingMovies(DEFAULT_LANGUAGE, 1)
                        .map(response -> {
                            Log.d(TAG, "API returned " + (response.getResults() != null ? response.getResults().size() : 0) + " trending movies");
                            return response.getResults();
                        })
                        .doOnSuccess(movies -> saveMovies(movies, true))
                        .doOnError(error -> Log.e(TAG, "API error getting trending movies: " + error.getMessage()));
                });
    }

    public Single<List<Movie>> getNowPlayingMovies() {
        Log.d(TAG, "Getting now playing movies");
        return movieDao.getNowPlayingMovies()
                .map(entities -> {
                    Log.d(TAG, "Found " + entities.size() + " now playing movies in local DB");
                    return movieMapper.toDomainList(entities);
                })
                .onErrorResumeNext(throwable -> {
                    Log.e(TAG, "Error getting now playing movies from DB: " + throwable.getMessage());
                    return movieApiService.getNowPlayingMovies(DEFAULT_LANGUAGE, 1)
                        .map(response -> {
                            Log.d(TAG, "API returned " + (response.getResults() != null ? response.getResults().size() : 0) + " now playing movies");
                            return response.getResults();
                        })
                        .doOnSuccess(movies -> saveMovies(movies, false))
                        .doOnError(error -> Log.e(TAG, "API error getting now playing movies: " + error.getMessage()));
                });
    }

    public Single<List<Movie>> searchMovies(String query) {
        return movieApiService.searchMovies(query, DEFAULT_LANGUAGE, 1, false)
            .map(response -> response.getResults())
            .doOnSuccess(movies -> {
                if (movies != null && !movies.isEmpty()) {
                    // Save search results to database
                    saveMovies(movies, false);
                }
            })
            .onErrorResumeNext(throwable -> {
                // On network error, try to search in the local database (partial match)
                return movieDao.getAllMovies()
                    .firstOrError()
                    .map(entities -> {
                        List<Movie> allMovies = movieMapper.toDomainList(entities);
                        List<Movie> filteredMovies = new ArrayList<>();
                        String lowercaseQuery = query.toLowerCase();
                        for (Movie movie : allMovies) {
                            if (movie.getTitle().toLowerCase().contains(lowercaseQuery)) {
                                filteredMovies.add(movie);
                            }
                        }
                        return filteredMovies;
                    })
                    .onErrorReturn(error -> new ArrayList<>());
            });
    }

    public Flowable<List<Movie>> getBookmarkedMovies() {
        return movieDao.getBookmarkedMovies()
            .map(entities -> movieMapper.toDomainList(entities));
    }

    public Completable bookmarkMovie(Movie movie) {
        movie.setBookmarked(true);
        return Completable.fromAction(() -> 
            movieDao.insertMovie(movieMapper.toEntity(movie))
        );
    }

    public Completable unbookmarkMovie(Movie movie) {
        movie.setBookmarked(false);
        return Completable.fromAction(() ->
            movieDao.insertMovie(movieMapper.toEntity(movie))
        );
    }

    public Single<Movie> getMovieById(int movieId) {
        return movieDao.getMovieById(movieId)
            .map(entity -> movieMapper.fromEntity(entity))
            .onErrorResumeNext(throwable ->
                movieApiService.getMovieDetails(movieId, DEFAULT_LANGUAGE)
                    .doOnSuccess(movie -> {
                        // Save the movie to the database
                        Completable.fromAction(() -> 
                            movieDao.insertMovie(movieMapper.toEntity(movie))
                        )
                        .subscribeOn(Schedulers.io())
                        .subscribe(() -> {}, error -> {});
                    })
            );
    }

    public Completable deleteNonBookmarkedMovies() {
        return movieDao.deleteNonBookmarkedMovies()
                .subscribeOn(Schedulers.io());
    }

    private void saveMovies(List<Movie> movies, boolean isTrending) {
        Log.d(TAG, "Saving " + (movies != null ? movies.size() : 0) + " movies to database, isTrending=" + isTrending);
        Completable.fromAction(() -> {
            List<MovieEntity> entities = movieMapper.toEntityList(movies);
            for (MovieEntity entity : entities) {
                entity.setBookmarked(false);
                entity.setTrending(isTrending);
            }
            movieDao.insertMovies(entities);
            Log.d(TAG, "Successfully saved movies to database");
        })
        .subscribeOn(Schedulers.io())
        .subscribe(() -> {}, throwable -> {
            Log.e(TAG, "Error saving movies to database: " + throwable.getMessage());
        });
    }
    
    // Force fetch from network, bypassing local database
    public Single<List<Movie>> getTrendingMovies(boolean forceRefresh) {
        if (forceRefresh) {
            return movieApiService.getTrendingMovies(DEFAULT_LANGUAGE, 1)
                .map(response -> response.getResults())
                .doOnSuccess(movies -> saveMovies(movies, true));
        } else {
            return getTrendingMovies();
        }
    }
    
    // Force fetch from network, bypassing local database
    public Single<List<Movie>> getNowPlayingMovies(boolean forceRefresh) {
        if (forceRefresh) {
            return movieApiService.getNowPlayingMovies(DEFAULT_LANGUAGE, 1)
                .map(response -> response.getResults())
                .doOnSuccess(movies -> saveMovies(movies, false));
        } else {
            return getNowPlayingMovies();
        }
    }
} 