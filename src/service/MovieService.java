package service;

import model.Movie;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MovieService {
    private final Map<String, Movie> movieMap = new ConcurrentHashMap<>();

    public void addMovie(Movie movie) {
        movieMap.put(movie.getId(), movie);
    }

    public Movie getMovie(String movieId) {
        return movieMap.get(movieId);
    }
}
