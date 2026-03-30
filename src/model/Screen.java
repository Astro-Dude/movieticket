package model;

import java.util.List;

public class Screen {
    private final String id;
    private final String name;
    private final List<Seat> seats;

    public Screen(String id, String name, List<Seat> seats) {
        this.id = id;
        this.name = name;
        this.seats = seats;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public List<Seat> getSeats() { return seats; }

    @Override
    public String toString() {
        return "Screen{" + name + ", seats=" + seats.size() + "}";
    }
}
