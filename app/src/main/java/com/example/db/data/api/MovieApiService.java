package com.example.db.data.api;

import com.example.db.data.model.Movie;
import com.example.db.data.model.MovieResponse;
import io.reactivex.rxjava3.core.Single;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MovieApiService {
    @GET("trending/movie/day")
    Single<MovieResponse> getTrendingMovies(
        @Query("language") String language,
        @Query("page") int page
    );

    @GET("movie/now_playing")
    Single<MovieResponse> getNowPlayingMovies(
        @Query("language") String language,
        @Query("page") int page
    );

    @GET("search/movie")
    Single<MovieResponse> searchMovies(
        @Query("query") String query,
        @Query("language") String language,
        @Query("page") int page,
        @Query("include_adult") boolean includeAdult
    );

    @GET("movie/{movie_id}")
    Single<Movie> getMovieDetails(
        @Path("movie_id") int movieId,
        @Query("language") String language
    );
} 