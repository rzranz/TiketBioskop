package com.cinema.booking.dto;

import com.cinema.booking.model.Booking;
import com.cinema.booking.model.BookingStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * DTO Response konfirmasi pemesanan tiket (Digital Ticket Summary).
 */
public record BookingResponse(
    String bookingCode,
    String customerName,
    int ticketCount,
    List<String> bookedSeats,
    BigDecimal totalAmount,
    BigDecimal paymentAmount,
    BigDecimal changeAmount,
    BookingStatus status,
    Instant createdAt
) {
    public static BookingResponse fromEntity(Booking booking) {
        List<String> seats = booking.getSeats().stream()
            .map(s -> s.getSeatCode() + " (" + s.getTier().getDisplayName() + ")")
            .toList();

        return new BookingResponse(
            booking.getBookingCode(),
            booking.getCustomerName(),
            booking.getSeats().size(),
            seats,
            booking.getTotalAmount(),
            booking.getPaymentAmount(),
            booking.getChangeAmount(),
            booking.getStatus(),
            booking.getCreatedAt()
        );
    }
}

