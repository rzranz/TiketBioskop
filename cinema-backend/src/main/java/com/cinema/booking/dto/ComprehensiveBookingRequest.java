package com.cinema.booking.dto;

import com.cinema.booking.model.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTO Request Pemesanan Tiket Bioskop + Makanan (F&B) Komprehensif.
 */
public record ComprehensiveBookingRequest(
    @NotNull(message = "ID jadwal tayang (showtime) wajib dicantumkan")
    Long showtimeId,

    @NotBlank(message = "Nama pembeli wajib diisi")
    String customerName,

    String customerEmail,

    @NotEmpty(message = "Paling sedikit 1 kursi harus dipilih")
    List<String> seatCodes,

    Map<Long, Integer> foodOrders, // foodItemId -> quantity

    PaymentMethod paymentMethod,

    @NotNull(message = "Jumlah pembayaran wajib diisi")
    @DecimalMin(value = "0.00", inclusive = false, message = "Nominal pembayaran harus lebih dari 0")
    BigDecimal paymentAmount
) {}

