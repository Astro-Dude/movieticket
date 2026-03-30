package strategy;

import model.Show;
import model.Seat;

/**
 * Adds a 20% surcharge for shows starting between 6 PM and 9 PM.
 */
public class PeakHourPricingRule implements PricingRule {
    @Override
    public double calculateSurcharge(double basePrice, Show show, Seat seat) {
        int hour = show.getStartTime().getHour();
        if (hour >= 18 && hour <= 21) {
            return basePrice * 0.20;
        }
        return 0;
    }
}
