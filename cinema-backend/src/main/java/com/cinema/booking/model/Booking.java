package com.cinema.booking.model;

import com.cinema.booking.exception.BusinessException;
import com.cinema.booking.service.payment.PaymentResult;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Entity Agregat Pemesanan Tiket Bioskop.
 * Menerapkan Domain-Driven Design & State Transition Explicit Behavior (Bab 5.2 Panduan .agents).
 * Status tidak dapat diubah sembarangan via setter publik, melainkan melalui metode domain
 * yang memvalidasi invarian bisnis secara ketat.
 */
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_code", nullable = false, unique = true, length = 32)
    private String bookingCode;

    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "payment_amount", precision = 12, scale = 2)
    private BigDecimal paymentAmount;

    @Column(name = "change_amount", precision = 12, scale = 2)
    private BigDecimal changeAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 20)
    private PaymentMethod paymentMethod;

    @Column(name = "payment_reference", length = 64)
    private String paymentReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BookingStatus status = BookingStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "cancellation_reason", length = 255)
    private String cancellationReason;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "booking_seats",
        joinColumns = @JoinColumn(name = "booking_id"),
        inverseJoinColumns = @JoinColumn(name = "seat_id")
    )
    private List<Seat> seats = new ArrayList<>();

    protected Booking() {
        // Diperlukan oleh JPA
    }

    /**
     * Factory method untuk membuat entitas Booking baru dalam status PENDING.
     */
    public static Booking create(String bookingCode, String customerName, BigDecimal totalAmount, List<Seat> seats) {
        Booking booking = new Booking();
        booking.bookingCode = bookingCode;
        booking.customerName = customerName;
        booking.totalAmount = totalAmount;
        booking.status = BookingStatus.PENDING;
        booking.createdAt = Instant.now();
        booking.seats = new ArrayList<>(seats);
        return booking;
    }

    /**
     * Explicit State Transition: Konfirmasi Pembayaran.
     * Melindungi invarian: Hanya booking PENDING yang dapat dikonfirmasi.
     */
    public void confirmPayment(PaymentResult payment) {
        if (this.status != BookingStatus.PENDING) {
            throw new BusinessException(String.format(
                "Tidak dapat mengonfirmasi pemesanan %s karena status saat ini adalah %s",
                this.bookingCode, this.status
            ));
        }

        this.status = BookingStatus.CONFIRMED;
        this.paymentAmount = payment.amountPaid();
        this.changeAmount = payment.changeAmount();
        this.paymentMethod = payment.method();
        this.paymentReference = payment.transactionReference();
        this.confirmedAt = payment.processedAt();
    }

    /**
     * Explicit State Transition: Pembatalan Pemesanan.
     */
    public void cancel(String reason) {
        if (this.status == BookingStatus.CANCELLED) {
            throw new BusinessException("Pemesanan ini sudah dibatalkan sebelumnya.");
        }

        this.status = BookingStatus.CANCELLED;
        this.cancellationReason = reason;
        this.cancelledAt = Instant.now();

        // Kembalikan status kursi menjadi tersedia
        for (Seat seat : this.seats) {
            seat.setAvailable(true);
        }
    }

    // Getters Imutabel
    public Long getId() {
        return id;
    }

    public String getBookingCode() {
        return bookingCode;
    }

    public String getCustomerName() {
        return customerName;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public BigDecimal getChangeAmount() {
        return changeAmount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getConfirmedAt() {
        return confirmedAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public List<Seat> getSeats() {
        return Collections.unmodifiableList(seats);
    }
}
