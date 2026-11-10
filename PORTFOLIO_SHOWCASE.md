# 🎬 CineMax TIX — Enterprise Cinema Ticketing & Concession Platform

> **Modern Re-engineering of Cinema Ticketing System:** Mentransformasikan legacy ticketing application menjadi platform pemesanan tiket bioskop modern berstandar enterprise yang terinspirasi dari aplikasi terkemuka (TIX ID / Cinema XXI / CGV) dengan arsitektur **Clean Code**, **Design Patterns**, sistem **High-Concurrency Seat Locking (TTL)**, serta **Self-Service Kiosk Simulator**.

---

## 📌 Executive Summary

**CineMax TIX** adalah platform fullstack pemesanan tiket bioskop dan *food & beverage* (F&B) yang dirancang untuk mengatasi tantangan transaksi berskala tinggi (*high-traffic ticket rush*). 

Proyek ini merupakan hasil *architectural re-engineering* dari sistem monolitik sederhana menjadi aplikasi modern berbasis **Spring Boot 4 (Java 25)** di sisi backend dan **Modular Vanilla JS & Tailwind CSS** di sisi frontend, menerapkan prinsip **SOLID**, **Clean Code Design Patterns (CCDP)**, serta pencegahan *race condition* dan *double-booking* menggunakan **Pessimistic Locking**.

