import controller.MovieTicketController;
import enums.BookingStatus;
import enums.City;
import enums.PaymentStatus;
import enums.SeatType;
import exception.SeatAlreadyBookedException;
import exception.ShowConflictException;
import model.*;
import service.*;
import strategy.*;

import java.time.LocalDateTime;
import java.util.Arrays;

import java.util.List;

/**
 * Comprehensive endpoint verification — tests every API including edge cases.
 */
public class EndpointVerification {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        TheatreService theatreService = new TheatreService();
        MovieService movieService = new MovieService();
        PricingService pricingService = new PricingService();
        ShowService showService = new ShowService(theatreService);
        BookingService bookingService = new BookingService(pricingService);
        MovieTicketController controller = new MovieTicketController(theatreService, showService, bookingService);

        pricingService.addRule(new PeakHourPricingRule());
        pricingService.addRule(new WeekendPricingRule());
        pricingService.addRule(new HighDemandPricingRule());

        // Seed data
        Movie inception = new Movie("M1", "Inception", 148);
        Movie interstellar = new Movie("M2", "Interstellar", 169);
        Movie tenet = new Movie("M3", "Tenet", 150);
        movieService.addMovie(inception);
        movieService.addMovie(interstellar);
        movieService.addMovie(tenet);

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

        List<Seat> screen3Seats = Arrays.asList(
                new Seat("S3-1", 1, 1, SeatType.SILVER),
                new Seat("S3-2", 1, 2, SeatType.GOLD)
        );
        Screen screen3 = new Screen("SCR3", "Screen 3", screen3Seats);

        // Separate screen for Delhi theatre (screens are physical — not shared across theatres)
        List<Seat> screen4Seats = Arrays.asList(
                new Seat("S4-1", 1, 1, SeatType.SILVER),
                new Seat("S4-2", 1, 2, SeatType.GOLD),
                new Seat("S4-3", 2, 1, SeatType.PLATINUM)
        );
        Screen screen4 = new Screen("SCR4", "Screen 4", screen4Seats);

        Theatre pvr = new Theatre("T1", "PVR Koramangala", City.BANGALORE, Arrays.asList(screen1, screen2));
        Theatre inox = new Theatre("T2", "INOX Forum Mall", City.BANGALORE, Arrays.asList(screen3));
        Theatre pvrDelhi = new Theatre("T3", "PVR Select City", City.DELHI, Arrays.asList(screen4));
        theatreService.addTheatre(pvr);
        theatreService.addTheatre(inox);
        theatreService.addTheatre(pvrDelhi);

        pricingService.setBasePrice("T1", SeatType.SILVER, 150);
        pricingService.setBasePrice("T1", SeatType.GOLD, 250);
        pricingService.setBasePrice("T1", SeatType.PLATINUM, 400);
        pricingService.setBasePrice("T2", SeatType.SILVER, 200);
        pricingService.setBasePrice("T2", SeatType.GOLD, 300);
        pricingService.setBasePrice("T2", SeatType.PLATINUM, 500);
        pricingService.setBasePrice("T3", SeatType.SILVER, 180);
        pricingService.setBasePrice("T3", SeatType.GOLD, 280);
        pricingService.setBasePrice("T3", SeatType.PLATINUM, 450);

        // Shows: various times for pricing verification
        Show sh1 = showService.addShow("SH1", inception, screen1, pvr,
                LocalDateTime.of(2026, 4, 5, 19, 0));  // Sat 7PM (peak + weekend)
        Show sh2 = showService.addShow("SH2", interstellar, screen2, pvr,
                LocalDateTime.of(2026, 4, 5, 14, 0));  // Sat 2PM (weekend only)
        Show sh3 = showService.addShow("SH3", inception, screen3, inox,
                LocalDateTime.of(2026, 4, 7, 10, 0));  // Mon 10AM (no surcharge)
        Show sh4 = showService.addShow("SH4", tenet, screen4, pvrDelhi,
                LocalDateTime.of(2026, 4, 5, 20, 0));  // Sat 8PM (peak + weekend)

