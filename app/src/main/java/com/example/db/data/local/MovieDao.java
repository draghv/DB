package com.example.db.data.local;

import androidx.room.*;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import java.util.List;

@Dao
public interface MovieDao {
    @Query("SELECT * FROM movies")
    Flowable<List<MovieEntity>> getAllMovies();

    @Query("SELECT * FROM movies WHERE isBookmarked = 1")
    Flowable<List<MovieEntity>> getBookmarkedMovies();

    @Query("SELECT * FROM movies WHERE isTrending = 1")
    Single<List<MovieEntity>> getTrendingMovies();

    @Query("SELECT * FROM movies WHERE isTrending = 0 AND isBookmarked = 0")
    Single<List<MovieEntity>> getNowPlayingMovies();

    @Query("SELECT * FROM movies WHERE id = :movieId")
    Single<MovieEntity> getMovieById(int movieId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMovie(MovieEntity movie);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMovies(List<MovieEntity> movies);

    @Delete
    Completable deleteMovie(MovieEntity movie);

    @Query("UPDATE movies SET isBookmarked = :isBookmarked WHERE id = :movieId")
    Completable updateBookmarkStatus(int movieId, boolean isBookmarked);

    @Query("DELETE FROM movies WHERE isBookmarked = 0")
    Completable deleteNonBookmarkedMovies();
} 