package com.cinema.booking.service.payment;

import com.cinema.booking.exception.BusinessException;
import com.cinema.booking.model.PaymentMethod;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Factory & Registry untuk PaymentStrategy (Menerapkan Open-Closed Principle).
 * Menghubungkan metode pembayaran (CASH, QRIS) ke implementasi strateginya secara dinamis.
 */
@Component
public class PaymentStrategyFactory {

    private final Map<PaymentMethod, PaymentStrategy> strategies = new EnumMap<>(PaymentMethod.class);

    public PaymentStrategyFactory(List<PaymentStrategy> strategyList) {
        for (PaymentStrategy strategy : strategyList) {
            strategies.put(strategy.getSupportedMethod(), strategy);
        }
    }

    public PaymentStrategy getStrategy(PaymentMethod method) {
        PaymentMethod targetMethod = (method != null) ? method : PaymentMethod.CASH;
        PaymentStrategy strategy = strategies.get(targetMethod);
        if (strategy == null) {
            throw new BusinessException("Metode pembayaran " + targetMethod + " belum didukung.");
        }
        return strategy;
    }
}

