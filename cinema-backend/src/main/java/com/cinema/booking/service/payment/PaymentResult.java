package com.cinema.booking.service.payment;

import com.cinema.booking.model.PaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Nilai hasil pemrosesan pembayaran (Imutabel Record).
 */
public record PaymentResult(
    boolean successful,
    PaymentMethod method,
    BigDecimal amountPaid,
    BigDecimal changeAmount,
    String transactionReference,
    Instant processedAt
) {}

