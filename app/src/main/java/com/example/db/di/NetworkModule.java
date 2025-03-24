package com.example.db.di;

import android.util.Log;
import com.example.db.data.api.MovieApiService;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Singleton;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {
    private static final String TAG = "NetworkModule";
    private static final String BASE_URL = "https://api.themoviedb.org/3/";
    private static final String API_KEY = "933f90d71eb2796fd6cb719ed51db997";

    @Provides
    @Singleton
    public String provideApiKey() {
        return API_KEY;
    }

    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient(String apiKey) {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Create an interceptor to add API key to all requests
        Interceptor apiKeyInterceptor = chain -> {
            Request original = chain.request();
            HttpUrl originalHttpUrl = original.url();

            HttpUrl url = originalHttpUrl.newBuilder()
                .addQueryParameter("api_key", apiKey)
                .build();

            Request.Builder requestBuilder = original.newBuilder()
                .url(url);

            Request request = requestBuilder.build();
            Log.d(TAG, "Adding API key to request: " + request.url());
            return chain.proceed(request);
        };

        return new OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(apiKeyInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
    }

    @Provides
    @Singleton
    public Retrofit provideRetrofit(OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build();
    }

    @Provides
    @Singleton
    public MovieApiService provideMovieApiService(Retrofit retrofit) {
        return retrofit.create(MovieApiService.class);
    }
} 