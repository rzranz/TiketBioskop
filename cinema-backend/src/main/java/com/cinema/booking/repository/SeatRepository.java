package com.cinema.booking.repository;

import com.cinema.booking.model.Seat;
import com.cinema.booking.model.SeatTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

/**
 * Repository Akses Data Kursi Bioskop.
 * Menggunakan Spring Data JPA sehingga query database dasar otomatis tersedia tanpa menulis SQL manual.
 */
@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    Optional<Seat> findBySeatCode(String seatCode);

    List<Seat> findByAvailableTrueOrderBySeatCodeAsc();

    List<Seat> findByTier(SeatTier tier);

    /**
     * Mengambil daftar kursi dengan PESSIMISTIC_WRITE lock.
     * Sesuai panduan .agents dan PRD: Mencegah race condition ketika 2 pembeli
     * memesan kursi yang sama secara simultan. Baris data dikunci hingga transaksi selesai.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.seatCode IN :seatCodes")
    List<Seat> findAllBySeatCodeInForUpdate(@Param("seatCodes") List<String> seatCodes);
}

