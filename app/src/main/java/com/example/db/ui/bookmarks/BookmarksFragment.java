package com.example.db.ui.bookmarks;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import com.example.db.R;
import com.example.db.databinding.FragmentBookmarksBinding;
import com.example.db.ui.adapter.MovieAdapter;
import com.example.db.data.model.Movie;
import com.google.android.material.snackbar.Snackbar;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class BookmarksFragment extends Fragment implements MovieAdapter.MovieClickListener {
    private FragmentBookmarksBinding binding;
    private BookmarksViewModel viewModel;
    private MovieAdapter movieAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBookmarksBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(BookmarksViewModel.class);
        setupRecyclerView();
        setupObservers();
    }

    private void setupRecyclerView() {
        movieAdapter = new MovieAdapter(this);
        binding.bookmarksRecyclerView.setLayoutManager(
            new GridLayoutManager(requireContext(), 2)
        );
        binding.bookmarksRecyclerView.setAdapter(movieAdapter);
    }

    private void setupObservers() {
        viewModel.getBookmarkedMovies().observe(getViewLifecycleOwner(), movies -> {
            movieAdapter.submitList(movies);
            binding.emptyStateText.setVisibility(
                movies != null && !movies.isEmpty() ? View.GONE : View.VISIBLE
            );
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onMovieClick(Movie movie) {
        BookmarksFragmentDirections.ActionBookmarksFragmentToMovieDetailsFragment action =
            BookmarksFragmentDirections.actionBookmarksFragmentToMovieDetailsFragment(movie.getId());
        Navigation.findNavController(requireView()).navigate(action);
    }

    @Override
    public void onBookmarkClick(Movie movie) {
        viewModel.unbookmarkMovie(movie);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 