package com.cinema.booking.exception;

import java.math.BigDecimal;

public class InsufficientPaymentException extends BusinessException {
    public InsufficientPaymentException(BigDecimal required, BigDecimal provided) {
        super(String.format("Pembayaran tidak mencukupi. Total tagihan: Rp %s, Uang dibayarkan: Rp %s",
            required.toPlainString(), provided.toPlainString()));
    }
}

