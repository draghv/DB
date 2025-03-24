package com.example.db.ui.details;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.bumptech.glide.Glide;
import com.example.db.R;
import com.example.db.databinding.FragmentMovieDetailsBinding;
import com.example.db.data.model.Movie;
import com.google.android.material.snackbar.Snackbar;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MovieDetailsFragment extends Fragment {
    private FragmentMovieDetailsBinding binding;
    private MovieDetailsViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMovieDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MovieDetailsViewModel.class);
        
        int movieId = MovieDetailsFragmentArgs.fromBundle(getArguments()).getMovieId();
        viewModel.loadMovieDetails(movieId);
        
        setupObservers();
        setupToolbar();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigateUp();
        });
    }

    private void setupObservers() {
        viewModel.getMovie().observe(getViewLifecycleOwner(), this::updateUI);
        
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.content.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void updateUI(Movie movie) {
        if (movie == null) return;

        binding.toolbar.setTitle(movie.getTitle());
        
        Glide.with(this)
            .load(movie.getFullPosterPath())
            .placeholder(R.drawable.placeholder_poster)
            .error(R.drawable.placeholder_poster)
            .into(binding.posterImage);
            
        binding.titleText.setText(movie.getTitle());
        binding.overviewText.setText(movie.getOverview());
        binding.ratingText.setText(String.format("%.1f", movie.getVoteAverage()));
        binding.releaseDateText.setText(movie.getReleaseDate());
        
        binding.bookmarkButton.setSelected(movie.isBookmarked());
        binding.bookmarkButton.setOnClickListener(v -> {
            if (movie.isBookmarked()) {
                viewModel.unbookmarkMovie(movie);
            } else {
                viewModel.bookmarkMovie(movie);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 