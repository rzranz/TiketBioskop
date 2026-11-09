package com.cinema.booking.service.payment;

import com.cinema.booking.exception.BusinessException;
import com.cinema.booking.model.PaymentMethod;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Implementasi Strategy untuk Pembayaran Digital / QRIS.
 * Mengharuskan nominal pembayaran tepat sama dengan tagihan (tanpa kembalian fisik).
 */
@Component
public class QrisPaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentMethod getSupportedMethod() {
        return PaymentMethod.QRIS;
    }

    @Override
    public PaymentResult process(BigDecimal totalAmount, BigDecimal paymentAmount) {
        if (paymentAmount == null || paymentAmount.compareTo(totalAmount) != 0) {
            throw new BusinessException(String.format(
                "Pembayaran QRIS memerlukan nominal presisi. Total: Rp %s, Dibayar: Rp %s",
                totalAmount.toPlainString(),
                paymentAmount != null ? paymentAmount.toPlainString() : "0"
            ));
        }

        String ref = "QRIS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        return new PaymentResult(
            true,
            PaymentMethod.QRIS,
            paymentAmount,
            BigDecimal.ZERO,
            ref,
            Instant.now()
        );
    }
}

