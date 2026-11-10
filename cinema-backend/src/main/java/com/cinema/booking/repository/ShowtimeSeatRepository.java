package com.cinema.booking.repository;

import com.cinema.booking.model.ShowtimeSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShowtimeSeatRepository extends JpaRepository<ShowtimeSeat, Long> {

    List<ShowtimeSeat> findByShowtimeIdOrderBySeatRowAscSeatNumberAsc(Long showtimeId);

    Optional<ShowtimeSeat> findByShowtimeIdAndSeatCode(Long showtimeId, String seatCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM ShowtimeSeat s WHERE s.showtimeId = :showtimeId AND s.seatCode IN :seatCodes")
    List<ShowtimeSeat> findByShowtimeIdAndSeatCodeInForUpdate(
        @Param("showtimeId") Long showtimeId,
        @Param("seatCodes") List<String> seatCodes
    );
}

