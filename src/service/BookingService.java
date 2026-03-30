package service;

import enums.BookingStatus;
import enums.PaymentStatus;
import exception.SeatAlreadyBookedException;
import model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Handles seat booking with concurrency control and cancellation with refund.
 *
 * Concurrency for seat booking:
 * - A ReentrantLock per show ensures that two users trying to book
 *   overlapping seats on the same show are serialized.
 * - Different shows can be booked in parallel (no contention).
 *
 * Booking flow:
 * 1. Acquire lock for the show
 * 2. Validate all requested seats are available
 * 3. Mark seats as booked atomically
 * 4. Calculate price, create payment, create booking
 * 5. Release lock
 *
 * If any seat is already booked, no seats are reserved (all-or-nothing).
 */
public class BookingService {
    private final Map<String, Booking> bookingMap = new ConcurrentHashMap<>();
    // Lock per show for seat booking atomicity
    private final Map<String, ReentrantLock> showLocks = new ConcurrentHashMap<>();
    private final PricingService pricingService;

    public BookingService(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    /**
     * Book tickets for a show.
     * @param show the show to book
     * @param seatIds list of seat IDs to book
     * @return the confirmed Booking
     * @throws SeatAlreadyBookedException if any seat is already taken
     */
    public Booking bookTickets(Show show, List<String> seatIds) {
        ReentrantLock lock = showLocks.computeIfAbsent(show.getId(), k -> new ReentrantLock());
        lock.lock();
        try {
            // Resolve seat objects from screen
            List<Seat> requestedSeats = new ArrayList<>();
            for (String seatId : seatIds) {
                Seat seat = show.getScreen().getSeats().stream()
                        .filter(s -> s.getId().equals(seatId))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Invalid seat ID: " + seatId));
                requestedSeats.add(seat);
            }

            // Check availability — all-or-nothing
            for (Seat seat : requestedSeats) {
                if (show.getBookedSeatIds().contains(seat.getId())) {
                    throw new SeatAlreadyBookedException(
                            "Seat " + seat + " is already booked for show " + show.getId());
                }
            }

            // Mark all seats as booked atomically (under lock)
            for (Seat seat : requestedSeats) {
                show.getBookedSeatIds().add(seat.getId());
            }

            // Calculate price and create booking
            double totalPrice = pricingService.calculateTotalPrice(show, requestedSeats);
            String bookingId = "BKG-" + UUID.randomUUID().toString().substring(0, 8);
            Booking booking = new Booking(bookingId, show, requestedSeats, totalPrice);

            // Create payment
            Payment payment = new Payment("PAY-" + UUID.randomUUID().toString().substring(0, 8), totalPrice);
            booking.setPayment(payment);

            bookingMap.put(bookingId, booking);
            return booking;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Cancel a booking and process refund.
     * Releases the seats back to the show and marks payment as refunded.
     */
    public Booking cancelBooking(String bookingId) {
        Booking booking = bookingMap.get(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("Booking not found: " + bookingId);
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking already cancelled: " + bookingId);
        }

        Show show = booking.getShow();
        ReentrantLock lock = showLocks.computeIfAbsent(show.getId(), k -> new ReentrantLock());
        lock.lock();
        try {
            // Release seats
            for (Seat seat : booking.getSeats()) {
                show.getBookedSeatIds().remove(seat.getId());
            }
            // Update booking status
            booking.setStatus(BookingStatus.CANCELLED);
            // Process refund
            booking.getPayment().setStatus(PaymentStatus.REFUNDED);
            return booking;
        } finally {
            lock.unlock();
        }
    }

    public Booking getBooking(String bookingId) {
        return bookingMap.get(bookingId);
    }
}