        // =====================================================
        // 1. showTheatres(city)
        // =====================================================
        System.out.println("=== 1. showTheatres(city) ===");

        // 1a. City with multiple theatres
        List<Theatre> blrTheatres = controller.showTheatres(City.BANGALORE);
        check("showTheatres(BANGALORE) returns 2 theatres", blrTheatres.size() == 2);

        // 1b. City with one theatre
        List<Theatre> delTheatres = controller.showTheatres(City.DELHI);
        check("showTheatres(DELHI) returns 1 theatre", delTheatres.size() == 1);
        check("showTheatres(DELHI) returns PVR Select City",
                delTheatres.get(0).getName().equals("PVR Select City"));

        // 1c. City with no theatres
        List<Theatre> mumTheatres = controller.showTheatres(City.MUMBAI);
        check("showTheatres(MUMBAI) returns empty list", mumTheatres.isEmpty());

        // =====================================================
        // 2. showMovies(city)
        // =====================================================
        System.out.println("\n=== 2. showMovies(city) ===");

        // 2a. City with multiple movies
        List<Movie> blrMovies = controller.showMovies(City.BANGALORE);
        check("showMovies(BANGALORE) returns 2 movies", blrMovies.size() == 2);

        // 2b. City with one movie
        List<Movie> delMovies = controller.showMovies(City.DELHI);
        check("showMovies(DELHI) returns 1 movie", delMovies.size() == 1);
        check("showMovies(DELHI) returns Tenet", delMovies.get(0).getName().equals("Tenet"));

        // 2c. City with no shows
        List<Movie> mumMovies = controller.showMovies(City.MUMBAI);
        check("showMovies(MUMBAI) returns empty list", mumMovies.isEmpty());

        // =====================================================
        // 3. getShowsForMovie(movieId, city)
        // =====================================================
        System.out.println("\n=== 3. getShowsForMovie(movieId, city) ===");

        // 3a. Movie in multiple theatres in same city
        List<Show> inceptionBlr = controller.getShowsForMovie("M1", City.BANGALORE);
        check("Inception in BANGALORE has 2 shows", inceptionBlr.size() == 2);

        // 3b. Movie in one theatre
        List<Show> tenetDelhi = controller.getShowsForMovie("M3", City.DELHI);
        check("Tenet in DELHI has 1 show", tenetDelhi.size() == 1);

        // 3c. Movie not in city
        List<Show> tenetBlr = controller.getShowsForMovie("M3", City.BANGALORE);
        check("Tenet in BANGALORE has 0 shows", tenetBlr.isEmpty());

        // 3d. Non-existent movie
        List<Show> noMovie = controller.getShowsForMovie("M999", City.BANGALORE);
        check("Non-existent movie returns empty", noMovie.isEmpty());

        // =====================================================
        // 4. getShowsInTheatre(theatreId)
        // =====================================================
        System.out.println("\n=== 4. getShowsInTheatre(theatreId) ===");

        // 4a. Theatre with multiple shows
        List<Show> pvrShows = controller.getShowsInTheatre("T1");
        check("PVR Koramangala has 2 shows", pvrShows.size() == 2);

        // 4b. Theatre with one show
        List<Show> inoxShows = controller.getShowsInTheatre("T2");
        check("INOX has 1 show", inoxShows.size() == 1);

        // 4c. Non-existent theatre
        List<Show> noTheatre = controller.getShowsInTheatre("T999");
        check("Non-existent theatre returns empty", noTheatre.isEmpty());

        // =====================================================
        // 5. bookTickets(showId, seatIds)
        // =====================================================
        System.out.println("\n=== 5. bookTickets(showId, seatIds) ===");

