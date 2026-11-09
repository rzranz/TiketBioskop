# 🎬 Cinema Booking Platform — Reengineering Showcase

Proyek ini merupakan portofolio **Reengineering & Modernisasi Perangkat Lunak** dari sistem penjualan tiket bioskop berbasis desktop legacy (**Java Swing / NetBeans**) menjadi **Modern Backend REST API berbasis Java Spring Boot Modular Monolith** sesuai standar *clean architecture*, *design patterns*, keamanan transaksi, dan pencegahan *double-booking*.

---

## 🏛️ Arsitektur: Before vs After

```
[ BEFORE: LEGACY SYSTEM ]
+-----------------------------------------------------------+
| PenjualanTiketBioskop (NetBeans Matisse Swing GUI)        |
| - Semua logika (UI, hitung harga, struk) di 1 file        |
| - Data disimpan di variabel memori lokal (volatile)       |
| - Tidak ada database, tidak ada API, single desktop user   |
+-----------------------------------------------------------+
                             ⬇
                [ REENGINEERING PROCESS ]
                             ⬇
[ AFTER: MODERN BACKEND SYSTEM ]
+-----------------------------------------------------------+
| cinema-backend (Spring Boot Modular Monolith + REST API)  |
|                                                           |
|  [ Klien: Web / Mobile / Kiosk ]                          |
|                 │ (HTTP JSON Requests)                    |
|                 ▼                                         |
|  1. Controller Layer: REST API & Jakarta Validation       |
|  2. Service Layer: Logika Bisnis & Transaksi ACID         |
|  3. Strategy Pattern: Cash & QRIS Payment Processors      |
|  4. Repository Layer: Spring Data JPA + Database Locking  |
|  5. Database: Pessimistic Write Lock (`FOR UPDATE`)       |
+-----------------------------------------------------------+
```

---

## 💎 Penerapan Clean Code & Design Patterns

Sesuai dengan panduan arsitektur di [Java_SpringBoot_Best_Practices.md](.agents/skills/java-best-practice/Java_SpringBoot_Best_Practices.md):

1. **Strategy Pattern (Payment Processing):**
   * Antarmuka `PaymentStrategy` memisahkan variasi cara pembayaran.
   * `CashPaymentStrategy`: menangani pembayaran uang tunai loket beserta perhitungan uang kembalian.
   * `QrisPaymentStrategy`: menangani pembayaran digital berbasis QRIS dengan verifikasi nominal presisi.
   * Memenuhi *Open-Closed Principle* (OCP): metode pembayaran baru (misal Credit Card, Virtual Account) dapat ditambahkan tanpa memodifikasi kode pemesanan yang ada.
2. **Factory & Registry Pattern (`PaymentStrategyFactory`):**
   * Menggunakan Dependency Injection Spring untuk mendaftarkan dan memilih strategi pembayaran secara dinamis saat runtime.
3. **Explicit State Transition Pattern (Domain-Driven Invariant):**
   * Menghindari mutasi status liar via setter (`setStatus(...)`).
   * Menggunakan metode transisi domain eksplisit di `Booking`:
     * `booking.confirmPayment(paymentResult)`: Memvalidasi bahwa hanya booking berstatus `PENDING` yang bisa dikonfirmasi.
     * `booking.cancel(reason)`: Membatalkan booking dan secara otomatis membebaskan kembali kursi yang terisi ke inventaris.
4. **Pencegahan Double-Booking (Database Concurrency Locking):**
   * Menggunakan **Pessimistic Write Lock (`FOR UPDATE`)** di `SeatRepository.findAllBySeatCodeInForUpdate()` untuk mengeliminasi *race condition* saat dua pelanggan memilih kursi yang sama bersamaan.
   * Dilengkapi kolom `@Version` untuk *Optimistic Locking* sebagai pengamanan lapis kedua.
5. **Representasi Finansial yang Tepat:**
   * Menggunakan `BigDecimal` untuk seluruh mata uang dan perhitungan matematika harga (menghindari floating-point rounding error).
