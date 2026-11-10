# 🎬 CineMax TIX — Modern Cinema Ticketing Platform & CMS Backoffice
> **Enterprise Software Reengineering & Clean Code Architecture Showcase**  
> Transformasi menyeluruh dari aplikasi desktop warisan (*legacy*) **Java Swing / NetBeans** menjadi **Modern Spring Boot Modular Monolith** dengan arsitektur frontend web responsif, pencegahan *race-condition double-booking*, *dynamic seat hold*, *concession F&B*, tiket digital, simulator *self-ticketing kiosk*, dan *admin backoffice CMS*.

---

## 🏛️ Arsitektur Rekayasa: Legacy vs Modern

```text
[ SEBELUM: SISTEM DESKTOP LEGACY ]
┌─────────────────────────────────────────────────────────────┐
│ PenjualanTiketBioskop (NetBeans Matisse Swing GUI Desktop)  │
│ • Monolitik kaku: UI, logika hitung harga, & struk di 1 file│
│ • Data disimpan di variabel memori lokal (volatile / hilang)│
│ • Tidak ada basis data, tidak ada REST API, single desktop  │
│ • Rawan duplikasi tiket dan tidak memiliki manajemen jadwal │
└─────────────────────────────────────────────────────────────┘
                              ⬇
                 [ REENGINEERING PROCESS ]
                              ⬇
[ SESUDAH: MODERN ENTERPRISE PLATFORM ]
┌─────────────────────────────────────────────────────────────┐
│ 1. Klien Pengguna & Admin (Clean Modular Frontend Architecture)│
│    ├── Mobile-First Ticketing App (TIX ID / CGV inspired)   │
│    ├── Admin Backoffice CMS (Film, Bioskop, Studio, Jadwal) │
│    └── Kiosk Ticket Printer Simulator (Animasi Struk Termal)│
│                                                             │
│                 │ (HTTP JSON REST Calls)                    │
│                 ▼                                           │
│ 2. Backend Engine (Spring Boot Modular Monolith - Java 25)  │
│    ├── Controller Layer: Jakarta Validation & REST JSON DTO │
│    ├── Service Layer: Transaksi ACID, Domain Invariants     │
│    ├── GoF Strategy Pattern: Processor Pembayaran Tunai & QRIS│
│    ├── Concurrency Layer: Pessimistic Write Lock (FOR UPDATE)│
│    └── Repository: Spring Data JPA + H2 In-Memory DB Engine │
└─────────────────────────────────────────────────────────────┘
```

---

## 💎 Penerapan Clean Code & Design Patterns (CCDP)

Proyek ini dirancang secara ketat menerapkan prinsip **SOLID** dan pola desain GoF:

### 1. Backend Design Patterns
* **Strategy Pattern (`PaymentStrategy`):**
  * `CashPaymentStrategy`: Menghitung pembayaran tunai loket dan uang kembalian (*change*).
  * `QrisPaymentStrategy`: Menangani pembayaran digital QRIS instan.
  * Memenuhi *Open-Closed Principle (OCP)*: metode pembayaran baru (e.g., Kartu Kredit, Virtual Account) dapat ditambah tanpa mengubah kode pemesanan utama.
* **Factory & Registry Pattern (`PaymentStrategyFactory`):**
  * Memanfaatkan Spring Dependency Injection untuk resolusi dinamis strategi pembayaran saat runtime.
* **Domain State Machine (Explicit Invariants di Entity `Booking`):**
  * Menghindari mutasi status sembarangan via setter liar.
  * Transisi status melalui metode domain eksplisit: `confirmPayment()`, `claimTicketAtKiosk()`, `claimFoodAtCounter()`, dan `cancel()`.
* **Pencegahan Double-Booking (Concurrency Invariant):**
  * Menggunakan **Pessimistic Write Lock (`SELECT ... FOR UPDATE`)** pada `ShowtimeSeatRepository` untuk mengeliminasi *race-condition* saat dua pelanggan memilih kursi yang sama bersamaan.
  * Kolom `@Version` untuk *Optimistic Locking* sebagai pengamanan lapis kedua.
* **Penyimpanan Finansial Presisi:**
  * Menggunakan `BigDecimal` untuk seluruh mata uang dan perhitungan harga guna menghindari kesalahan pembulatan *floating-point*.
* **Immutability pada Batas API:**
  * Seluruh DTO request/response dibangun menggunakan **Java Record** (`ApiResponse`, `SeatTierResponse`, `ComprehensiveBookingResponse`).

