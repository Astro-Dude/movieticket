package strategy;

import model.Show;
import model.Seat;
import java.time.DayOfWeek;

/**
 * Adds a 10% surcharge for Saturday and Sunday shows.
 */
public class WeekendPricingRule implements PricingRule {
    @Override
    public double calculateSurcharge(double basePrice, Show show, Seat seat) {
        DayOfWeek day = show.getStartTime().getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            return basePrice * 0.10;
        }
        return 0;
    }
}
