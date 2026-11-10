package com.cinema.booking.service;

import com.cinema.booking.dto.ComprehensiveBookingRequest;
import com.cinema.booking.dto.ComprehensiveBookingResponse;
import com.cinema.booking.exception.BusinessException;
import com.cinema.booking.exception.ResourceNotFoundException;
import com.cinema.booking.exception.SeatNotAvailableException;
import com.cinema.booking.model.*;
import com.cinema.booking.repository.*;
import com.cinema.booking.service.payment.PaymentResult;
import com.cinema.booking.service.payment.PaymentStrategy;
import com.cinema.booking.service.payment.PaymentStrategyFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class ComprehensiveBookingService {

    private final BookingRepository bookingRepository;
    private final ShowtimeRepository showtimeRepository;
    private final ShowtimeSeatRepository showtimeSeatRepository;
    private final MovieRepository movieRepository;
    private final CinemaRepository cinemaRepository;
    private final StudioRepository studioRepository;
    private final FoodItemRepository foodItemRepository;
    private final PaymentStrategyFactory paymentStrategyFactory;
    private final TicketNumberGenerator ticketNumberGenerator;

    public ComprehensiveBookingService(
        BookingRepository bookingRepository,
        ShowtimeRepository showtimeRepository,
        ShowtimeSeatRepository showtimeSeatRepository,
        MovieRepository movieRepository,
        CinemaRepository cinemaRepository,
        StudioRepository studioRepository,
        FoodItemRepository foodItemRepository,
        PaymentStrategyFactory paymentStrategyFactory,
        TicketNumberGenerator ticketNumberGenerator
    ) {
        this.bookingRepository = bookingRepository;
        this.showtimeRepository = showtimeRepository;
        this.showtimeSeatRepository = showtimeSeatRepository;
        this.movieRepository = movieRepository;
        this.cinemaRepository = cinemaRepository;
        this.studioRepository = studioRepository;
        this.foodItemRepository = foodItemRepository;
        this.paymentStrategyFactory = paymentStrategyFactory;
        this.ticketNumberGenerator = ticketNumberGenerator;
    }

    @Transactional
    public ComprehensiveBookingResponse createComprehensiveBooking(ComprehensiveBookingRequest request) {
        // 1. Ambil data jadwal tayang (Showtime)
        Showtime showtime = showtimeRepository.findById(request.showtimeId())
            .orElseThrow(() -> new ResourceNotFoundException("Jadwal tayang dengan ID " + request.showtimeId() + " tidak ditemukan."));

        Movie movie = movieRepository.findById(showtime.getMovieId()).orElse(null);
        Cinema cinema = cinemaRepository.findById(showtime.getCinemaId()).orElse(null);
        Studio studio = studioRepository.findById(showtime.getStudioId()).orElse(null);

        String movieTitle = (movie != null) ? movie.getTitle() : "Cinema Movie";
        String moviePoster = (movie != null) ? movie.getPosterUrl() : "";
        String cinemaName = (cinema != null) ? cinema.getName() : "Cinema Hall";
        String studioName = (studio != null) ? studio.getName() + " (" + showtime.getStudioType() + ")" : showtime.getStudioType();
        String showDateTime = showtime.getShowDate() + " • " + showtime.getStartTime();

        // 2. Kunci kursi per jadwal dengan Pessimistic Lock
        List<ShowtimeSeat> seats = showtimeSeatRepository.findByShowtimeIdAndSeatCodeInForUpdate(request.showtimeId(), request.seatCodes());
        if (seats.size() != request.seatCodes().size()) {
            throw new ResourceNotFoundException("Satu atau lebih kursi tidak valid untuk jadwal ini.");
        }

        // Cek ketersediaan
        for (ShowtimeSeat seat : seats) {
            if (seat.getStatus() == SeatStatus.BOOKED) {
                throw new SeatNotAvailableException(seat.getSeatCode());
            }
        }

        // 3. Hitung total tiket
        BigDecimal totalTicket = seats.stream()
            .map(ShowtimeSeat::getPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. Hitung pesanan makanan (F&B)
        BigDecimal totalFood = BigDecimal.ZERO;
        List<String> foodItemsList = new ArrayList<>();
        if (request.foodOrders() != null && !request.foodOrders().isEmpty()) {
            for (Map.Entry<Long, Integer> entry : request.foodOrders().entrySet()) {
                Long foodId = entry.getKey();
                Integer qty = entry.getValue();
                if (qty != null && qty > 0) {
                    FoodItem item = foodItemRepository.findById(foodId).orElse(null);
                    if (item != null) {
                        BigDecimal subtotal = item.getPrice().multiply(BigDecimal.valueOf(qty));
                        totalFood = totalFood.add(subtotal);
                        foodItemsList.add(item.getName() + " (x" + qty + " - Rp " + subtotal.toPlainString() + ")");
                    }
                }
            }
        }
        String foodSummary = foodItemsList.isEmpty() ? "Tidak ada pesanan makanan" : String.join(", ", foodItemsList);

        // 5. Grand Total (Tiket + Makanan)
        BigDecimal grandTotal = totalTicket.add(totalFood);

        // 6. Jalankan Strategi Pembayaran (Strategy Pattern)
        PaymentStrategy paymentStrategy = paymentStrategyFactory.getStrategy(request.paymentMethod());
        PaymentResult paymentResult = paymentStrategy.process(grandTotal, request.paymentAmount());

        // 7. Tandai kursi sebagai BOOKED (merah permanen)
        for (ShowtimeSeat seat : seats) {
            seat.book();
        }
        showtimeSeatRepository.saveAll(seats);

        // 8. Buat entitas Booking
        String bookingCode = ticketNumberGenerator.generateBookingCode();
        String seatSummary = String.join(", ", request.seatCodes());

        Booking booking = Booking.create(
            bookingCode,
            request.customerName().trim(),
            movieTitle,
            moviePoster,
            cinemaName,
            studioName,
            showDateTime,
            seatSummary,
            totalTicket,
            foodSummary,
            totalFood,
            grandTotal,
            List.of()
        );
        booking.confirmPayment(paymentResult);

        Booking saved = bookingRepository.save(booking);
        return ComprehensiveBookingResponse.fromEntity(saved);
    }

    @Transactional
    public ComprehensiveBookingResponse claimTicketAtKiosk(String claimCode) {
        Booking booking = bookingRepository.findAll().stream()
            .filter(b -> b.getClaimCode().equalsIgnoreCase(claimCode.trim()))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Tiket dengan PIN/Klaim " + claimCode + " tidak ditemukan."));

        booking.claimTicketAtKiosk();
        Booking updated = bookingRepository.save(booking);
        return ComprehensiveBookingResponse.fromEntity(updated);
    }

    @Transactional
    public ComprehensiveBookingResponse claimFoodAtCounter(String claimCode) {
        Booking booking = bookingRepository.findAll().stream()
            .filter(b -> b.getClaimCode().equalsIgnoreCase(claimCode.trim()))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Pesanan dengan PIN/Klaim " + claimCode + " tidak ditemukan."));

        booking.claimFoodAtCounter();
        Booking updated = bookingRepository.save(booking);
        return ComprehensiveBookingResponse.fromEntity(updated);
    }

    @Transactional(readOnly = true)
    public List<ComprehensiveBookingResponse> getAllActiveBookings() {
        return bookingRepository.findAllByOrderByCreatedAtDesc().stream()
            .map(ComprehensiveBookingResponse::fromEntity)
            .toList();
    }
}

