package strategy;

import model.Show;
import model.Seat;

/**
 * Strategy interface for dynamic pricing.
 * Each rule can add a surcharge on top of the base price.
 */
public interface PricingRule {
    double calculateSurcharge(double basePrice, Show show, Seat seat);
}
