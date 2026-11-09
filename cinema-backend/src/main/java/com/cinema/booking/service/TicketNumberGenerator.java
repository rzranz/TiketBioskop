package com.cinema.booking.service;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Generator Kode Unik Tiket Bioskop.
 * Memisahkan tanggung jawab formatting kode tiket dari service utama (Single Responsibility Principle).
 */
@Component
public class TicketNumberGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public String generateBookingCode() {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String shortId = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "TIX-" + timestamp + "-" + shortId;
    }
}

