package strategy;

import model.Show;
import model.Seat;

/**
 * Adds a 15% surcharge when more than 70% of seats are already booked.
 */
public class HighDemandPricingRule implements PricingRule {
    private static final double DEMAND_THRESHOLD = 0.70;

    @Override
    public double calculateSurcharge(double basePrice, Show show, Seat seat) {
        int totalSeats = show.getScreen().getSeats().size();
        int bookedSeats = show.getBookedSeatIds().size();
        double occupancy = (double) bookedSeats / totalSeats;
        if (occupancy >= DEMAND_THRESHOLD) {
            return basePrice * 0.15;
        }
        return 0;
    }
}
