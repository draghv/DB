package com.example.db.di;

import android.content.Context;
import androidx.room.Room;
import com.example.db.data.local.MovieDatabase;
import com.example.db.data.local.MovieDao;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Singleton;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    @Provides
    @Singleton
    MovieDatabase provideDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(
            context,
            MovieDatabase.class,
            "movie_database"
        ).fallbackToDestructiveMigration()
        .build();
    }

    @Provides
    @Singleton
    MovieDao provideMovieDao(MovieDatabase database) {
        return database.movieDao();
    }
} 