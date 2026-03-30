import controller.MovieTicketController;
import enums.City;
import enums.SeatType;
import exception.SeatAlreadyBookedException;
import model.*;
import service.*;
import strategy.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * End-to-end demo exercising both user flows, concurrency, and cancellation.
 */
public class MovieTicketBookingDemo {

    public static void main(String[] args) {
        // ---- Setup services ----
        TheatreService theatreService = new TheatreService();
        MovieService movieService = new MovieService();
        PricingService pricingService = new PricingService();
        ShowService showService = new ShowService(theatreService);
        BookingService bookingService = new BookingService(pricingService);
        MovieTicketController controller = new MovieTicketController(theatreService, showService, bookingService);

        // Register pricing rules (Strategy pattern)
        pricingService.addRule(new PeakHourPricingRule());
        pricingService.addRule(new WeekendPricingRule());
        pricingService.addRule(new HighDemandPricingRule());

        // ---- Seed data ----
        Movie movie1 = new Movie("M1", "Inception", 148);
        Movie movie2 = new Movie("M2", "Interstellar", 169);
        movieService.addMovie(movie1);
        movieService.addMovie(movie2);

        // Create seats for Screen 1
        List<Seat> screen1Seats = Arrays.asList(
                new Seat("S1-1", 1, 1, SeatType.SILVER),
                new Seat("S1-2", 1, 2, SeatType.SILVER),
                new Seat("S1-3", 2, 1, SeatType.GOLD),
                new Seat("S1-4", 2, 2, SeatType.GOLD),
                new Seat("S1-5", 3, 1, SeatType.PLATINUM),
                new Seat("S1-6", 3, 2, SeatType.PLATINUM)
        );
        Screen screen1 = new Screen("SCR1", "Screen 1", screen1Seats);

        List<Seat> screen2Seats = Arrays.asList(
                new Seat("S2-1", 1, 1, SeatType.SILVER),
                new Seat("S2-2", 1, 2, SeatType.GOLD),
                new Seat("S2-3", 2, 1, SeatType.PLATINUM)
        );
        Screen screen2 = new Screen("SCR2", "Screen 2", screen2Seats);

        Theatre theatre1 = new Theatre("T1", "PVR Koramangala", City.BANGALORE,
                Arrays.asList(screen1, screen2));
        Theatre theatre2 = new Theatre("T2", "INOX Forum Mall", City.BANGALORE,
                Arrays.asList(screen1));
        theatreService.addTheatre(theatre1);
        theatreService.addTheatre(theatre2);

        // Set base prices per theatre + seat type
        pricingService.setBasePrice("T1", SeatType.SILVER, 150);
        pricingService.setBasePrice("T1", SeatType.GOLD, 250);
        pricingService.setBasePrice("T1", SeatType.PLATINUM, 400);
        pricingService.setBasePrice("T2", SeatType.SILVER, 200);
        pricingService.setBasePrice("T2", SeatType.GOLD, 300);
        pricingService.setBasePrice("T2", SeatType.PLATINUM, 500);

        // Add shows
        Show show1 = showService.addShow("SH1", movie1, screen1, theatre1,
                LocalDateTime.of(2026, 4, 5, 19, 0)); // peak hour + weekend (Saturday)
        Show show2 = showService.addShow("SH2", movie2, screen2, theatre1,
                LocalDateTime.of(2026, 4, 5, 14, 0)); // weekend matinee
        Show show3 = showService.addShow("SH3", movie1, screen1, theatre2,
                LocalDateTime.of(2026, 4, 6, 11, 0)); // Sunday morning

        // ==============================
        // FLOW 1: User searches by city → movies
        // ==============================
        System.out.println("=== Flow 1: Search by City → Movies ===");
        List<Movie> moviesInBangalore = controller.showMovies(City.BANGALORE);
        System.out.println("Movies in Bangalore: " + moviesInBangalore);

        // User picks Inception → see shows
        List<Show> inceptionShows = controller.getShowsForMovie("M1", City.BANGALORE);
        System.out.println("Shows for Inception in Bangalore: " + inceptionShows);

        // ==============================
        // FLOW 2: User searches by city → theatres
        // ==============================
        System.out.println("\n=== Flow 2: Search by City → Theatres ===");
        List<Theatre> theatresInBangalore = controller.showTheatres(City.BANGALORE);
        System.out.println("Theatres in Bangalore: " + theatresInBangalore);

        // User picks PVR Koramangala → see shows
        List<Show> pvrShows = controller.getShowsInTheatre("T1");
        System.out.println("Shows at PVR Koramangala: " + pvrShows);

        // ==============================
        // BOOKING
        // ==============================
        System.out.println("\n=== Booking ===");
        // Book 2 Gold seats for Show 1 (peak hour + weekend → surcharges apply)
        Booking booking1 = controller.bookTickets("SH1", Arrays.asList("S1-3", "S1-4"));
        System.out.println("Booking 1: " + booking1);
        System.out.println("Payment: " + booking1.getPayment());

        // ==============================
        // CONCURRENCY: Two users try to book the same seat
        // ==============================
        System.out.println("\n=== Concurrency Test: Double Booking ===");
        // First user books seat S1-5
        Booking booking2 = controller.bookTickets("SH1", Arrays.asList("S1-5"));
        System.out.println("User A booked: " + booking2);

        // Second user tries the same seat → should fail
        try {
            controller.bookTickets("SH1", Arrays.asList("S1-5"));
            System.out.println("ERROR: Should not reach here!");
        } catch (SeatAlreadyBookedException e) {
            System.out.println("User B correctly rejected: " + e.getMessage());
        }

        // ==============================
        // CANCELLATION + REFUND
        // ==============================
        System.out.println("\n=== Cancellation + Refund ===");
        System.out.println("Before cancel — booking2 status: " + booking2.getStatus()
                + ", payment: " + booking2.getPayment().getStatus());
        Booking cancelled = controller.cancelBooking(booking2.getId());
        System.out.println("After cancel  — status: " + cancelled.getStatus()
                + ", payment: " + cancelled.getPayment().getStatus());

        // Seat S1-5 is now available again — another user can book it
        Booking booking3 = controller.bookTickets("SH1", Arrays.asList("S1-5"));
        System.out.println("User C re-booked freed seat: " + booking3);

        // ==============================
        // CONCURRENCY: Admin show conflict
        // ==============================
        System.out.println("\n=== Admin: Show Conflict Detection ===");
        try {
            // Try adding an overlapping show on the same screen
            showService.addShow("SH-CONFLICT", movie2, screen1, theatre1,
                    LocalDateTime.of(2026, 4, 5, 19, 30)); // overlaps with SH1 (19:00-21:28)
        } catch (Exception e) {
            System.out.println("Conflict detected: " + e.getMessage());
        }

        System.out.println("\n=== Done ===");
    }
}
