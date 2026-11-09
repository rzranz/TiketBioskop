package com.cinema.booking.controller;

import com.cinema.booking.dto.ApiResponse;
import com.cinema.booking.dto.SeatResponse;
import com.cinema.booking.dto.SeatTierResponse;
import com.cinema.booking.service.SeatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller untuk Endpoint Kursi Bioskop.
 */
@RestController
@RequestMapping("/api/seats")
@CrossOrigin(origins = "*")
public class SeatController {

    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SeatResponse>>> getAllSeats() {
        return ResponseEntity.ok(ApiResponse.ok("Daftar seluruh kursi berhasil diambil", seatService.getAllSeats()));
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<SeatResponse>>> getAvailableSeats() {
        return ResponseEntity.ok(ApiResponse.ok("Daftar kursi yang tersedia", seatService.getAvailableSeats()));
    }

    @GetMapping("/tiers")
    public ResponseEntity<ApiResponse<List<SeatTierResponse>>> getSeatTiers() {
        return ResponseEntity.ok(ApiResponse.ok("Daftar kategori dan harga kursi", seatService.getSeatTiers()));
    }
}
