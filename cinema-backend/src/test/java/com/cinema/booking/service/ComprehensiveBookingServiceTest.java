package com.cinema.booking.service;

import com.cinema.booking.dto.ComprehensiveBookingRequest;
import com.cinema.booking.dto.ComprehensiveBookingResponse;
import com.cinema.booking.exception.BusinessException;
import com.cinema.booking.model.BookingStatus;
import com.cinema.booking.model.PaymentMethod;
import com.cinema.booking.model.SeatStatus;
import com.cinema.booking.model.ShowtimeSeat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ComprehensiveBookingServiceTest {

    @Autowired
    private ComprehensiveBookingService comprehensiveBookingService;

    @Autowired
    private ShowtimeSeatService showtimeSeatService;

    @Test
    @DisplayName("Seat Hold Pattern: Kursi harus berhasil dikunci sementara (Status HELD / Abu-Abu) selama 5 menit")
    void shouldSuccessfullyHoldSeatsWithGreyStatus() {
        Long showtimeId = 1L; // Jadwal tayang pertama
        List<String> seatsToHold = List.of("C1", "C2");

        Map<String, Object> holdResult = showtimeSeatService.holdSeats(showtimeId, seatsToHold, "Randi", 5);

        assertNotNull(holdResult);
        assertEquals("HELD", holdResult.get("status"));
        assertEquals(seatsToHold, holdResult.get("heldSeats"));

        // Verifikasi kursi di database berubah status menjadi HELD
        List<ShowtimeSeat> seats = showtimeSeatService.getSeatsForShowtime(showtimeId);
        ShowtimeSeat c1 = seats.stream().filter(s -> s.getSeatCode().equals("C1")).findFirst().orElseThrow();
        assertEquals(SeatStatus.HELD, c1.getStatus());
        assertEquals("Randi", c1.getHeldBy());
    }

    @Test
    @DisplayName("Full Checkout: Tiket + F&B Popcorn + Pembayaran QRIS + Penerbitan PIN Klaim Kiosk")
    void shouldCompleteFullOrderWithTicketsAndFoodAndGenerateClaimPin() {
        Long showtimeId = 1L;

        // Pesan kursi C3 (Rp 45.000) + 1 Popcorn Caramel (FoodId 1 = Rp 55.000) = Total Rp 100.000
        ComprehensiveBookingRequest request = new ComprehensiveBookingRequest(
            showtimeId,
            "Randi Pratama",
            "randi@tixid.example.com",
            List.of("C3"),
            Map.of(1L, 1),
            PaymentMethod.QRIS,
            new BigDecimal("100000.00")
        );

        ComprehensiveBookingResponse response = comprehensiveBookingService.createComprehensiveBooking(request);

        assertNotNull(response);
        assertNotNull(response.bookingCode());
        assertNotNull(response.claimCode());
        assertTrue(response.claimCode().startsWith("CLM-"), "Kode klaim harus memiliki format PIN 'CLM-XXXXXX'");
        assertEquals("Randi Pratama", response.customerName());
        assertEquals(new BigDecimal("100000.00"), response.grandTotal());
        assertEquals(PaymentMethod.QRIS, response.paymentMethod());
        assertEquals(BookingStatus.CONFIRMED, response.status());
        assertFalse(response.ticketClaimed(), "Tiket baru belum boleh berstatus tercetak");
    }

    @Test
    @DisplayName("Kiosk Simulator: Klaim tiket fisik di mesin kiosk mandiri")
    void shouldClaimTicketAtKioskSuccessfully() {
        Long showtimeId = 1L;
        ComprehensiveBookingRequest request = new ComprehensiveBookingRequest(
            showtimeId,
            "Budi",
            "budi@example.com",
            List.of("D1"),
            Map.of(),
            PaymentMethod.CASH,
            new BigDecimal("50000.00")
        );
        ComprehensiveBookingResponse booked = comprehensiveBookingService.createComprehensiveBooking(request);

        // Simulasi cetak di Kiosk menggunakan PIN Klaim
        ComprehensiveBookingResponse claimed = comprehensiveBookingService.claimTicketAtKiosk(booked.claimCode());
        assertTrue(claimed.ticketClaimed(), "Status tiket harus berubah menjadi ticketClaimed = true");

        // Klaim ulang kedua kali harus ditolak
        assertThrows(
            BusinessException.class,
            () -> comprehensiveBookingService.claimTicketAtKiosk(booked.claimCode())
        );
    }
}

