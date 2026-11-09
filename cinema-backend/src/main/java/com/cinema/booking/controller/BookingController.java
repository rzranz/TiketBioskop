package com.cinema.booking.controller;

import com.cinema.booking.dto.ApiResponse;
import com.cinema.booking.dto.BookingResponse;
import com.cinema.booking.dto.CreateBookingRequest;
import com.cinema.booking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller untuk Pemesanan, Pembatalan, dan Pengecekan Tiket Bioskop.
 */
@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * Endpoint Transaksi Pemesanan Tiket.
     * Menggantikan tombol 'Input Data' di aplikasi lama.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        BookingResponse response = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok("Pemesanan tiket bioskop berhasil dikonfirmasi!", response));
    }

    /**
     * Mengambil detail pemesanan berdasarkan kode booking unik.
     * Menggantikan tombol 'Print Data' di aplikasi lama.
     */
    @GetMapping("/{bookingCode}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingByCode(@PathVariable String bookingCode) {
        BookingResponse response = bookingService.getBookingByCode(bookingCode);
        return ResponseEntity.ok(ApiResponse.ok("Detail tiket ditemukan", response));
    }

    /**
     * Membatalkan pemesanan dan mengembalikan status kursi menjadi tersedia.
     */
    @PostMapping("/{bookingCode}/cancel")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(
        @PathVariable String bookingCode,
        @RequestBody(required = false) Map<String, String> body
    ) {
        String reason = (body != null) ? body.get("reason") : null;
        BookingResponse response = bookingService.cancelBooking(bookingCode, reason);
        return ResponseEntity.ok(ApiResponse.ok("Pemesanan berhasil dibatalkan dan kursi telah dibebaskan", response));
    }

    /**
     * Mengambil seluruh riwayat pemesanan tiket di sistem.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getAllBookings() {
        return ResponseEntity.ok(ApiResponse.ok("Daftar riwayat pemesanan", bookingService.getAllBookings()));
    }
}
