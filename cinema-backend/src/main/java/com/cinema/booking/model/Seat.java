package com.cinema.booking.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Entity Kursi Bioskop.
 * Menyimpan data fisik kursi, kategori (Reguler, Sweet Box, Satine Suite),
 * ketersediaan, dan versi locking untuk mencegah double-booking.
 */
@Entity
@Table(name = "seats", uniqueConstraints = {
    @UniqueConstraint(name = "uk_seat_code", columnNames = {"seat_code"})
})
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seat_code", nullable = false, length = 10)
    private String seatCode;

    @Column(name = "seat_row", nullable = false, length = 5)
    private String seatRow;

    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "tier", nullable = false, length = 30)
    private SeatTier tier;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "is_available", nullable = false)
    private boolean available = true;

    /**
     * Optimistic locking version: Spring Data JPA secara otomatis mengecek kolom ini.
     * Jika dua transaksi mencoba membooking kursi yang sama bersamaan, transaksi kedua akan gagal (OptimisticLockException).
     */
    @Version
    private Long version;

    public Seat() {
    }

    public Seat(String seatCode, String seatRow, Integer seatNumber, SeatTier tier, BigDecimal price) {
        this.seatCode = seatCode;
        this.seatRow = seatRow;
        this.seatNumber = seatNumber;
        this.tier = tier;
        this.price = price;
        this.available = true;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public String getSeatCode() {
        return seatCode;
    }

    public void setSeatCode(String seatCode) {
        this.seatCode = seatCode;
    }

    public String getSeatRow() {
        return seatRow;
    }

    public void setSeatRow(String seatRow) {
        this.seatRow = seatRow;
    }

    public Integer getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(Integer seatNumber) {
        this.seatNumber = seatNumber;
    }

    public SeatTier getTier() {
        return tier;
    }

    public void setTier(SeatTier tier) {
        this.tier = tier;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public Long getVersion() {
        return version;
    }
}

