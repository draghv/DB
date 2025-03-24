package com.example.db.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.db.R;
import com.example.db.databinding.FragmentHomeBinding;
import com.example.db.ui.adapter.MovieAdapter;
import com.example.db.data.model.Movie;
import com.google.android.material.snackbar.Snackbar;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeFragment extends Fragment implements MovieAdapter.MovieClickListener {
    private static final String TAG = "HomeFragment";
    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private MovieAdapter trendingAdapter;
    private MovieAdapter nowPlayingAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "HomeFragment created");
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        setupRecyclerViews();
        setupSwipeRefresh();
        setupObservers();
        setupFab();
        
        // Force a refresh on creation
        viewModel.refreshMovies();
    }

    private void setupRecyclerViews() {
        trendingAdapter = new MovieAdapter(this);
        nowPlayingAdapter = new MovieAdapter(this);

        binding.trendingMoviesRecyclerView.setLayoutManager(
            new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        binding.trendingMoviesRecyclerView.setAdapter(trendingAdapter);

        binding.nowPlayingRecyclerView.setLayoutManager(
            new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        binding.nowPlayingRecyclerView.setAdapter(nowPlayingAdapter);
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener(() -> {
            viewModel.refreshMovies();
        });
    }

    private void setupObservers() {
        viewModel.getTrendingMovies().observe(getViewLifecycleOwner(), movies -> {
            Log.d(TAG, "Trending movies updated: " + (movies != null ? movies.size() : 0) + " items");
            trendingAdapter.submitList(movies);
            if (movies != null && movies.isEmpty()) {
                Toast.makeText(requireContext(), "No trending movies found", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getNowPlayingMovies().observe(getViewLifecycleOwner(), movies -> {
            Log.d(TAG, "Now playing movies updated: " + (movies != null ? movies.size() : 0) + " items");
            nowPlayingAdapter.submitList(movies);
            if (movies != null && movies.isEmpty()) {
                Toast.makeText(requireContext(), "No now playing movies found", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.swipeRefresh.setRefreshing(isLoading);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Log.e(TAG, "Error loading data: " + error);
                Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_LONG).show();
                Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupFab() {
        binding.fabRefresh.setOnClickListener(v -> {
            viewModel.refreshMovies();
        });
    }

    @Override
    public void onMovieClick(Movie movie) {
        HomeFragmentDirections.ActionHomeToDetails action =
            HomeFragmentDirections.actionHomeToDetails(movie.getId());
        Navigation.findNavController(requireView()).navigate(action);
    }

    @Override
    public void onBookmarkClick(Movie movie) {
        viewModel.bookmarkMovie(movie);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 