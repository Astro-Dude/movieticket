package model;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A Show represents a specific screening of a movie on a screen at a given time.
 * It tracks which seats are booked via a concurrent set for thread-safe booking.
 */
public class Show {
    private final String id;
    private final Movie movie;
    private final Screen screen;
    private final Theatre theatre;
    private final LocalDateTime startTime;
    // Thread-safe set of booked seat IDs
    private final Set<String> bookedSeatIds;

    public Show(String id, Movie movie, Screen screen, Theatre theatre, LocalDateTime startTime) {
        this.id = id;
        this.movie = movie;
        this.screen = screen;
        this.theatre = theatre;
        this.startTime = startTime;
        this.bookedSeatIds = ConcurrentHashMap.newKeySet();
    }

    public String getId() { return id; }
    public Movie getMovie() { return movie; }
    public Screen getScreen() { return screen; }
    public Theatre getTheatre() { return theatre; }
    public LocalDateTime getStartTime() { return startTime; }
    public Set<String> getBookedSeatIds() { return bookedSeatIds; }

    @Override
    public String toString() {
        return "Show{" + movie.getName() + " @ " + theatre.getName() + " " + screen.getName()
                + " at " + startTime + "}";
    }
}
