package com.cinema.booking.controller;

import com.cinema.booking.dto.ApiResponse;
import com.cinema.booking.dto.HoldSeatRequest;
import com.cinema.booking.model.ShowtimeSeat;
import com.cinema.booking.service.ShowtimeSeatService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/showtimes")
@CrossOrigin(origins = "*")
public class ShowtimeController {

    private final ShowtimeSeatService showtimeSeatService;

    public ShowtimeController(ShowtimeSeatService showtimeSeatService) {
        this.showtimeSeatService = showtimeSeatService;
    }

    @GetMapping("/{id}/seats")
    public ResponseEntity<ApiResponse<List<ShowtimeSeat>>> getShowtimeSeats(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(showtimeSeatService.getSeatsForShowtime(id)));
    }

    @PostMapping("/{id}/hold")
    public ResponseEntity<ApiResponse<Map<String, Object>>> holdSeats(
        @PathVariable Long id,
        @Valid @RequestBody HoldSeatRequest request
    ) {
        Map<String, Object> result = showtimeSeatService.holdSeats(id, request.seatCodes(), request.customerName(), 5);
        return ResponseEntity.ok(ApiResponse.ok("Kursi berhasil ditahan selama 5 menit untuk pembayaran (Warna Abu-Abu)", result));
    }

    @PostMapping("/{id}/release")
    public ResponseEntity<ApiResponse<String>> releaseSeats(
        @PathVariable Long id,
        @RequestBody Map<String, List<String>> body
    ) {
        List<String> codes = body.get("seatCodes");
        if (codes != null && !codes.isEmpty()) {
            showtimeSeatService.releaseSeats(id, codes);
        }
        return ResponseEntity.ok(ApiResponse.ok("Kursi dikembalikan ke status tersedia", null));
    }
}