        // 5a. Basic booking — single seat
        Booking b1 = controller.bookTickets("SH1", Arrays.asList("S1-1"));
        check("Single seat booking is CONFIRMED", b1.getStatus() == BookingStatus.CONFIRMED);
        check("Single seat booking has 1 seat", b1.getSeats().size() == 1);
        check("Payment is SUCCESS", b1.getPayment().getStatus() == PaymentStatus.SUCCESS);

        // 5b. Multi-seat booking
        Booking b2 = controller.bookTickets("SH1", Arrays.asList("S1-3", "S1-4"));
        check("Multi-seat booking has 2 seats", b2.getSeats().size() == 2);
        check("Multi-seat booking is CONFIRMED", b2.getStatus() == BookingStatus.CONFIRMED);

        // 5c. Pricing: Sat 7PM show at T1, SILVER seat = 150 base + 20% peak + 10% weekend = 195
        check("SILVER seat at peak+weekend = 195.0", b1.getTotalPrice() == 195.0);

        // 5d. Pricing: GOLD seats at peak+weekend = 250 + 50 + 25 = 325 each, 650 total
        check("2 GOLD seats at peak+weekend = 650.0", b2.getTotalPrice() == 650.0);

        // 5e. Booking on a weekday non-peak show (no surcharges)
        Booking b3 = controller.bookTickets("SH3", Arrays.asList("S3-1"));
        check("SILVER seat Mon 10AM at T2 = 200.0 (no surcharge)", b3.getTotalPrice() == 200.0);

        // 5f. Double booking same seat → SeatAlreadyBookedException
        try {
            controller.bookTickets("SH1", Arrays.asList("S1-1"));
            check("Double booking should throw", false);
        } catch (SeatAlreadyBookedException e) {
            check("Double booking throws SeatAlreadyBookedException", true);
        }

        // 5g. Partial overlap — one seat booked, one free → all-or-nothing rejection
        try {
            controller.bookTickets("SH1", Arrays.asList("S1-1", "S1-2")); // S1-1 already booked
            check("Partial overlap should throw", false);
        } catch (SeatAlreadyBookedException e) {
            check("Partial overlap throws SeatAlreadyBookedException", true);
            // S1-2 should NOT be booked (all-or-nothing)
            check("S1-2 is still free after partial overlap rejection",
                    !sh1.getBookedSeatIds().contains("S1-2"));
        }

        // 5h. Invalid seat ID
        try {
            controller.bookTickets("SH1", Arrays.asList("INVALID-SEAT"));
            check("Invalid seat should throw", false);
        } catch (IllegalArgumentException e) {
            check("Invalid seat throws IllegalArgumentException", true);
        }

        // 5i. Invalid show ID
        try {
            controller.bookTickets("SH-INVALID", Arrays.asList("S1-1"));
            check("Invalid show should throw", false);
        } catch (IllegalArgumentException e) {
            check("Invalid show throws IllegalArgumentException", true);
        }

        // =====================================================
        // 6. cancelBooking(bookingId)
        // =====================================================
        System.out.println("\n=== 6. cancelBooking(bookingId) ===");

        // 6a. Normal cancellation
        String b1Id = b1.getId();
        check("Before cancel: CONFIRMED", b1.getStatus() == BookingStatus.CONFIRMED);
        Booking cancelled = controller.cancelBooking(b1Id);
        check("After cancel: CANCELLED", cancelled.getStatus() == BookingStatus.CANCELLED);
        check("Payment refunded", cancelled.getPayment().getStatus() == PaymentStatus.REFUNDED);

        // 6b. Seat is freed after cancellation — can rebook
        Booking b4 = controller.bookTickets("SH1", Arrays.asList("S1-1"));
        check("Freed seat S1-1 re-booked successfully", b4.getStatus() == BookingStatus.CONFIRMED);

