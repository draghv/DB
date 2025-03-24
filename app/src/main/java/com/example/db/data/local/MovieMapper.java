package com.example.db.data.local;

import com.example.db.data.model.Movie;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MovieMapper {
    @Inject
    public MovieMapper() {}

    public Movie fromEntity(MovieEntity entity) {
        Movie movie = new Movie(
            entity.getId(),
            entity.getTitle(),
            entity.getOverview(),
            entity.getPosterPath(),
            entity.getBackdropPath(),
            entity.getReleaseDate(),
            entity.getVoteAverage()
        );
        movie.setBookmarked(entity.isBookmarked());
        return movie;
    }

    public MovieEntity toEntity(Movie movie) {
        return new MovieEntity(
            movie.getId(),
            movie.getTitle(),
            movie.getOverview(),
            movie.getPosterPath(),
            movie.getBackdropPath(),
            movie.getReleaseDate(),
            movie.getVoteAverage(),
            movie.isBookmarked()
        );
    }

    public List<Movie> toDomainList(List<MovieEntity> entities) {
        List<Movie> movies = new ArrayList<>();
        for (MovieEntity entity : entities) {
            movies.add(fromEntity(entity));
        }
        return movies;
    }

    public List<MovieEntity> toEntityList(List<Movie> movies) {
        List<MovieEntity> entities = new ArrayList<>();
        for (Movie movie : movies) {
            entities.add(toEntity(movie));
        }
        return entities;
    }
} 