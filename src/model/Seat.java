package model;

import enums.SeatType;

public class Seat {
    private final String id;
    private final int rowNumber;
    private final int seatNumber;
    private final SeatType seatType;

    public Seat(String id, int rowNumber, int seatNumber, SeatType seatType) {
        this.id = id;
        this.rowNumber = rowNumber;
        this.seatNumber = seatNumber;
        this.seatType = seatType;
    }

    public String getId() { return id; }
    public int getRowNumber() { return rowNumber; }
    public int getSeatNumber() { return seatNumber; }
    public SeatType getSeatType() { return seatType; }

    @Override
    public String toString() {
        return "Seat{R" + rowNumber + "-S" + seatNumber + " " + seatType + "}";
    }
}