        // 6c. Cancel already cancelled booking
        try {
            controller.cancelBooking(b1Id);
            check("Re-cancel should throw", false);
        } catch (IllegalStateException e) {
            check("Re-cancel throws IllegalStateException", true);
        }

        // 6d. Cancel non-existent booking
        try {
            controller.cancelBooking("BKG-NONEXISTENT");
            check("Non-existent cancel should throw", false);
        } catch (IllegalArgumentException e) {
            check("Non-existent cancel throws IllegalArgumentException", true);
        }

        // =====================================================
        // 7. Admin: addShow concurrency (show conflicts)
        // =====================================================
        System.out.println("\n=== 7. Admin: Show Addition + Conflict Detection ===");

        // 7a. Overlapping show on same screen → conflict
        try {
            showService.addShow("SH-X", interstellar, screen1, pvr,
                    LocalDateTime.of(2026, 4, 5, 19, 30)); // overlaps SH1 (19:00-21:28)
            check("Overlapping show should throw", false);
        } catch (ShowConflictException e) {
            check("Overlapping show throws ShowConflictException", true);
        }

        // 7b. Non-overlapping show on same screen → OK
        Show sh5 = showService.addShow("SH5", interstellar, screen1, pvr,
                LocalDateTime.of(2026, 4, 5, 22, 0)); // after SH1 ends at 21:28
        check("Non-overlapping show added successfully", sh5 != null);

        // 7c. Same time but different screen → OK (no conflict)
        Show sh6 = showService.addShow("SH6", tenet, screen2, pvr,
                LocalDateTime.of(2026, 4, 5, 19, 0)); // same time as SH1 but on screen2
        check("Same time different screen is allowed", sh6 != null);

        // 7d. Show exactly adjacent (no gap, no overlap) → OK
        // SH5 is Interstellar (169 min) starting 22:00, ends 00:49
        Show sh7 = showService.addShow("SH7", tenet, screen1, pvr,
                LocalDateTime.of(2026, 4, 6, 0, 49)); // starts exactly when SH5 ends
        check("Adjacent show (no overlap) is allowed", sh7 != null);

        // =====================================================
        // 8. Pricing: High Demand surcharge
        // =====================================================
        System.out.println("\n=== 8. Pricing: High Demand Rule ===");
        // SH2 is on screen2 (3 seats). Book 3 seats to get >70% occupancy first
        // Currently 0 booked. Book 2 seats → 66% (no surcharge yet)
        Booking bDemand1 = controller.bookTickets("SH2", Arrays.asList("S2-1", "S2-2"));
        // Sat 2PM, weekend only: SILVER=150+15=165, GOLD=250+25=275 → total=440
        check("2 seats at 66% occupancy, weekend: SILVER+GOLD = 440.0",
                bDemand1.getTotalPrice() == 440.0);
        // Now 2/3 booked = 66.7%. Book 3rd → seats are marked booked before pricing,
        // so occupancy at price-calc time = 3/3 = 100% ≥ 70% → demand surcharge applies
        Booking bDemand2 = controller.bookTickets("SH2", Arrays.asList("S2-3"));
        // PLATINUM at T1: 400 base + 10% weekend (40) + 15% demand (60) = 500
        check("3rd seat triggers demand surcharge: PLATINUM = 500.0",
                bDemand2.getTotalPrice() == 500.0);

        // =====================================================
        // SUMMARY
        // =====================================================
        System.out.println("\n========================================");
        System.out.println("RESULTS: " + passed + " passed, " + failed + " failed out of " + (passed + failed));
        if (failed == 0) {
            System.out.println("ALL ENDPOINTS VERIFIED SUCCESSFULLY!");
        } else {
            System.out.println("SOME TESTS FAILED — see above.");
        }
        System.out.println("========================================");
    }

    private static void check(String label, boolean condition) {
        if (condition) {
            System.out.println("  PASS: " + label);
            passed++;
        } else {
            System.out.println("  FAIL: " + label);
            failed++;
        }
    }
}
