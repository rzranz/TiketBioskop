package com.cinema.booking.dto;

import com.cinema.booking.model.Seat;
import com.cinema.booking.model.SeatTier;

import java.math.BigDecimal;

/**
 * DTO Imutabel (Java Record) untuk mengembalikan informasi kursi ke klien.
 */
public record SeatResponse(
    Long id,
    String seatCode,
    String seatRow,
    Integer seatNumber,
    SeatTier tier,
    String tierName,
    BigDecimal price,
    boolean available
) {
    public static SeatResponse fromEntity(Seat seat) {
        return new SeatResponse(
            seat.getId(),
            seat.getSeatCode(),
            seat.getSeatRow(),
            seat.getSeatNumber(),
            seat.getTier(),
            seat.getTier().getDisplayName(),
            seat.getPrice(),
            seat.isAvailable()
        );
    }
}

