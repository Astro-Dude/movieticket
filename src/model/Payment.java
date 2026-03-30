package model;

import enums.PaymentStatus;

public class Payment {
    private final String id;
    private final double amount;
    private PaymentStatus status;

    public Payment(String id, double amount) {
        this.id = id;
        this.amount = amount;
        this.status = PaymentStatus.SUCCESS;
    }

    public String getId() { return id; }
    public double getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    @Override
    public String toString() {
        return "Payment{" + id + ", amount=" + amount + ", status=" + status + "}";
    }
}
