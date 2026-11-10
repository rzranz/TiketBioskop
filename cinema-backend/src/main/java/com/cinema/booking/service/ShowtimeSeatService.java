package com.cinema.booking.service;

import com.cinema.booking.exception.BusinessException;
import com.cinema.booking.exception.ResourceNotFoundException;
import com.cinema.booking.exception.SeatNotAvailableException;
import com.cinema.booking.model.SeatStatus;
import com.cinema.booking.model.ShowtimeSeat;
import com.cinema.booking.repository.ShowtimeSeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Service Manajemen Inventaris Kursi per Jadwal Tayang.
 * Mengelola status AVAILABLE, HELD (abu-abu saat checkout dengan TTL timer), dan BOOKED.
 */
@Service
public class ShowtimeSeatService {

    private final ShowtimeSeatRepository showtimeSeatRepository;

    public ShowtimeSeatService(ShowtimeSeatRepository showtimeSeatRepository) {
        this.showtimeSeatRepository = showtimeSeatRepository;
    }

    @Transactional(readOnly = true)
    public List<ShowtimeSeat> getSeatsForShowtime(Long showtimeId) {
        List<ShowtimeSeat> seats = showtimeSeatRepository.findByShowtimeIdOrderBySeatRowAscSeatNumberAsc(showtimeId);
        // Pastikan status HELD yang sudah lewat waktu otomatis terefleksi
        return seats;
    }

    /**
     * Mengunci sementara kursi (Status HELD / Abu-Abu) selama masa checkout/pembayaran (misal 5 menit).
     */
    @Transactional
    public Map<String, Object> holdSeats(Long showtimeId, List<String> seatCodes, String customerName, int durationMinutes) {
        // Ambil kursi dengan database lock
        List<ShowtimeSeat> seats = showtimeSeatRepository.findByShowtimeIdAndSeatCodeInForUpdate(showtimeId, seatCodes);

        if (seats.size() != seatCodes.size()) {
            throw new ResourceNotFoundException("Satu atau lebih kursi tidak ditemukan pada jadwal ini.");
        }

        for (ShowtimeSeat seat : seats) {
            if (!seat.isCurrentlyAvailable()) {
                throw new SeatNotAvailableException(seat.getSeatCode());
            }
        }

        for (ShowtimeSeat seat : seats) {
            seat.hold(customerName != null ? customerName : "Guest", durationMinutes);
        }
        showtimeSeatRepository.saveAll(seats);

        Instant expiresAt = Instant.now().plusSeconds(durationMinutes * 60L);
        return Map.of(
            "showtimeId", showtimeId,
            "heldSeats", seatCodes,
            "expiresAt", expiresAt,
            "durationMinutes", durationMinutes,
            "status", "HELD"
        );
    }

    @Transactional
    public void releaseSeats(Long showtimeId, List<String> seatCodes) {
        List<ShowtimeSeat> seats = showtimeSeatRepository.findByShowtimeIdAndSeatCodeInForUpdate(showtimeId, seatCodes);
        for (ShowtimeSeat seat : seats) {
            if (seat.getStatus() == SeatStatus.HELD) {
                seat.release();
            }
        }
        showtimeSeatRepository.saveAll(seats);
    }
}

