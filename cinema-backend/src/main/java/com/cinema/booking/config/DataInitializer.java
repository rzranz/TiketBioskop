package com.cinema.booking.config;

import com.cinema.booking.model.*;
import com.cinema.booking.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Seeder data komprehensif: Film nyata, Bioskop berdasar kota, Studio, Jadwal Jam Tayang,
 * Kursi per Jadwal (Showtime Inventory), Menu Makanan & Minuman (F&B), serta Akun User.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final SeatRepository seatRepository;
    private final MovieRepository movieRepository;
    private final CinemaRepository cinemaRepository;
    private final StudioRepository studioRepository;
    private final ShowtimeRepository showtimeRepository;
    private final ShowtimeSeatRepository showtimeSeatRepository;
    private final FoodItemRepository foodItemRepository;
    private final UserRepository userRepository;

    public DataInitializer(
        SeatRepository seatRepository,
        MovieRepository movieRepository,
        CinemaRepository cinemaRepository,
        StudioRepository studioRepository,
        ShowtimeRepository showtimeRepository,
        ShowtimeSeatRepository showtimeSeatRepository,
        FoodItemRepository foodItemRepository,
        UserRepository userRepository
    ) {
        this.seatRepository = seatRepository;
        this.movieRepository = movieRepository;
        this.cinemaRepository = cinemaRepository;
        this.studioRepository = studioRepository;
        this.showtimeRepository = showtimeRepository;
        this.showtimeSeatRepository = showtimeSeatRepository;
        this.foodItemRepository = foodItemRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        // 1. Initial User Profile (seperti screenshot TIX ID dengan 8.399 Coins)
        if (userRepository.count() == 0) {
            userRepository.save(new User(
                "Randi Pratama",
                "randi@tixid.example.com",
                "081234567890",
                "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&q=80",
                8399
            ));
        }

        // 2. Initial Movies (Official movie posters from TMDb & Wikimedia)
        if (movieRepository.count() == 0) {
            log.info("Menginisialisasi data film bioskop...");
            List<Movie> movies = List.of(
                new Movie(
                    "Godzilla Minus One",
                    "https://image.tmdb.org/t/p/w500/hkxxMIGaiCTmrEArK7J56JTKUlB.jpg",
                    "https://image.tmdb.org/t/p/w1280/jXJxMcVoEuXzym3vFnjqDW4ifo6.jpg",
                    "Action, Sci-Fi, Drama",
                    125,
                    "8.3/10",
                    "98%",
                    "13+",
                    "Jepang pascaperang berada pada titik terendah ketika sebuah krisis baru yang mengerikan muncul dalam bentuk monster raksasa yang bermutasi akibat radiasi bom atom.",
                    "https://www.youtube.com/watch?v=VvSrHIX5a-0",
                    true,
                    1
                ),
                new Movie(
                    "Agak Laen",
                    "https://upload.wikimedia.org/wikipedia/en/9/91/Agak_Laen_film_poster.jpg",
                    "https://image.tmdb.org/t/p/w1280/jXJxMcVoEuXzym3vFnjqDW4ifo6.jpg",
                    "Comedy, Horror",
                    119,
                    "8.0/10",
                    "95%",
                    "13+",
                    "Empat sahabat pengelola rumah hantu pasar malam yang terancam bangkrut mengalami insiden tak terduga saat seorang pengunjung wafat karena serangan jantung dan harus mereka kubur diam-diam.",
                    "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                    true,
                    2
                ),
                new Movie(
                    "Spider-Man: No Way Home",
                    "https://image.tmdb.org/t/p/w500/1g0dhYtq4irTY1GPXvft6k4YLjm.jpg",
                    "https://image.tmdb.org/t/p/w1280/14QbnygCuTO0vl7CAFmPf1fgZfV.jpg",
                    "Action, Adventure, Sci-Fi",
                    148,
                    "8.2/10",
                    "93%",
                    "13+",
                    "Identitas Peter Parker sebagai Spider-Man terbongkar ke seluruh dunia. Bantuan Doctor Strange yang keliru justru membuka gerbang multiverse yang mempertemukan musuh-musuh legendaris.",
                    "https://www.youtube.com/watch?v=JfVOs4VSpmA",
                    true,
                    null
                ),
                new Movie(
                    "Everything Everywhere All At Once",
                    "https://image.tmdb.org/t/p/w500/u68AjlvlutfEIcpmbYpKcdi09ut.jpg",
                    "https://image.tmdb.org/t/p/w1280/fOy2JurzKANt6Rq7nGsgUtQBVT6.jpg",
                    "Action, Adventure, Sci-Fi",
                    139,
                    "7.8/10",
                    "94%",
                    "17+",
                    "Seorang imigran Tionghoa paruh baya terseret ke dalam petualangan gila melintasi multisemesta di mana hanya dialah yang dapat menyelamatkan seluruh eksistensi semesta.",
                    "https://www.youtube.com/watch?v=wxN1T1uxQ2g",
                    true,
                    null
                ),
                new Movie(
                    "Sonic The Hedgehog 2",
                    "https://image.tmdb.org/t/p/w500/6DrHO1jr3qVrViUO6s6kFiAGM7.jpg",
                    "https://image.tmdb.org/t/p/w1280/egoyMDLqCflOTTO4Pskjb1FBYdU.jpg",
                    "Animation, Action, Comedy",
                    122,
                    "6.5/10",
                    "69%",
                    "SU",
                    "Sonic dan Tails berpacu melintasi dunia untuk menemukan zamrud Master Emerald mistis sebelum jatuh ke tangan Dr. Robotnik dan sekutu barunya, Knuckles.",
                    "https://www.youtube.com/watch?v=47r8FXYZWNU",
                    true,
                    null
                ),
                new Movie(
                    "Siksa Kubur",
                    "https://upload.wikimedia.org/wikipedia/en/2/24/Grave_Torture_2024_poster.jpg",
                    "https://image.tmdb.org/t/p/w1280/14QbnygCuTO0vl7CAFmPf1fgZfV.jpg",
                    "Horror, Mystery",
                    117,
                    "7.4/10",
                    "88%",
                    "17+",
                    "Setelah kedua orang tuanya meninggal akibat bom bunuh diri, Sita tidak lagi percaya pada agama dan bertekad mencari orang paling berdosa untuk membuktikan bahwa siksa kubur itu tidak ada.",
                    "https://www.youtube.com/watch?v=7b77vYf9Pps",
                    true,
                    null
                ),
                new Movie(
                    "Dune: Part Two",
                    "https://image.tmdb.org/t/p/w500/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg",
                    "https://image.tmdb.org/t/p/w1280/xOMo8BRK7PfcJv9JCnx7s520DRq.jpg",
                    "Sci-Fi, Adventure",
                    166,
                    "8.6/10",
                    "92%",
                    "13+",
                    "Paul Atreides bersatu dengan Chani dan kaum Fremen dalam perjalanan balas dendam terhadap para konspirator yang telah meruntuhkan keluarganya.",
                    "https://www.youtube.com/watch?v=Way9Dexny3w",
                    false,
                    null
                ),
                new Movie(
                    "Avengers: Endgame",
                    "https://image.tmdb.org/t/p/w500/or06FN3Dka5tukK1e9sl16pB3iy.jpg",
                    "https://image.tmdb.org/t/p/w1280/7RyHsO4yDXtBv1zUU3mTpHeQ0d5.jpg",
                    "Action, Sci-Fi",
                    181,
                    "8.4/10",
                    "94%",
                    "13+",
                    "Setelah peristiwa dahsyat di Avengers: Infinity War, sisa anggota Avengers berkumpul kembali demi membalikkan tindakan Thanos dan memulihkan keteraturan di alam semesta.",
                    "https://www.youtube.com/watch?v=TcMBFSGVi1c",
                    false,
                    null
                )
            );
            movieRepository.saveAll(movies);
        }

        // 3. Initial Cinemas (Sesuai kota & screenshot 4: "23 PASKAL SHOPPING CENTER CGV")
        if (cinemaRepository.count() == 0) {
            log.info("Menginisialisasi data bioskop...");
            List<Cinema> cinemas = List.of(
                new Cinema("23 Paskal Shopping Center CGV", "CGV", "BANDUNG", "Jl. Pasir Kaliki No. 23, Bandung"),
                new Cinema("Paris Van Java CGV", "CGV", "BANDUNG", "Jl. Sukajadi No. 131-139, Bandung"),
                new Cinema("Ciwalk XXI", "XXI", "BANDUNG", "Cihampelas Walk, Jl. Cihampelas, Bandung"),
                new Cinema("Grand Indonesia CGV", "CGV", "JAKARTA", "Grand Indonesia Mall Lt. 8, Jakarta Pusat"),
                new Cinema("Plaza Senayan XXI", "XXI", "JAKARTA", "Plaza Senayan Lt. 5, Jakarta Selatan"),
                new Cinema("Tunjungan Plaza XXI", "XXI", "SURABAYA", "Tunjungan Plaza 5 Lt. 10, Surabaya"),
                new Cinema("Pakuwon Mall CGV", "CGV", "SURABAYA", "Pakuwon Mall Lt. 2, Surabaya"),
                new Cinema("Ambarrukmo XXI", "XXI", "YOGYAKARTA", "Plaza Ambarrukmo Lt. 3, Sleman, Yogyakarta")
            );
            cinemaRepository.saveAll(cinemas);
        }

        // 4. Initial Studios & Showtimes for 23 Paskal CGV (Cinema ID 1)
        Cinema paskal = cinemaRepository.findAll().stream()
            .filter(c -> c.getName().contains("23 Paskal"))
            .findFirst().orElse(null);

        if (paskal != null && studioRepository.count() == 0) {
            log.info("Menginisialisasi studio dan jadwal tayang...");
            Studio regStudio = studioRepository.save(new Studio(paskal.getId(), "Studio 1", "REGULAR 2D", 34));
            Studio goldStudio = studioRepository.save(new Studio(paskal.getId(), "Gold Class", "GOLD CLASS 2D", 34));
            Studio velvetStudio = studioRepository.save(new Studio(paskal.getId(), "Velvet Suite", "VELVET 2D", 34));

            Movie godzilla = movieRepository.findAll().stream().filter(m -> m.getTitle().contains("Godzilla")).findFirst().orElse(null);
            Movie spiderman = movieRepository.findAll().stream().filter(m -> m.getTitle().contains("Spider-Man")).findFirst().orElse(null);

            String today = LocalDate.now().toString();

            List<Showtime> showtimes = new ArrayList<>();
            if (godzilla != null) {
                // REGULAR 2D (Rp 45.000)
                showtimes.add(new Showtime(godzilla.getId(), paskal.getId(), regStudio.getId(), "REGULAR 2D", today, "12:30", new BigDecimal("45000.00")));
                showtimes.add(new Showtime(godzilla.getId(), paskal.getId(), regStudio.getId(), "REGULAR 2D", today, "15:15", new BigDecimal("45000.00")));
                showtimes.add(new Showtime(godzilla.getId(), paskal.getId(), regStudio.getId(), "REGULAR 2D", today, "18:00", new BigDecimal("45000.00")));
                showtimes.add(new Showtime(godzilla.getId(), paskal.getId(), regStudio.getId(), "REGULAR 2D", today, "20:45", new BigDecimal("45000.00")));

                // GOLD CLASS 2D (Rp 80.000 - sesuai screenshot 4)
                showtimes.add(new Showtime(godzilla.getId(), paskal.getId(), goldStudio.getId(), "GOLD CLASS 2D", today, "13:30", new BigDecimal("80000.00")));
                showtimes.add(new Showtime(godzilla.getId(), paskal.getId(), goldStudio.getId(), "GOLD CLASS 2D", today, "16:15", new BigDecimal("80000.00")));
                showtimes.add(new Showtime(godzilla.getId(), paskal.getId(), goldStudio.getId(), "GOLD CLASS 2D", today, "19:00", new BigDecimal("80000.00")));

                // VELVET 2D (Rp 80.000 - sesuai screenshot 4)
                showtimes.add(new Showtime(godzilla.getId(), paskal.getId(), velvetStudio.getId(), "VELVET 2D", today, "14:00", new BigDecimal("80000.00")));
                showtimes.add(new Showtime(godzilla.getId(), paskal.getId(), velvetStudio.getId(), "VELVET 2D", today, "16:45", new BigDecimal("80000.00")));
                showtimes.add(new Showtime(godzilla.getId(), paskal.getId(), velvetStudio.getId(), "VELVET 2D", today, "19:30", new BigDecimal("80000.00")));
            }

            if (spiderman != null) {
                showtimes.add(new Showtime(spiderman.getId(), paskal.getId(), regStudio.getId(), "REGULAR 2D", today, "13:00", new BigDecimal("45000.00")));
                showtimes.add(new Showtime(spiderman.getId(), paskal.getId(), regStudio.getId(), "REGULAR 2D", today, "16:30", new BigDecimal("45000.00")));
                showtimes.add(new Showtime(spiderman.getId(), paskal.getId(), regStudio.getId(), "REGULAR 2D", today, "19:45", new BigDecimal("45000.00")));
            }

            List<Showtime> savedShowtimes = showtimeRepository.saveAll(showtimes);

            // Inisialisasi kursi per jadwal tayang
            for (Showtime st : savedShowtimes) {
                List<ShowtimeSeat> stSeats = new ArrayList<>();
                // Row A: Satine Suite (A1-A4)
                for (int i = 1; i <= 4; i++) {
                    stSeats.add(new ShowtimeSeat(st.getId(), "A" + i, "A", i, SeatTier.SATINE_SUITE, SeatTier.SATINE_SUITE.getBasePrice()));
                }
                // Row B: Sweet Box (B1-B6)
                for (int i = 1; i <= 6; i++) {
                    stSeats.add(new ShowtimeSeat(st.getId(), "B" + i, "B", i, SeatTier.SWEET_BOX, SeatTier.SWEET_BOX.getBasePrice()));
                }
                // Row C, D, E: Reguler (8 per row)
                for (String r : List.of("C", "D", "E")) {
                    for (int i = 1; i <= 8; i++) {
                        stSeats.add(new ShowtimeSeat(st.getId(), r + i, r, i, SeatTier.REGULER, SeatTier.REGULER.getBasePrice()));
                    }
                }
                showtimeSeatRepository.saveAll(stSeats);
            }
        }

        // 5. Initial Food & Beverage Items (Concessions)
        if (foodItemRepository.count() == 0) {
            log.info("Menginisialisasi menu makanan & minuman bioskop...");
            List<FoodItem> foods = List.of(
                new FoodItem("Popcorn Caramel XXI", "POPCORN", new BigDecimal("55000.00"), "https://images.unsplash.com/photo-1578849278619-e73505e9610f?auto=format&fit=crop&w=300&q=80", "Popcorn manis gurih renyah dengan baluran karamel mentega khas bioskop."),
                new FoodItem("Popcorn Salty Crispy", "POPCORN", new BigDecimal("45000.00"), "https://images.unsplash.com/photo-1585647347483-22b66260dfff?auto=format&fit=crop&w=300&q=80", "Popcorn asin gurih klasik dengan mentega pilihan."),
                new FoodItem("Coca-Cola Large", "MINUMAN", new BigDecimal("25000.00"), "https://images.unsplash.com/photo-1622483767028-3f66f32aef97?auto=format&fit=crop&w=300&q=80", "Minuman berkarbonasi dingin segar ukuran jumbo."),
                new FoodItem("Iced Lemon Tea Segar", "MINUMAN", new BigDecimal("28000.00"), "https://images.unsplash.com/photo-1556679343-c7306c1976bc?auto=format&fit=crop&w=300&q=80", "Teh lemon dingin segar pelepas dahaga."),
                new FoodItem("Crispy Nachos with Warm Cheese", "SNACK", new BigDecimal("42000.00"), "https://images.unsplash.com/photo-1513456852971-30c0b8199d4d?auto=format&fit=crop&w=300&q=80", "Tortilla chips renyah disajikan dengan cocolan saus keju hangat lezat."),
                new FoodItem("Hotdog Sosis Keju Panggang", "SNACK", new BigDecimal("38000.00"), "https://images.unsplash.com/photo-1619740455993-9e612b1af08a?auto=format&fit=crop&w=300&q=80", "Roti lembut dengan sosis sapi premium panggang dan saus mustard."),
                new FoodItem("Combo Seru Berdua (1 Caramel + 2 Drinks)", "COMBO", new BigDecimal("85000.00"), "https://images.unsplash.com/photo-1505686994434-e3cc5abf1330?auto=format&fit=crop&w=300&q=80", "Paket hemat nonton berdua: 1 Popcorn Caramel Besar + 2 Minuman Dingin.")
            );
            foodItemRepository.saveAll(foods);
        }

        // 6. Seed legacy studio seats if empty
        if (seatRepository.count() == 0) {
            List<Seat> initialSeats = new ArrayList<>();
            for (int i = 1; i <= 4; i++) initialSeats.add(new Seat("A" + i, "A", i, SeatTier.SATINE_SUITE, SeatTier.SATINE_SUITE.getBasePrice()));
            for (int i = 1; i <= 6; i++) initialSeats.add(new Seat("B" + i, "B", i, SeatTier.SWEET_BOX, SeatTier.SWEET_BOX.getBasePrice()));
            for (String row : List.of("C", "D", "E")) {
                for (int i = 1; i <= 8; i++) initialSeats.add(new Seat(row + i, row, i, SeatTier.REGULER, SeatTier.REGULER.getBasePrice()));
            }
            seatRepository.saveAll(initialSeats);
        }
    }
}
