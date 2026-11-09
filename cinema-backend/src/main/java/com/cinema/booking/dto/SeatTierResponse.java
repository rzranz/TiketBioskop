package com.cinema.booking.dto;

import com.cinema.booking.model.SeatTier;
import java.math.BigDecimal;

/**
 * DTO Record untuk informasi kategori/tier kursi dan harga dasarnya.
 */
public record SeatTierResponse(
    String code,
    String name,
    BigDecimal price
) {
    public static SeatTierResponse fromTier(SeatTier tier) {
        return new SeatTierResponse(tier.name(), tier.getDisplayName(), tier.getBasePrice());
    }
}

