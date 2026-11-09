package com.cinema.booking.service;

import com.cinema.booking.dto.BookingResponse;
import com.cinema.booking.dto.CreateBookingRequest;
import com.cinema.booking.exception.ResourceNotFoundException;
import com.cinema.booking.exception.SeatNotAvailableException;
import com.cinema.booking.model.Booking;
import com.cinema.booking.model.Seat;
import com.cinema.booking.repository.BookingRepository;
import com.cinema.booking.repository.SeatRepository;
import com.cinema.booking.service.payment.PaymentResult;
import com.cinema.booking.service.payment.PaymentStrategy;
import com.cinema.booking.service.payment.PaymentStrategyFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service Logika Bisnis Pemesanan Tiket Bioskop.
 * Menerapkan Clean Code:
 * 1. Single Responsibility Principle (pembayaran didelegasikan ke Strategy Pattern).
 * 2. Dependency Injection melalui constructor.
 * 3. Atomic Database Transactions (@Transactional).
 * 4. Pessimistic Write Lock untuk mencegah Double-Booking.
 * 5. Explicit Domain State Transitions pada entitas Booking.
 */
@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final SeatRepository seatRepository;
    private final PaymentStrategyFactory paymentStrategyFactory;
    private final TicketNumberGenerator ticketNumberGenerator;

    public BookingService(
        BookingRepository bookingRepository,
        SeatRepository seatRepository,
        PaymentStrategyFactory paymentStrategyFactory,
        TicketNumberGenerator ticketNumberGenerator
    ) {
        this.bookingRepository = bookingRepository;
        this.seatRepository = seatRepository;
        this.paymentStrategyFactory = paymentStrategyFactory;
        this.ticketNumberGenerator = ticketNumberGenerator;
    }

    /**
     * Membuat reservasi dan konfirmasi pemesanan tiket.
     */
    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {
        // 1. Ambil kursi dengan Pessimistic Write Lock (mencegah race condition)
        List<Seat> seats = seatRepository.findAllBySeatCodeInForUpdate(request.seatCodes());

        // Validasi keberadaan seluruh kursi yang dipilih
        if (seats.size() != request.seatCodes().size()) {
            throw new ResourceNotFoundException("Satu atau lebih kursi yang diminta tidak ditemukan di sistem.");
        }

        // 2. Validasi ketersediaan kursi
        for (Seat seat : seats) {
            if (!seat.isAvailable()) {
                throw new SeatNotAvailableException(seat.getSeatCode());
            }
        }

        // 3. Hitung total biaya (BigDecimal)
        BigDecimal totalAmount = seats.stream()
            .map(Seat::getPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. Eksekusi Strategi Pembayaran (Strategy Pattern)
        PaymentStrategy paymentStrategy = paymentStrategyFactory.getStrategy(request.paymentMethod());
        PaymentResult paymentResult = paymentStrategy.process(totalAmount, request.paymentAmount());

        // 5. Ubah status ketersediaan kursi menjadi terisi
        for (Seat seat : seats) {
            seat.setAvailable(false);
        }
        seatRepository.saveAll(seats);

        // 6. Buat entitas Booking menggunakan Factory Method & State Transition
        String bookingCode = ticketNumberGenerator.generateBookingCode();
        Booking booking = Booking.create(bookingCode, request.customerName().trim(), totalAmount, seats);
        booking.confirmPayment(paymentResult);

        Booking savedBooking = bookingRepository.save(booking);

        return BookingResponse.fromEntity(savedBooking);
    }

    /**
     * Membatalkan pemesanan dan mengembalikan kursi ke status tersedia.
     */
    @Transactional
    public BookingResponse cancelBooking(String bookingCode, String reason) {
        Booking booking = bookingRepository.findByBookingCode(bookingCode)
            .orElseThrow(() -> new ResourceNotFoundException("Booking dengan kode " + bookingCode + " tidak ditemukan."));

        booking.cancel(reason != null ? reason : "Dibatalkan oleh pengguna");
        seatRepository.saveAll(booking.getSeats());

        Booking updated = bookingRepository.save(booking);
        return BookingResponse.fromEntity(updated);
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
