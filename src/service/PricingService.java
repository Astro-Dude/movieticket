package service;

import enums.SeatType;
import model.Seat;
import model.Show;
import strategy.PricingRule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Computes the final price for a seat in a show.
 * Final price = base price + sum of all surcharges from registered pricing rules.
 *
 * Base price is keyed on (theatreId, seatType) — each theatre sets its own base rates.
 */
public class PricingService {
    // Key: "theatreId:SeatType" -> base price
    private final Map<String, Double> basePriceMap = new HashMap<>();
    private final List<PricingRule> rules = new ArrayList<>();

    public void setBasePrice(String theatreId, SeatType seatType, double price) {
        basePriceMap.put(theatreId + ":" + seatType.name(), price);
    }

    public void addRule(PricingRule rule) {
        rules.add(rule);
    }

    public double calculatePrice(Show show, Seat seat) {
        String key = show.getTheatre().getId() + ":" + seat.getSeatType().name();
        Double basePrice = basePriceMap.get(key);
        if (basePrice == null) {
            throw new IllegalStateException("No base price configured for " + key);
        }

        double totalSurcharge = 0;
        for (PricingRule rule : rules) {
            totalSurcharge += rule.calculateSurcharge(basePrice, show, seat);
        }
        return basePrice + totalSurcharge;
    }

    public double calculateTotalPrice(Show show, List<Seat> seats) {
        double total = 0;
        for (Seat seat : seats) {
            total += calculatePrice(show, seat);
        }
        return total;
    }
}