### 2. Frontend Clean Architecture (Decomposed & Modular)
Memecah file monolitik sebelumnya menjadi lapisan-lapisan independen (**Single Responsibility Principle**, setiap file berukuran di bawah 300 baris):
* [`index.html`](cinema-backend/src/main/resources/static/index.html) — Murni hanya markup semantik HTML dan kontainer modal (tanpa inline CSS & script).
* [`css/style.css`](cinema-backend/src/main/resources/static/css/style.css) — Design system, variabel animasi fisik pegas (`.btn-spring`), kurva layar bioskop, dan animasi cetak termal kiosk.
* [`js/config.js`](cinema-backend/src/main/resources/static/js/config.js) — *Single source of truth* untuk state aplikasi (`AppState`) dan sesi pengguna/admin.
* [`js/utils.js`](cinema-backend/src/main/resources/static/js/utils.js) — Format mata uang Rupiah, sistem floating toast non-blocking, dan generator poster SVG fallback otomatis.
* [`js/api.js`](cinema-backend/src/main/resources/static/js/api.js) — Klien API terisolasi yang membungkus pemanggilan fetch REST endpoint backend.
* [`js/components/auth.js`](cinema-backend/src/main/resources/static/js/components/auth.js) — Logika login pengguna reguler dan gateway administrator backoffice.
* [`js/components/movies.js`](cinema-backend/src/main/resources/static/js/components/movies.js) — Katalog film tayang, filter kota, dan accordion jadwal tayang bioskop langsung di halaman film.
* [`js/components/booking.js`](cinema-backend/src/main/resources/static/js/components/booking.js) — Denah pemilihan kursi, countdown hold timer 5 menit (kursi terkunci abu-abu), pesanan F&B, dan pembayaran.
* [`js/components/tickets.js`](cinema-backend/src/main/resources/static/js/components/tickets.js) — Tiket digital dengan barcode, kode klaim PIN 6-digit (`CLM-XXXXXX`), dan simulasi cetak kiosk mandiri.
* [`js/components/admin.js`](cinema-backend/src/main/resources/static/js/components/admin.js) — Panel kontrol Backoffice CMS untuk menambah film, bioskop, studio, jadwal tayang, dan laporan transaksi live.
* [`js/app.js`](cinema-backend/src/main/resources/static/js/app.js) — Bootstrapper, micro-interaction splash screen, dan routing navigasi tab.

---

## 🌟 Fitur Unggulan Aplikasi

1. **Pemilihan Jadwal Langsung di Halaman Detail Film (Mirip TIX ID & CGV):**
   * Klik poster film mana pun $\rightarrow$ Langsung muncul pilihan tanggal (*Hari Ini, Rab, Kam*) serta daftar bioskop dan jenis studio (*Regular 2D, Gold Class 2D, Velvet 2D*).
   * Tombol *"Pesan Tiket"* terkunci aman sampai pengguna mengeklik jam tayang yang diinginkan.
2. **Kunci Kursi Sementara (Seat Hold dengan Status Abu-abu):**
   * Saat pembeli melanjutkan ke tahap checkout, kursi otomatis berubah status menjadi `HELD` (warna abu-abu dengan ikon gembok) di layar pembeli lain.
   * Dilengkapi countdown timer 5 menit; jika pembayaran tidak diselesaikan sebelum batas waktu, kursi otomatis dilepas kembali ke status tersedia.
3. **Pemesanan Makanan & Minuman (F&B Concessions):**
   * Pengguna dapat menambahkan popcorn, nachos, dan minuman ke dalam transaksi tiket dengan total kalkulasi terpadu.
4. **Tiket Digital & Simulasi Kiosk Mandiri di Bioskop:**
   * Setiap pesanan sukses menghasilkan e-ticket dengan barcode dan PIN 6-digit unik.
   * Menu **Kiosk Simulator** memungkinkan pengunjung memasukkan PIN untuk mencetak tiket fisik termal secara realistis.
5. **Panel Admin Backoffice CMS Terpisah:**
   * Login administrator khusus (`admin` / `admin123`) membuka antarmuka manajemen untuk menambah film baru, mendaftarkan bioskop, membuat studio, menjadwalkan jam tayang (otomatis membuat 34 kursi bertingkat), dan memonitor laporan pendapatan.

---

## 📂 Struktur Direktori Proyek

