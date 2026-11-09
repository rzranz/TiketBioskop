package com.cinema.booking.service;

import com.cinema.booking.dto.BookingResponse;
import com.cinema.booking.dto.CreateBookingRequest;
import com.cinema.booking.exception.InsufficientPaymentException;
import com.cinema.booking.exception.SeatNotAvailableException;
import com.cinema.booking.model.BookingStatus;
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
    @DisplayName("Harus berhasil memesan kursi jika status tersedia dan pembayaran cukup")
    void shouldSuccessfullyBookSeats() {
        // C1 (Reguler Rp 45.000) + C2 (Reguler Rp 45.000) = Rp 90.000
        CreateBookingRequest request = new CreateBookingRequest(
            "Budi Santoso",
            List.of("C1", "C2"),
            new BigDecimal("100000.00")
        );

        BookingResponse response = bookingService.createBooking(request);

        assertNotNull(response);
        assertNotNull(response.bookingCode());
        assertEquals("Budi Santoso", response.customerName());
        assertEquals(2, response.ticketCount());
        assertEquals(new BigDecimal("90000.00"), response.totalAmount());
        assertEquals(new BigDecimal("100000.00"), response.paymentAmount());
        assertEquals(new BigDecimal("10000.00"), response.changeAmount());
        assertEquals(BookingStatus.CONFIRMED, response.status());
    }

    @Test
    @DisplayName("Harus menolak pemesanan ganda (Double-Booking Prevention)")
    void shouldRejectDoubleBooking() {
        // Booking pertama untuk kursi D1
        CreateBookingRequest firstRequest = new CreateBookingRequest(
            "Rian",
            List.of("D1"),
            new BigDecimal("50000.00")
        );
        bookingService.createBooking(firstRequest);

        // Booking kedua mencoba memesan kursi D1 lagi
        CreateBookingRequest secondRequest = new CreateBookingRequest(
            "Dewi",
            List.of("D1"),
            new BigDecimal("50000.00")
        );

        SeatNotAvailableException exception = assertThrows(
            SeatNotAvailableException.class,
            () -> bookingService.createBooking(secondRequest)
        );

        assertTrue(exception.getMessage().contains("D1"));
    }

    @Test
    @DisplayName("Harus menolak pemesanan jika uang pembayaran kurang")
    void shouldRejectInsufficientPayment() {
        // A1 (Satine Suite Rp 120.000), tapi hanya bayar Rp 50.000
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
}

