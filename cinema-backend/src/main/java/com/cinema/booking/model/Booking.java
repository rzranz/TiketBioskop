package com.cinema.booking.model;

import com.cinema.booking.exception.BusinessException;
import com.cinema.booking.service.payment.PaymentResult;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Entity Agregat Pemesanan Tiket Bioskop & Makanan (F&B).
 * Menyimpan data pemesanan terpadu: Film, Jadwal, Kursi, Makanan, Kode Klaim Kiosk, dan Status.
 */
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_code", nullable = false, unique = true, length = 32)
    private String bookingCode;

    @Column(name = "claim_code", nullable = false, length = 20)
    private String claimCode; // Kode acak 6-digit untuk mengambil tiket di mesin kiosk

    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @Column(name = "movie_title", length = 150)
    private String movieTitle;

    @Column(name = "movie_poster_url", length = 500)
    private String moviePosterUrl;

    @Column(name = "cinema_name", length = 150)
    private String cinemaName;

    @Column(name = "studio_name", length = 100)
    private String studioName;

    @Column(name = "show_date_time", length = 50)
    private String showDateTime;

    @Column(name = "seat_codes_summary", length = 100)
    private String seatCodesSummary;

    @Column(name = "total_ticket_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalTicketAmount = BigDecimal.ZERO;

    @Lob
    @Column(name = "food_summary", columnDefinition = "TEXT")
    private String foodSummary; // ringkasan makanan yang dipesan

    @Column(name = "total_food_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalFoodAmount = BigDecimal.ZERO;

    @Column(name = "grand_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal grandTotal;

    @Column(name = "payment_amount", precision = 12, scale = 2)
    private BigDecimal paymentAmount;

    @Column(name = "change_amount", precision = 12, scale = 2)
    private BigDecimal changeAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 20)
    private PaymentMethod paymentMethod;

    @Column(name = "payment_reference", length = 64)
    private String paymentReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BookingStatus status = BookingStatus.PENDING;

    @Column(name = "is_ticket_claimed", nullable = false)
    private boolean ticketClaimed = false;

    @Column(name = "is_food_claimed", nullable = false)
    private boolean foodClaimed = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "booking_seats",
        joinColumns = @JoinColumn(name = "booking_id"),
        inverseJoinColumns = @JoinColumn(name = "seat_id")
    )
    private List<Seat> seats = new ArrayList<>();

    protected Booking() {
    }

    public static Booking create(String bookingCode, String customerName, BigDecimal totalAmount, List<Seat> seats) {
        return create(
            bookingCode,
            customerName,
            "Film Studio Premiere",
            "",
            "Bioskop CineMax",
            "Studio 1",
            "Hari Ini",
            seats.stream().map(Seat::getSeatCode).reduce((a, b) -> a + ", " + b).orElse(""),
            totalAmount,
            "Tidak ada pesanan makanan",
            BigDecimal.ZERO,
            totalAmount,
            seats
        );
    }

    public static Booking create(
        String bookingCode, 
        String customerName, 
        String movieTitle,
        String moviePosterUrl,
        String cinemaName,
        String studioName,
        String showDateTime,
        String seatCodesSummary,
        BigDecimal totalTicketAmount,
        String foodSummary,
        BigDecimal totalFoodAmount,
        BigDecimal grandTotal,
        List<Seat> seats
    ) {
        Booking b = new Booking();
        b.bookingCode = bookingCode;
        // Generate random 6-digit claim PIN (misal: "CLM-482915")
        b.claimCode = "CLM-" + (100000 + new Random().nextInt(900000));
        b.customerName = customerName;
        b.movieTitle = movieTitle;
        b.moviePosterUrl = moviePosterUrl;
        b.cinemaName = cinemaName;
        b.studioName = studioName;
        b.showDateTime = showDateTime;
        b.seatCodesSummary = seatCodesSummary;
        b.totalTicketAmount = totalTicketAmount;
        b.foodSummary = foodSummary != null ? foodSummary : "Tidak ada pesanan makanan";
        b.totalFoodAmount = totalFoodAmount != null ? totalFoodAmount : BigDecimal.ZERO;
        b.grandTotal = grandTotal;
        b.status = BookingStatus.PENDING;
        b.ticketClaimed = false;
        b.foodClaimed = false;
        b.createdAt = Instant.now();
        b.seats = new ArrayList<>(seats != null ? seats : List.of());
        return b;
    }

    public void confirmPayment(PaymentResult payment) {
        if (this.status != BookingStatus.PENDING) {
            throw new BusinessException("Hanya pemesanan PENDING yang dapat dikonfirmasi.");
        }
        this.status = BookingStatus.CONFIRMED;
        this.paymentAmount = payment.amountPaid();
        this.changeAmount = payment.changeAmount();
        this.paymentMethod = payment.method();
        this.paymentReference = payment.transactionReference();
        this.confirmedAt = payment.processedAt();
    }

    public void claimTicketAtKiosk() {
        if (this.status != BookingStatus.CONFIRMED) {
            throw new BusinessException("Tiket belum lunas dibayar.");
        }
        if (this.ticketClaimed) {
            throw new BusinessException("Tiket fisik sudah pernah dicetak sebelumnya.");
        }
        this.ticketClaimed = true;
    }

    public void claimFoodAtCounter() {
        if (this.status != BookingStatus.CONFIRMED) {
            throw new BusinessException("Pesanan belum lunas dibayar.");
        }
        if (this.foodClaimed) {
            throw new BusinessException("Makanan sudah pernah diambil di counter.");
        }
        this.foodClaimed = true;
    }

    public void cancel(String reason) {
        if (this.status == BookingStatus.CANCELLED) {
            throw new BusinessException("Pemesanan sudah dibatalkan sebelumnya.");
        }
        this.status = BookingStatus.CANCELLED;
        this.cancelledAt = Instant.now();
        for (Seat s : seats) {
            s.setAvailable(true);
        }
    }

    // Getters
    public Long getId() { return id; }
    public String getBookingCode() { return bookingCode; }
    public String getClaimCode() { return claimCode; }
    public String getCustomerName() { return customerName; }
    public String getMovieTitle() { return movieTitle; }
    public String getMoviePosterUrl() { return moviePosterUrl; }
    public String getCinemaName() { return cinemaName; }
    public String getStudioName() { return studioName; }
    public String getShowDateTime() { return showDateTime; }
    public String getSeatCodesSummary() { return seatCodesSummary; }
    public BigDecimal getTotalAmount() { return grandTotal != null ? grandTotal : totalTicketAmount; }
    public BigDecimal getTotalTicketAmount() { return totalTicketAmount; }
    public String getFoodSummary() { return foodSummary; }
    public BigDecimal getTotalFoodAmount() { return totalFoodAmount; }
    public BigDecimal getGrandTotal() { return grandTotal; }
    public BigDecimal getPaymentAmount() { return paymentAmount; }
    public BigDecimal getChangeAmount() { return changeAmount; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public String getPaymentReference() { return paymentReference; }
    public BookingStatus getStatus() { return status; }
    public boolean isTicketClaimed() { return ticketClaimed; }
    public boolean isFoodClaimed() { return foodClaimed; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getConfirmedAt() { return confirmedAt; }
    public List<Seat> getSeats() { return Collections.unmodifiableList(seats); }
}
