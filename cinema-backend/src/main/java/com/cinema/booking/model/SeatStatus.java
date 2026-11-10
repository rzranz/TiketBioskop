package com.cinema.booking.model;

/**
 * Status Ketersediaan Kursi pada Jadwal Tertentu.
 * AVAILABLE: Kursi kosong dan dapat dipilih.
 * HELD: Kursi sedang dalam proses pembayaran (tampil abu-abu), akan kembali AVAILABLE jika batas waktu bayar habis.
 * BOOKED: Kursi sudah lunas dibayar (tampil merah/terkunci).
 */
public enum SeatStatus {
    AVAILABLE,
    HELD,
    BOOKED
}

