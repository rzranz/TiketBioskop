package com.cinema.booking.service;

import com.cinema.booking.dto.BookingResponse;
import com.cinema.booking.dto.CreateBookingRequest;
import com.cinema.booking.exception.InsufficientPaymentException;
import com.cinema.booking.exception.ResourceNotFoundException;
import com.cinema.booking.exception.SeatNotAvailableException;
import com.cinema.booking.model.Booking;
import com.cinema.booking.model.Seat;
import com.cinema.booking.repository.BookingRepository;
import com.cinema.booking.repository.SeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Service Logika Bisnis Pemesanan Tiket Bioskop.
 * Menerapkan pola transaksi ACID dan Database Locking untuk mencegah Double-Booking.
 */
@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final SeatRepository seatRepository;

    public BookingService(BookingRepository bookingRepository, SeatRepository seatRepository) {
        this.bookingRepository = bookingRepository;
        this.seatRepository = seatRepository;
    }

    /**
     * Membuat reservasi dan konfirmasi pemesanan tiket.
     * Menggunakan @Transactional untuk menjamin atomisitas.
     */
    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {
        // 1. Ambil kursi dengan Pessimistic Write Lock (mencegah booking ganda serentak)
        List<Seat> seats = seatRepository.findAllBySeatCodeInForUpdate(request.seatCodes());

        // Validasi kelengkapan kursi yang diminta
        if (seats.size() != request.seatCodes().size()) {
            throw new ResourceNotFoundException("Satu atau lebih kursi yang diminta tidak ditemukan di sistem.");
        }

        // 2. Validasi ketersediaan masing-masing kursi
        for (Seat seat : seats) {
            if (!seat.isAvailable()) {
                throw new SeatNotAvailableException(seat.getSeatCode());
            }
        }

        // 3. Hitung total biaya menggunakan BigDecimal
        BigDecimal totalAmount = seats.stream()
            .map(Seat::getPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. Validasi pembayaran cukup
        if (request.paymentAmount().compareTo(totalAmount) < 0) {
            throw new InsufficientPaymentException(totalAmount, request.paymentAmount());
        }

        // 5. Hitung kembalian
        BigDecimal changeAmount = request.paymentAmount().subtract(totalAmount);

        // 6. Kunci kursi (ubah status menjadi tidak tersedia)
        for (Seat seat : seats) {
            seat.setAvailable(false);
        }
        seatRepository.saveAll(seats);

        // 7. Generate kode booking unik
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String shortId = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        String bookingCode = "TIX-" + timestamp + "-" + shortId;

        // 8. Buat dan simpan entitas Booking
        Booking booking = new Booking(
            bookingCode,
            request.customerName().trim(),
            totalAmount,
            request.paymentAmount(),
            changeAmount,
            seats
        );
        Booking savedBooking = bookingRepository.save(booking);

        return BookingResponse.fromEntity(savedBooking);
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingByCode(String bookingCode) {
        return bookingRepository.findByBookingCode(bookingCode)
            .map(BookingResponse::fromEntity)
            .orElseThrow(() -> new ResourceNotFoundException("Booking dengan kode " + bookingCode + " tidak ditemukan."));
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAllByOrderByCreatedAtDesc().stream()
            .map(BookingResponse::fromEntity)
            .toList();
    }
}

