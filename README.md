# 🎬 Cinema Booking Platform — Reengineering Showcase

Proyek ini merupakan portofolio **Reengineering & Modernisasi Perangkat Lunak** dari sistem penjualan tiket bioskop berbasis desktop legacy (**Java Swing / NetBeans**) menjadi **Modern Backend REST API berbasis Java Spring Boot Modular Monolith** sesuai standar *clean architecture*, keamanan transaksi, dan pencegahan *double-booking*.

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
|  2. Service Layer: Logika Transaksi Finansial             |
|  3. Repository Layer: Spring Data JPA + Locking           |
|  4. Database: Pessimistic Write Lock (`FOR UPDATE`)       |
+-----------------------------------------------------------+
```

---

## 🚀 Fitur Utama Backend Baru

1. **Pencegahan Double-Booking (Zero Race Condition):**
   * Menggunakan **Pessimistic Write Locking (`FOR UPDATE`)** di level database saat proses pemilihan kursi, menjamin dua pembeli tidak dapat memesan kursi yang sama secara bersamaan.
2. **Perhitungan Finansial Presisi:**
   * Menggunakan `BigDecimal` untuk seluruh nominal uang tiket, pembayaran, dan kembalian (menghindari floating-point rounding error).
3. **Data Kursi Terstruktur:**
   * **Satine Suite (Baris A):** VIP Recliner (Rp 120.000)
   * **Sweet Box (Baris B):** Couple Sofa (Rp 75.000)
   * **Reguler (Baris C, D, E):** Standard Seats (Rp 45.000)
4. **Validasi & Error Handling Terpusat:**
   * Dilengkapi `@RestControllerAdvice` global exception handler yang mengembalikan format JSON rapi.
5. **Automated Testing:**
   * Pengujian integrasi otomatis (JUnit 5 + Spring Boot Test) memvalidasi skenario pemesanan, pencegahan double booking, dan validasi pembayaran.

---

## 📂 Struktur Direktori

```
TiketBioskop/
├── legacy/                               # Arsip kode aplikasi desktop lama (Java Swing NetBeans)
│   └── PenjualanTiketBioskop/
├── cinema-backend/                       # Modern Spring Boot Backend (Maven)
│   ├── src/main/java/com/cinema/booking/
│   │   ├── config/                       # Data seeder studio & layout kursi
│   │   ├── controller/                   # Endpoint REST API (/api/seats, /api/bookings)
│   │   ├── dto/                          # Java Record DTO & Envelope Response
│   │   ├── exception/                    # Global Exception Handler & Business Exceptions
│   │   ├── model/                        # JPA Entity (Seat, Booking, Enum SeatTier, Status)
│   │   ├── repository/                   # Spring Data JPA dengan Pessimistic Lock
│   │   └── service/                      # Logika transaksi pemesanan tiket
│   └── src/test/java/com/cinema/booking/ # Suite Automated Test
├── Cinema_Booking_Reengineering_PRD.md   # Dokumen Kebutuhan Produk (PRD) & Desain Solusi
├── .agents/                              # Panduan Standar Rekayasa Java & Spring Boot
└── README.md                             # Dokumentasi proyek
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
Server akan berjalan di `http://localhost:8080`.

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
* `GET /api/seats/tiers` — Mendapatkan kategori kursi dan harga dasarnya.

### 2. Pemesanan Tiket (`/api/bookings`)
* `POST /api/bookings` — Melakukan reservasi dan pembayaran tiket bioskop.
  * **Payload Request:**
    ```json
    {
      "customerName": "Budi Santoso",
      "seatCodes": ["C1", "C2"],
      "paymentAmount": 100000.00
    }
    ```
* `GET /api/bookings/{bookingCode}` — Melihat detail tiket berdasarkan kode pemesanan.
* `GET /api/bookings` — Melihat seluruh riwayat pemesanan tiket di sistem.
