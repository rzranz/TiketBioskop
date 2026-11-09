package com.cinema.booking.dto;

import com.cinema.booking.model.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO Request Pemesanan Tiket dari Klien.
 * Dilengkapi validasi deklaratif Jakarta Validation dan opsi metode pembayaran (CASH/QRIS).
 */
public record CreateBookingRequest(
    @NotBlank(message = "Nama pembeli wajib diisi")
    @Size(min = 2, max = 100, message = "Nama pembeli harus antara 2 hingga 100 karakter")
    String customerName,

    @NotEmpty(message = "Paling sedikit satu kursi harus dipilih")
    @Size(max = 10, message = "Maksimal pemesanan adalah 10 tiket per transaksi")
    List<@NotBlank(message = "Kode kursi tidak boleh kosong") String> seatCodes,

    @NotNull(message = "Jumlah pembayaran wajib dicantumkan")
    @DecimalMin(value = "0.00", inclusive = false, message = "Nominal pembayaran harus lebih dari 0")
    BigDecimal paymentAmount,

    PaymentMethod paymentMethod
) {
    public CreateBookingRequest(String customerName, List<String> seatCodes, BigDecimal paymentAmount) {
        this(customerName, seatCodes, paymentAmount, PaymentMethod.CASH);
    }
}
