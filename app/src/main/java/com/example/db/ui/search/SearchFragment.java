package com.example.db.ui.search;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import com.example.db.R;
import com.example.db.databinding.FragmentSearchBinding;
import com.example.db.ui.adapter.MovieAdapter;
import com.example.db.data.model.Movie;
import com.google.android.material.snackbar.Snackbar;
import dagger.hilt.android.AndroidEntryPoint;
import java.util.concurrent.TimeUnit;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.subjects.PublishSubject;

@AndroidEntryPoint
public class SearchFragment extends Fragment implements MovieAdapter.MovieClickListener {
    private FragmentSearchBinding binding;
    private SearchViewModel viewModel;
    private MovieAdapter movieAdapter;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private final PublishSubject<String> searchSubject = PublishSubject.create();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        setupRecyclerView();
        setupSearchView();
        setupObservers();
    }

    private void setupRecyclerView() {
        movieAdapter = new MovieAdapter(this);
        binding.searchRecyclerView.setLayoutManager(
            new GridLayoutManager(requireContext(), 2)
        );
        binding.searchRecyclerView.setAdapter(movieAdapter);
    }

    private void setupSearchView() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchSubject.onNext(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchSubject.onNext(newText);
                return true;
            }
        });

        disposables.add(searchSubject
            .debounce(300, TimeUnit.MILLISECONDS)
            .filter(query -> query.length() >= 2)
            .distinctUntilChanged()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(query -> viewModel.searchMovies(query)));
    }

    private void setupObservers() {
        viewModel.getSearchResults().observe(getViewLifecycleOwner(), movies -> {
            movieAdapter.submitList(movies);
            binding.emptyState.setVisibility(
                movies != null && !movies.isEmpty() ? View.GONE : View.VISIBLE
            );
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onMovieClick(Movie movie) {
        SearchFragmentDirections.ActionSearchFragmentToMovieDetailsFragment action =
            SearchFragmentDirections.actionSearchFragmentToMovieDetailsFragment(movie.getId());
        Navigation.findNavController(requireView()).navigate(action);
    }

    @Override
    public void onBookmarkClick(Movie movie) {
        viewModel.bookmarkMovie(movie);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposables.clear();
        binding = null;
    }
} 