6. **Immutability pada API Boundary:**
   * Seluruh DTO request/response (`SeatResponse`, `BookingResponse`, `SeatTierResponse`, `ApiResponse`) dibangun menggunakan **Java Record**.
7. **Single Responsibility (SRP):**
   * Format kode tiket dipisahkan ke komponen khusus `TicketNumberGenerator`.
   * Penanganan error dipusatkan di `GlobalExceptionHandler` (`@RestControllerAdvice`).

---

## 📂 Struktur Direktori

```
TiketBioskop/
├── legacy/                               # Arsip kode aplikasi desktop lama (Java Swing NetBeans)
│   └── PenjualanTiketBioskop/
├── cinema-backend/                       # Modern Spring Boot Backend (Maven)
│   ├── src/main/java/com/cinema/booking/
│   │   ├── config/                       # Data seeder studio & layout kursi (A1-E8)
│   │   ├── controller/                   # Endpoint REST API (/api/seats, /api/bookings)
│   │   ├── dto/                          # Java Record DTO & Envelope Response
│   │   ├── exception/                    # Global Exception Handler & Business Exceptions
│   │   ├── model/                        # JPA Entity (Seat, Booking, Enum SeatTier, Status)
│   │   ├── repository/                   # Spring Data JPA dengan Pessimistic Lock
│   │   └── service/                      # Logika transaksi & generator tiket
│   │       └── payment/                  # Strategy Pattern untuk Pembayaran (Cash, QRIS)
│   └── src/test/java/com/cinema/booking/ # Suite Automated Test (Unit & Integration)
├── Cinema_Booking_Reengineering_PRD.md   # Dokumen Kebutuhan Produk (PRD) & Desain Solusi
├── .agents/                              # Panduan Standar Rekayasa Java & Spring Boot
└── README.md                             # Dokumentasi portofolio
```

---

## 🛠️ Cara Menjalankan Aplikasi

### Prasyarat
* Java JDK 21 atau lebih baru (telah diuji dengan OpenJDK 25 LTS).
* Tidak perlu install Maven secara manual (menggunakan Maven Wrapper bawaan).

### 1. Menjalankan Server Backend
Buka terminal / PowerShell di direktori proyek:
```powershell
cd cinema-backend
.\mvnw.cmd spring-boot:run
```
Server akan aktif di `http://localhost:8080`.

### 2. Mengakses H2 Database Web Console
Buka browser ke `http://localhost:8080/h2-console`:
* **JDBC URL:** `jdbc:h2:mem:cinemadb`
* **User Name:** `sa`
* **Password:** *(kosongkan)*

### 3. Menjalankan Automated Test
```powershell
cd cinema-backend
.\mvnw.cmd test
```

---

## 📡 Dokumentasi Endpoint REST API

### 1. Kursi Bioskop (`/api/seats`)
* `GET /api/seats` — Mendapatkan daftar seluruh denah kursi studio (A1 s/d E8).
* `GET /api/seats/available` — Mendapatkan daftar kursi yang saat ini masih kosong/tersedia.
* `GET /api/seats/tiers` — Mendapatkan kategori kursi dan harga dasarnya (Reguler, Sweet Box, Satine Suite).

### 2. Pemesanan & Pembatalan Tiket (`/api/bookings`)
* `POST /api/bookings` — Melakukan reservasi dan pembayaran tiket bioskop (bisa Cash atau QRIS).
  * **Payload Request (Cash):**
    ```json
    {
      "customerName": "Budi Santoso",
      "seatCodes": ["C1", "C2"],
      "paymentAmount": 100000.00,
      "paymentMethod": "CASH"
    }
    ```
  * **Payload Request (QRIS):**
    ```json
    {
      "customerName": "Siti Rahma",
      "seatCodes": ["B1"],
      "paymentAmount": 75000.00,
      "paymentMethod": "QRIS"
    }
    ```
* `GET /api/bookings/{bookingCode}` — Melihat detail tiket berdasarkan kode pemesanan.
* `POST /api/bookings/{bookingCode}/cancel` — Membatalkan pemesanan dan mengembalikan kursi ke status tersedia.
* `GET /api/bookings` — Melihat seluruh riwayat pemesanan tiket di sistem.
