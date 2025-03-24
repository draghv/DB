package com.example.db.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.db.R;
import com.example.db.data.model.Movie;
import com.example.db.databinding.ItemMovieBinding;

public class MovieAdapter extends ListAdapter<Movie, MovieAdapter.MovieViewHolder> {
    private final MovieClickListener listener;

    public MovieAdapter(MovieClickListener listener) {
        super(new MovieDiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMovieBinding binding = ItemMovieBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false
        );
        return new MovieViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class MovieViewHolder extends RecyclerView.ViewHolder {
        private final ItemMovieBinding binding;

        MovieViewHolder(ItemMovieBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Movie movie) {
            binding.titleText.setText(movie.getTitle());
            binding.overviewText.setText(movie.getOverview());
            binding.ratingText.setText(
                String.format("%.1f", movie.getVoteAverage())
            );

            Glide.with(itemView)
                .load(movie.getFullPosterPath())
                .placeholder(R.drawable.placeholder_poster)
                .error(R.drawable.placeholder_poster)
                .into(binding.posterImage);

            binding.bookmarkButton.setSelected(movie.isBookmarked());

            itemView.setOnClickListener(v -> listener.onMovieClick(movie));
            binding.bookmarkButton.setOnClickListener(v -> listener.onBookmarkClick(movie));
        }
    }

    public interface MovieClickListener {
        void onMovieClick(Movie movie);
        void onBookmarkClick(Movie movie);
    }

    private static class MovieDiffCallback extends DiffUtil.ItemCallback<Movie> {
        @Override
        public boolean areItemsTheSame(@NonNull Movie oldItem, @NonNull Movie newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Movie oldItem, @NonNull Movie newItem) {
            return oldItem.equals(newItem);
        }
    }
} 