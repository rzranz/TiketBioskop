package com.cinema.booking.model;

import java.math.BigDecimal;

/**
 * Kategori Kursi Bioskop.
 * Diambil dari pilihan form aplikasi lama: Reguler, Sweet Box, dan Satine Suite.
 * Menggunakan BigDecimal untuk nominal harga (standar finansial & panduan .agents).
 */
public enum SeatTier {
    REGULER("Reguler", new BigDecimal("45000.00")),
    SWEET_BOX("Sweet Box", new BigDecimal("75000.00")),
    SATINE_SUITE("Satine Suite", new BigDecimal("120000.00"));

    private final String displayName;
    private final BigDecimal basePrice;

    SeatTier(String displayName, BigDecimal basePrice) {
        this.displayName = displayName;
        this.basePrice = basePrice;
    }

    public String getDisplayName() {
        return displayName;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }
}

