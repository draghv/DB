package com.example.db;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.example.db.databinding.ActivityMainBinding;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
            .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
        }
        
        // Handle intent (for deep linking)
        handleIntent(getIntent());
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }
    
    private void handleIntent(Intent intent) {
        if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            if (uri != null) {
                String path = uri.getPath();
                if (path != null && path.startsWith("/movie/")) {
                    try {
                        // Extract movie ID from URI
                        String movieIdStr = path.replace("/movie/", "");
                        int movieId = Integer.parseInt(movieIdStr);
                        
                        // Create navigation bundle and options
                        NavOptions navOptions = new NavOptions.Builder()
                            .setPopUpTo(R.id.nav_graph, false)
                            .build();
                        
                        // Navigate to the movie details fragment with the ID
                        Bundle args = new Bundle();
                        args.putInt("movieId", movieId);
                        navController.navigate(R.id.navigation_movie_details, args, navOptions);
                    } catch (NumberFormatException e) {
                        // Invalid movie ID format
                    }
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
} 