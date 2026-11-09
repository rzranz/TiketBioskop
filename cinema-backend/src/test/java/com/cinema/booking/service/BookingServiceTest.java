package com.cinema.booking.service;

import com.cinema.booking.dto.BookingResponse;
import com.cinema.booking.dto.CreateBookingRequest;
import com.cinema.booking.dto.SeatResponse;
import com.cinema.booking.exception.BusinessException;
import com.cinema.booking.exception.InsufficientPaymentException;
import com.cinema.booking.exception.SeatNotAvailableException;
import com.cinema.booking.model.BookingStatus;
import com.cinema.booking.model.PaymentMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class BookingServiceTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private SeatService seatService;

    @Test
    @DisplayName("Strategy Pattern: Pembayaran Cash harus berhasil menghitung total dan uang kembalian")
    void shouldSuccessfullyBookSeatsWithCash() {
        // C1 (Reguler Rp 45.000) + C2 (Reguler Rp 45.000) = Rp 90.000
        CreateBookingRequest request = new CreateBookingRequest(
            "Budi Santoso",
            List.of("C1", "C2"),
            new BigDecimal("100000.00"),
            PaymentMethod.CASH
        );

        BookingResponse response = bookingService.createBooking(request);

        assertNotNull(response);
        assertNotNull(response.bookingCode());
        assertEquals("Budi Santoso", response.customerName());
        assertEquals(2, response.ticketCount());
        assertEquals(new BigDecimal("90000.00"), response.totalAmount());
        assertEquals(new BigDecimal("100000.00"), response.paymentAmount());
        assertEquals(new BigDecimal("10000.00"), response.changeAmount());
        assertEquals(PaymentMethod.CASH, response.paymentMethod());
        assertEquals(BookingStatus.CONFIRMED, response.status());
    }

    @Test
    @DisplayName("Strategy Pattern: Pembayaran QRIS harus berhasil jika nominal presisi")
    void shouldSuccessfullyBookSeatsWithQris() {
        // B1 (Sweet Box Rp 75.000)
        CreateBookingRequest request = new CreateBookingRequest(
            "Siti Rahma",
            List.of("B1"),
            new BigDecimal("75000.00"),
            PaymentMethod.QRIS
        );

        BookingResponse response = bookingService.createBooking(request);

        assertNotNull(response);
        assertEquals(PaymentMethod.QRIS, response.paymentMethod());
        assertEquals(BigDecimal.ZERO, response.changeAmount());
        assertEquals(BookingStatus.CONFIRMED, response.status());
    }

    @Test
    @DisplayName("Strategy Pattern: Pembayaran QRIS harus ditolak jika nominal tidak pas")
    void shouldRejectQrisWhenAmountMismatches() {
        // B2 (Sweet Box Rp 75.000), dibayar Rp 80.000 via QRIS (harus ditolak)
        CreateBookingRequest request = new CreateBookingRequest(
            "Siti Rahma",
            List.of("B2"),
            new BigDecimal("80000.00"),
            PaymentMethod.QRIS
        );

        assertThrows(
            BusinessException.class,
            () -> bookingService.createBooking(request)
        );
    }

    @Test
    @DisplayName("Database Lock Invariant: Menolak pemesanan ganda (Zero Double-Booking)")
    void shouldRejectDoubleBooking() {
        CreateBookingRequest first = new CreateBookingRequest(
            "Rian",
            List.of("D1"),
            new BigDecimal("50000.00")
        );
        bookingService.createBooking(first);

        CreateBookingRequest second = new CreateBookingRequest(
            "Dewi",
            List.of("D1"),
            new BigDecimal("50000.00")
        );

        SeatNotAvailableException exception = assertThrows(
            SeatNotAvailableException.class,
            () -> bookingService.createBooking(second)
        );

        assertTrue(exception.getMessage().contains("D1"));
    }

    @Test
    @DisplayName("Validasi Finansial: Menolak jika uang cash kurang")
    void shouldRejectInsufficientPayment() {
        CreateBookingRequest request = new CreateBookingRequest(
            "Andi",
            List.of("A1"),
            new BigDecimal("50000.00")
        );

        assertThrows(
            InsufficientPaymentException.class,
            () -> bookingService.createBooking(request)
        );
    }

    @Test
    @DisplayName("State Transition Pattern: Pembatalan pemesanan harus membebaskan kursi kembali")
    void shouldCancelBookingAndReleaseSeats() {
        CreateBookingRequest request = new CreateBookingRequest(
            "Joko",
            List.of("E1"),
            new BigDecimal("50000.00")
        );
        BookingResponse created = bookingService.createBooking(request);

        // Batalkan booking
        BookingResponse cancelled = bookingService.cancelBooking(created.bookingCode(), "Customer reschedule");
        assertEquals(BookingStatus.CANCELLED, cancelled.status());

        // Verifikasi kursi E1 sudah tersedia kembali di sistem
        List<SeatResponse> availableSeats = seatService.getAvailableSeats();
        boolean isE1Available = availableSeats.stream().anyMatch(s -> s.seatCode().equals("E1"));
        assertTrue(isE1Available, "Kursi E1 harus kembali berstatus tersedia setelah pembatalan.");
    }
}
