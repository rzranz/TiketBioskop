package com.cinema.booking.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Entity Status Kursi per Jadwal Tayang (Showtime Inventory).
 * Menangani status AVAILABLE, HELD (abu-abu saat proses bayar), dan BOOKED (merah setelah lunas).
 */
@Entity
@Table(name = "showtime_seats", uniqueConstraints = {
    @UniqueConstraint(name = "uk_showtime_seat", columnNames = {"showtime_id", "seat_code"})
})
public class ShowtimeSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "showtime_id", nullable = false)
    private Long showtimeId;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SeatStatus status = SeatStatus.AVAILABLE;

    @Column(name = "held_until")
    private Instant heldUntil;

    @Column(name = "held_by", length = 100)
    private String heldBy;

    @Version
    private Long version;

    public ShowtimeSeat() {
    }

    public ShowtimeSeat(Long showtimeId, String seatCode, String seatRow, Integer seatNumber, 
                        SeatTier tier, BigDecimal price) {
        this.showtimeId = showtimeId;
        this.seatCode = seatCode;
        this.seatRow = seatRow;
        this.seatNumber = seatNumber;
        this.tier = tier;
        this.price = price;
        this.status = SeatStatus.AVAILABLE;
    }

    public boolean isCurrentlyAvailable() {
        if (status == SeatStatus.BOOKED) return false;
        if (status == SeatStatus.HELD) {
            // Cek apakah masa hold sudah kadaluarsa (TTL expired)
            if (heldUntil != null && Instant.now().isAfter(heldUntil)) {
                return true; // otomatis dianggap available jika waktu habis
            }
            return false;
        }
        return true;
    }

    public void hold(String customerIdentifier, int durationMinutes) {
        this.status = SeatStatus.HELD;
        this.heldBy = customerIdentifier;
        this.heldUntil = Instant.now().plusSeconds(durationMinutes * 60L);
    }

    public void release() {
        this.status = SeatStatus.AVAILABLE;
        this.heldBy = null;
        this.heldUntil = null;
    }

    public void book() {
        this.status = SeatStatus.BOOKED;
        this.heldBy = null;
        this.heldUntil = null;
    }

    public Long getId() {
        return id;
    }

    public Long getShowtimeId() {
        return showtimeId;
    }

    public String getSeatCode() {
        return seatCode;
    }

    public String getSeatRow() {
        return seatRow;
    }

    public Integer getSeatNumber() {
        return seatNumber;
    }

    public SeatTier getTier() {
        return tier;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public SeatStatus getStatus() {
        // Otomatis update status jika masa HELD lewat waktu
        if (status == SeatStatus.HELD && heldUntil != null && Instant.now().isAfter(heldUntil)) {
            return SeatStatus.AVAILABLE;
        }
        return status;
    }

    public Instant getHeldUntil() {
        return heldUntil;
    }

    public String getHeldBy() {
        return heldBy;
    }
}

