package com.cinema.booking.dto;

import com.cinema.booking.model.Booking;
import com.cinema.booking.model.BookingStatus;
import com.cinema.booking.model.PaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * DTO Response Tiket Digital Lengkap (Film, Studio, Kursi, Makanan, Barcode/PIN Klaim Kiosk).
 */
public record ComprehensiveBookingResponse(
    String bookingCode,
    String claimCode,
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
    PaymentMethod paymentMethod,
    BigDecimal paymentAmount,
    BigDecimal changeAmount,
    boolean ticketClaimed,
    boolean foodClaimed,
    BookingStatus status,
    Instant createdAt
) {
    public static ComprehensiveBookingResponse fromEntity(Booking b) {
        return new ComprehensiveBookingResponse(
            b.getBookingCode(),
            b.getClaimCode(),
            b.getCustomerName(),
            b.getMovieTitle(),
            b.getMoviePosterUrl(),
            b.getCinemaName(),
            b.getStudioName(),
            b.getShowDateTime(),
            b.getSeatCodesSummary(),
            b.getTotalTicketAmount(),
            b.getFoodSummary(),
            b.getTotalFoodAmount(),
            b.getGrandTotal(),
            b.getPaymentMethod(),
            b.getPaymentAmount(),
            b.getChangeAmount(),
            b.isTicketClaimed(),
            b.isFoodClaimed(),
            b.getStatus(),
            b.getCreatedAt()
        );
    }
}

