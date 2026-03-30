package controller;

import enums.City;
import model.*;
import service.*;

import java.util.List;

/**
 * API facade — the entry point for all user-facing operations.
 */
public class MovieTicketController {
    private final TheatreService theatreService;
    private final ShowService showService;
    private final BookingService bookingService;

    public MovieTicketController(TheatreService theatreService,
                                  ShowService showService,
                                  BookingService bookingService) {
        this.theatreService = theatreService;
        this.showService = showService;
        this.bookingService = bookingService;
    }

    /** API: showTheatres(userCity) → list of theatres in the city */
    public List<Theatre> showTheatres(City city) {
        return theatreService.getTheatresByCity(city);
    }

    /** API: showMovies(city) → list of movies playing in theatres in the city */
    public List<Movie> showMovies(City city) {
        return showService.getMoviesByCity(city);
    }

    /** Get shows for a movie in a city (user chose movie → see theatres/shows) */
    public List<Show> getShowsForMovie(String movieId, City city) {
        return showService.getShowsByMovieAndCity(movieId, city);
    }

    /** Get shows in a theatre (user chose theatre → see movies/shows) */
    public List<Show> getShowsInTheatre(String theatreId) {
        return showService.getShowsByTheatre(theatreId);
    }

    /** API: bookTickets(showId, seats) → Booking (movie ticket) */
    public Booking bookTickets(String showId, List<String> seatIds) {
        Show show = showService.getShow(showId);
        if (show == null) {
            throw new IllegalArgumentException("Show not found: " + showId);
        }
        return bookingService.bookTickets(show, seatIds);
    }

    /** API: cancelBooking → cancellation + refund */
    public Booking cancelBooking(String bookingId) {
        return bookingService.cancelBooking(bookingId);
    }
}