- **Live Repository:** [GitHub - rzranz/TiketBioskop](https://github.com/rzranz/TiketBioskop)
- **Role:** Fullstack Software Engineer & System Architect
- **Tech Stack:** Java 25, Spring Boot 4.1.2, Spring Data JPA, Hibernate, H2 Database, Tailwind CSS, Modular ES6+ JavaScript, JUnit 5.

---

## 🚀 Key Features & Capabilities

### 1. Multi-City & Multi-Brand Cinema Discovery
- **Lokasi & Auto-Filter:** Filter bioskop dinamis berdasarkan kota (Bandung, Jakarta, Surabaya, Yogyakarta).
- **Brand Multi-Chain:** Mendukung rantai bioskop ternama (**CGV** dan **Cinema XXI**).
- **Interactive Schedule Accordion:** Tab bioskop menyediakan penelusuran jadwal real-time per bioskop lengkap dengan thumbnail film, rating usia (`13+`, `17+`), dan tombol jam tayang yang terhubung langsung ke pemilihan kursi.

### 2. Rich Movie Catalog & Showtimes Navigation
- **Katalog Film:** Film Sedang Tayang (*Now Showing*) dan Segera Hadir (*Coming Soon*) dengan metadata resmi TMDb & Wikimedia (rating IMDb, Rotten Tomatoes, sinopsis, durasi).
- **Direct Showtimes Grouping:** Pengelompokan jam tayang per tipe studio (*Regular 2D*, *Gold Class*, *Velvet 2D*) lengkap dengan nominal harga.

### 3. Visual Interactive Seat Map with Tiered Pricing
- **Denah Kursi Interaktif:** Representasi visual denah studio dengan lengkungan layar bioskop (*curved cinema screen*).
- **Tingkatan Kelas Kursi (Seat Tiers):**
  - **Satine Suite (Baris A):** Kursi premium eksklusif.
  - **Sweet Box (Baris B):** Kursi sofa berpasangan.
  - **Reguler (Baris C, D, E):** Kursi standar bioskop.

### 4. High-Concurrency Seat Locking with TTL Hold (Anti Double-Booking)
- **Status Kursi Real-Time:** 
  - 🟢 **Available (Hijau/Cyan):** Tersedia untuk dipilih.
  - 🟡 **Selected (Biru):** Kursi yang sedang Anda pilih.
  - ⚪ **Held (Abu-Abu):** Kursi yang sedang ditahan oleh pengguna lain yang sedang dalam proses pembayaran (*checkout*).
  - 🔴 **Booked (Merah):** Kursi yang telah resmi terbayar.
- **5-Minute Countdown Timer:** Menerapkan *Time-To-Live (TTL)* 300 detik. Jika pembeli tidak menyelesaikan pembayaran dalam 5 menit, kursi secara otomatis dibebaskan kembali (*auto-release*) ke publik.
- **Pessimistic Write Lock:** Mencegah dua pelanggan membeli kursi yang sama di waktu bersamaan secara atomik pada level database.

### 5. Integrated Concessions (F&B) Upselling
- Pemesanan makanan & minuman bioskop (Popcorn Caramel, Crispy Nachos, Coca-Cola, Hotdog, Combo Berdua) yang langsung terintegrasi ke dalam keranjang pembayaran tiket.

### 6. Strategy Pattern Payment Engine
- Arsitektur pembayaran modular mendukung metode:
  - **QRIS:** Validasi nominal presisi rupiah secara instan.
  - **Tunai (Cash):** Kalkulasi kembalian otomatis dengan penanganan validasi *insufficient payment*.

### 7. Self-Service Kiosk Thermal Printing Simulator
- **Digital E-Ticket:** Barcode 1D & QR Code dinamis dengan kode booking unik (contoh: `TIX-20261110-880B`) dan kode klaim rahasia (*claim code*).
- **Simulasi Kiosk Fisik:** Halaman simulator mesin cetak tiket mandiri di lobi bioskop dengan animasi *thermal print feed-out* dan simulasi pengambilan makanan di konter F&B.

### 8. Backoffice Admin CMS
- Panel manajemen terpisah untuk pengelola bioskop:
  - CRUD Film (poster, sinopsis, status tayang).
  - Manajemen Bioskop & Studio.
  - Penjadwalan Jam Tayang (*Showtimes*) baru beserta inisialisasi 34 kursi otomatis.
  - Rekapitulasi & Laporan Penjualan Transaksi.

---

## 🛠️ Software Architecture & Design Patterns

```
┌─────────────────────────────────────────────────────────────┐
│                       PRESENTATION                          │
│     index.html (Semantic UI) + Tailwind CSS (Responsive)    │
│  Modular Components: auth, movies, cinemas, booking, tickets │
└──────────────────────────────┬──────────────────────────────┘
                               │ REST API (JSON)
┌──────────────────────────────▼──────────────────────────────┐
│                    SPRING BOOT BACKEND                      │
│                                                             │
│  [ Controllers ] ──> DTOs & Bean Validation (@Valid)        │
│          │                                                  │
│  [ Services ]    ──> ComprehensiveBookingService            │
│          │           ├── Pessimistic Locking (Seat Hold)    │
│          │           └── Concurrency Control (Version/TTL)  │
│          │                                                  │
│  [ Patterns ]    ──> PaymentStrategyFactory                 │
│                      ├── QrisPaymentStrategy                │
│                      └── CashPaymentStrategy                │
│          │                                                  │
│  [ Repositories ]──> Spring Data JPA / Hibernate            │
└──────────────────────────────┬──────────────────────────────┘
                               │ JDBC
┌──────────────────────────────▼──────────────────────────────┐
│                      H2 IN-MEMORY DB                        │
│  Tables: movies, cinemas, studios, showtimes, seats,        │
│          showtime_seats, food_items, bookings, users        │
└─────────────────────────────────────────────────────────────┘
```

### Clean Code & Design Patterns Highlights:
1. **Strategy Pattern:** Memisahkan algoritma kalkulasi dan validasi pembayaran ke dalam `PaymentStrategy`, mempermudah penambahan gateway pembayaran baru (GoPay, Virtual Account) tanpa memodifikasi kode inti (*Open/Closed Principle*).
2. **Pessimistic Locking (`PESSIMISTIC_WRITE`):** Menjamin integritas data inventori kursi saat ribuan transaksi terjadi bersamaan.
3. **Single Responsibility Principle (SRP):** File frontend monolitik (>2.100 baris) dipecah menjadi modul independen berukuran `<300 baris` per file (`api.js`, `booking.js`, `cinemas.js`, `movies.js`, `tickets.js`, `admin.js`, `auth.js`).
4. **Standardized API Envelope:** Semua respon RESTful dibungkus seragam dalam format `ApiResponse<T>` dengan status keberhasilan, pesan, timestamp, dan payload data.
5. **Fail-Fast Validation & Global Exception Handling:** Menggunakan `@ControllerAdvice` dengan penanganan kustom `BusinessException`, `SeatNotAvailableException`, dan validasi DTO record.

---

## 📊 Engineering Impact & QA Metrics

- **Unit & Integration Tests:** 100% Pass Rate (`10 tests run, 0 failures, 0 errors, 0 skipped` via JUnit 5).
- **DOM & Event Handler QA Audit:** 0 broken DOM bindings, 0 missing element ID dari 69 event handler interaktif.
- **Concurrency Safety:** Teruji mencegah konflik multi-pembelian melalui simulasi holding kursi 5 menit.
- **Performance:** Waktu respon API rata-rata `<100ms` dengan memori footprint yang sangat ringan.

---

## 💻 Cara Menjalankan Aplikasi Secara Lokal

### Prasyarat
- Java Development Kit (JDK) 17+ (Direkomendasikan Java 21 atau Java 25)
- Git & Web Browser modern

### Langkah Instalasi
```bash
# 1. Clone repository
git clone https://github.com/rzranz/TiketBioskop.git
cd TiketBioskop/cinema-backend

# 2. Jalankan test suite
./mvnw clean test

# 3. Jalankan aplikasi Spring Boot
./mvnw spring-boot:run
```

Buka browser dan akses:
- **Aplikasi Web:** `http://localhost:8080`
- **H2 Database Console:** `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:cinemadb`, User: `sa`, Password: *kosong*)
- **Akun Admin Demo:** `admin@cinemax.id` / `admin123`
