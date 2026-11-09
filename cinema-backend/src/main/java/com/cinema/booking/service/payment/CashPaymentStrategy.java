package com.cinema.booking.service.payment;

import com.cinema.booking.exception.InsufficientPaymentException;
import com.cinema.booking.model.PaymentMethod;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Implementasi Strategy untuk Pembayaran Tunai (Cash di Loket).
 * Mengadopsi mekanisme hitung pembayaran dan kembalian dari aplikasi lama.
 */
@Component
public class CashPaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentMethod getSupportedMethod() {
        return PaymentMethod.CASH;
    }

    @Override
    public PaymentResult process(BigDecimal totalAmount, BigDecimal paymentAmount) {
        if (paymentAmount == null || paymentAmount.compareTo(totalAmount) < 0) {
            throw new InsufficientPaymentException(totalAmount, paymentAmount != null ? paymentAmount : BigDecimal.ZERO);
        }

        BigDecimal change = paymentAmount.subtract(totalAmount);
        String ref = "CASH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        return new PaymentResult(
            true,
            PaymentMethod.CASH,
            paymentAmount,
            change,
            ref,
            Instant.now()
        );
    }
}

