package com.cinema.booking.config;

import com.cinema.booking.model.Seat;
import com.cinema.booking.model.SeatTier;
import com.cinema.booking.repository.SeatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Initializer (Seeder) untuk mengisi tata letak kursi bioskop saat aplikasi dinyalakan.
 * Mengadopsi baris (A, B, C, D, E) dan jenis kursi (Satine Suite, Sweet Box, Reguler) dari aplikasi lama.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final SeatRepository seatRepository;

    public DataInitializer(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }

    @Override
    public void run(String... args) {
        if (seatRepository.count() > 0) {
            log.info("Database kursi sudah terisi, melewati tahap inisialisasi awal.");
            return;
        }

        log.info("Menginisialisasi layout kursi studio bioskop (A-E, Satine Suite, Sweet Box, Reguler)...");
        List<Seat> initialSeats = new ArrayList<>();

        // Baris A: Satine Suite (4 Kursi VIP Recliner: A1 - A4)
        for (int i = 1; i <= 4; i++) {
            initialSeats.add(new Seat("A" + i, "A", i, SeatTier.SATINE_SUITE, SeatTier.SATINE_SUITE.getBasePrice()));
        }

        // Baris B: Sweet Box (6 Kursi Couple Sofa: B1 - B6)
        for (int i = 1; i <= 6; i++) {
            initialSeats.add(new Seat("B" + i, "B", i, SeatTier.SWEET_BOX, SeatTier.SWEET_BOX.getBasePrice()));
        }

        // Baris C, D, E: Reguler (8 Kursi per baris: C1-C8, D1-D8, E1-E8)
        String[] regulerRows = {"C", "D", "E"};
        for (String row : regulerRows) {
            for (int i = 1; i <= 8; i++) {
                initialSeats.add(new Seat(row + i, row, i, SeatTier.REGULER, SeatTier.REGULER.getBasePrice()));
            }
        }

        seatRepository.saveAll(initialSeats);
        log.info("Berhasil menginisialisasi {} kursi di dalam studio.", initialSeats.size());
    }
}

