package com.cinema.booking.exception;

public class SeatNotAvailableException extends BusinessException {
    public SeatNotAvailableException(String seatCode) {
        super("Kursi " + seatCode + " sudah dipesan atau tidak lagi tersedia.");
    }
}

