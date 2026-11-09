package com.cinema.booking.service.payment;

import com.cinema.booking.model.PaymentMethod;

import java.math.BigDecimal;

/**
 * Strategy Pattern: Kontrak pemrosesan pembayaran bioskop.
 * Mengikuti panduan .agents bab 5 (Design Patterns).
 * Memisahkan logika pembayaran tunai loket vs digital/QRIS ke kelas terisolasi.
 */
public interface PaymentStrategy {

    PaymentMethod getSupportedMethod();

    PaymentResult process(BigDecimal totalAmount, BigDecimal paymentAmount);
}

