package com.cinema.booking.controller;

import com.cinema.booking.dto.ApiResponse;
import com.cinema.booking.dto.ComprehensiveBookingRequest;
import com.cinema.booking.dto.ComprehensiveBookingResponse;
import com.cinema.booking.service.ComprehensiveBookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v2/bookings")
@CrossOrigin(origins = "*")
public class ComprehensiveBookingController {

    private final ComprehensiveBookingService comprehensiveBookingService;

    public ComprehensiveBookingController(ComprehensiveBookingService comprehensiveBookingService) {
        this.comprehensiveBookingService = comprehensiveBookingService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ComprehensiveBookingResponse>> createBooking(
        @Valid @RequestBody ComprehensiveBookingRequest request
    ) {
        ComprehensiveBookingResponse response = comprehensiveBookingService.createComprehensiveBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok("Pemesanan tiket & makanan berhasil dikonfirmasi!", response));
    }

    @PostMapping("/claim-ticket")
    public ResponseEntity<ApiResponse<ComprehensiveBookingResponse>> claimTicket(
        @RequestBody Map<String, String> body
    ) {
        String claimCode = body.get("claimCode");
        ComprehensiveBookingResponse response = comprehensiveBookingService.claimTicketAtKiosk(claimCode);
        return ResponseEntity.ok(ApiResponse.ok("Tiket fisik berhasil dicetak di Kiosk!", response));
    }

    @PostMapping("/claim-food")
    public ResponseEntity<ApiResponse<ComprehensiveBookingResponse>> claimFood(
        @RequestBody Map<String, String> body
    ) {
        String claimCode = body.get("claimCode");
        ComprehensiveBookingResponse response = comprehensiveBookingService.claimFoodAtCounter(claimCode);
        return ResponseEntity.ok(ApiResponse.ok("Makanan & Minuman berhasil diambil di Counter F&B!", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ComprehensiveBookingResponse>>> getAllActiveBookings() {
        return ResponseEntity.ok(ApiResponse.ok("Daftar tiket aktif", comprehensiveBookingService.getAllActiveBookings()));
    }
}