```text
TiketBioskop/
├── legacy/                               # Arsip kode aplikasi desktop asli (Java Swing NetBeans)
│   └── PenjualanTiketBioskop/
├── cinema-backend/                       # Spring Boot Modular Monolith Application
│   ├── src/main/java/com/cinema/booking/
│   │   ├── config/                       # Data seeder film, bioskop, kursi, dan menu F&B
│   │   ├── controller/                   # Endpoint REST API (Movie, Cinema, Showtime, Booking, User)
│   │   ├── dto/                          # Java Record DTO & Envelope Response
│   │   ├── exception/                    # Global Exception Handler & Business Exceptions
│   │   ├── model/                        # JPA Entities (Movie, Cinema, Studio, Showtime, Seat, Booking)
│   │   ├── pattern/                      # Strategy & Factory Pattern (Cash, QRIS)
│   │   ├── repository/                   # Spring Data JPA Repository dengan Pessimistic Write Lock
│   │   └── service/                      # Logika transaksi ACID & pemesanan tiket
│   ├── src/main/resources/
│   │   ├── application.properties        # Konfigurasi Spring Boot & H2 Database
│   │   └── static/                       # Frontend Web UI Modular (HTML, CSS, JS)
│   │       ├── css/style.css             # Stylesheet & Animasi Kiosk
│   │       ├── js/                       # Skrip Modular (config, utils, api, app)
│   │       └── js/components/            # Komponen UI (auth, movies, booking, tickets, admin)
│   └── src/test/java/com/cinema/booking/ # Unit & Integration Test Suite (10 passing tests)
├── Cinema_Booking_Reengineering_PRD.md   # Dokumen Kebutuhan Produk (PRD) & Blueprint Arsitektur
├── .agents/                              # Panduan Standar Rekayasa Java & Spring Boot
└── README.md                             # Dokumentasi portofolio reengineering
```

---

## 🛠️ Cara Menjalankan & Menguji Aplikasi

### Prasyarat
* Java JDK 21 atau lebih baru (telah diuji dengan OpenJDK 25 LTS).
* Menggunakan Maven Wrapper bawaan (`mvnw.cmd`).

### 1. Menjalankan Aplikasi
Buka PowerShell di direktori proyek:
```powershell
cd cinema-backend
.\mvnw.cmd spring-boot:run
```
Buka browser dan akses **`http://localhost:8080`**.

### 2. Akun Demo untuk Pengujian
* **Pengguna Reguler:** Klik ikon profil di pojok kanan atas $\rightarrow$ Klik *"Gunakan Akun Demo Budi"* $\rightarrow$ Masuk.
* **Administrator CMS:** Klik ikon profil $\rightarrow$ Pilih *"Login sebagai Administrator"* $\rightarrow$ Masukkan Username `admin`, Password `admin123` $\rightarrow$ Masuk ke Dashboard Backoffice CMS.

### 3. Mengakses Konsol Basis Data (H2 Web Console)
Akses `http://localhost:8080/h2-console`:
* **JDBC URL:** `jdbc:h2:mem:cinemadb`
* **User Name:** `sa`
* **Password:** *(kosongkan)*

### 4. Menjalankan Pengujian Otomatis
```powershell
cd cinema-backend
.\mvnw.cmd test
```
*Hasil:* **10 tests run, 0 failures, 0 errors, 0 skipped (`BUILD SUCCESS`)**.

---

## 📡 Ringkasan Endpoint REST API

| Kategori | Endpoint | Metode | Deskripsi |
| :--- | :--- | :---: | :--- |
| **Film** | `/api/movies` | `GET` | Mendapatkan seluruh film sedang tayang |
| **Film** | `/api/movies/coming-soon` | `GET` | Mendapatkan daftar film akan datang |
| **Film** | `/api/movies` | `POST` | Menambahkan film baru ke katalog (Admin) |
| **Bioskop** | `/api/cinemas/cities` | `GET` | Mendapatkan daftar kota operasional |
| **Bioskop** | `/api/cinemas?city={city}` | `GET` | Mendapatkan bioskop berdasarkan filter kota |
| **Bioskop** | `/api/cinemas/{id}/studios` | `GET` | Mendapatkan daftar studio dalam suatu bioskop |
| **Jadwal** | `/api/cinemas/showtimes` | `GET` | Mendapatkan jadwal tayang per film dan bioskop |
| **Jadwal** | `/api/cinemas/showtimes` | `POST` | Menambah jadwal tayang baru + auto generate 34 kursi |
| **Kursi** | `/api/showtimes/{id}/seats` | `GET` | Mengambil denah kursi dan status (*Available*, *Held*, *Booked*) |
| **Kursi** | `/api/showtimes/{id}/hold` | `POST` | Mengunci kursi sementara selama 5 menit (*TTL Hold*) |
| **Kursi** | `/api/showtimes/{id}/release`| `POST` | Melepaskan kursi yang terkunci |
| **Makanan** | `/api/foods` | `GET` | Mengambil katalog menu makanan & minuman |
| **Pemesanan**| `/api/v2/bookings` | `POST` | Membayar tiket & makanan (Cash / QRIS) dan menerbitkan e-ticket |
| **Pemesanan**| `/api/v2/bookings` | `GET` | Mengambil seluruh riwayat transaksi |
| **Kiosk** | `/api/v2/bookings/claim-ticket`| `POST` | Memvalidasi PIN dan mencetak tiket fisik di mesin Kiosk |
