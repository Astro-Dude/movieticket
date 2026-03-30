package model;

import enums.BookingStatus;
import java.util.List;

public class Booking {
    private final String id;
    private final Show show;
    private final List<Seat> seats;
    private final double totalPrice;
    private BookingStatus status;
    private Payment payment;

    public Booking(String id, Show show, List<Seat> seats, double totalPrice) {
        this.id = id;
        this.show = show;
        this.seats = seats;
        this.totalPrice = totalPrice;
        this.status = BookingStatus.CONFIRMED;
    }

    public String getId() { return id; }
    public Show getShow() { return show; }
    public List<Seat> getSeats() { return seats; }
    public double getTotalPrice() { return totalPrice; }
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }

    @Override
    public String toString() {
        return "Booking{" + id + ", " + show.getMovie().getName() + ", seats=" + seats.size()
                + ", price=" + totalPrice + ", status=" + status + "}";
    }
}
