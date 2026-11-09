package com.cinema.booking.service;

import com.cinema.booking.dto.SeatResponse;
import com.cinema.booking.dto.SeatTierResponse;
import com.cinema.booking.model.SeatTier;
import com.cinema.booking.repository.SeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * Service untuk mengelola informasi kursi bioskop.
 */
@Service
@Transactional(readOnly = true)
public class SeatService {

    private final SeatRepository seatRepository;

    public SeatService(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }

    public List<SeatResponse> getAllSeats() {
        return seatRepository.findAll().stream()
            .map(SeatResponse::fromEntity)
            .toList();
    }

    public List<SeatResponse> getAvailableSeats() {
        return seatRepository.findByAvailableTrueOrderBySeatCodeAsc().stream()
            .map(SeatResponse::fromEntity)
            .toList();
    }

    public List<SeatTierResponse> getSeatTiers() {
        return Arrays.stream(SeatTier.values())
            .map(SeatTierResponse::fromTier)
            .toList();
    }
}
