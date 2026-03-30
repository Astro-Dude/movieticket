package service;

import enums.City;
import exception.ShowConflictException;
import model.Movie;
import model.Screen;
import model.Show;
import model.Theatre;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Manages shows with concurrency control for admin operations.
 *
 * Concurrency for show addition:
 * - A ReentrantLock per screen ensures two admins can't add overlapping shows
 *   on the same screen simultaneously.
 * - Different screens can have shows added in parallel (no contention).
 */
public class ShowService {
    private final Map<String, Show> showMap = new ConcurrentHashMap<>();
    // Lock per screen to prevent conflicting show additions on the same screen
    private final Map<String, ReentrantLock> screenLocks = new ConcurrentHashMap<>();
    private final TheatreService theatreService;

    public ShowService(TheatreService theatreService) {
        this.theatreService = theatreService;
    }

    /**
     * Add a show with conflict detection.
     * Two shows on the same screen cannot overlap in time.
     */
    public Show addShow(String showId, Movie movie, Screen screen, Theatre theatre, LocalDateTime startTime) {
        ReentrantLock lock = screenLocks.computeIfAbsent(screen.getId(), k -> new ReentrantLock());
        lock.lock();
        try {
            // Check for time conflicts on the same screen
            LocalDateTime endTime = startTime.plusMinutes(movie.getDurationMinutes());
            for (Show existing : showMap.values()) {
                if (!existing.getScreen().getId().equals(screen.getId())) continue;
                LocalDateTime existingStart = existing.getStartTime();
                LocalDateTime existingEnd = existingStart.plusMinutes(existing.getMovie().getDurationMinutes());
                // Overlap check: new show starts before existing ends AND new show ends after existing starts
                if (startTime.isBefore(existingEnd) && endTime.isAfter(existingStart)) {
                    throw new ShowConflictException(
                            "Time conflict with existing show: " + existing
                                    + ". Requested: " + startTime + " - " + endTime);
                }
            }
            Show show = new Show(showId, movie, screen, theatre, startTime);
            showMap.put(showId, show);
            return show;
        } finally {
            lock.unlock();
        }
    }

    public Show getShow(String showId) {
        return showMap.get(showId);
    }

    /** Get all shows for a movie in a given city. */
    public List<Show> getShowsByMovieAndCity(String movieId, City city) {
        return showMap.values().stream()
                .filter(s -> s.getMovie().getId().equals(movieId)
                        && s.getTheatre().getCity() == city)
                .collect(Collectors.toList());
    }

    /** Get all shows in a specific theatre. */
    public List<Show> getShowsByTheatre(String theatreId) {
        return showMap.values().stream()
                .filter(s -> s.getTheatre().getId().equals(theatreId))
                .collect(Collectors.toList());
    }

    /** Get all unique movies playing in a given city. */
    public List<Movie> getMoviesByCity(City city) {
        return showMap.values().stream()
                .filter(s -> s.getTheatre().getCity() == city)
                .map(Show::getMovie)
                .distinct()
                .collect(Collectors.toList());
    }
}
