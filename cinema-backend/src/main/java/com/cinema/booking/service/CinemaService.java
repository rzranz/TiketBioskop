package com.cinema.booking.service;

import com.cinema.booking.model.Cinema;
import com.cinema.booking.model.SeatTier;
import com.cinema.booking.model.Showtime;
import com.cinema.booking.model.ShowtimeSeat;
import com.cinema.booking.model.Studio;
import com.cinema.booking.repository.CinemaRepository;
import com.cinema.booking.repository.ShowtimeRepository;
import com.cinema.booking.repository.ShowtimeSeatRepository;
import com.cinema.booking.repository.StudioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CinemaService {

    private final CinemaRepository cinemaRepository;
    private final ShowtimeRepository showtimeRepository;
    private final StudioRepository studioRepository;
    private final ShowtimeSeatRepository showtimeSeatRepository;

    public CinemaService(
        CinemaRepository cinemaRepository, 
        ShowtimeRepository showtimeRepository,
        StudioRepository studioRepository,
        ShowtimeSeatRepository showtimeSeatRepository
    ) {
        this.cinemaRepository = cinemaRepository;
        this.showtimeRepository = showtimeRepository;
        this.studioRepository = studioRepository;
        this.showtimeSeatRepository = showtimeSeatRepository;
    }

    public List<String> getAllCities() {
        return List.of("BANDUNG", "JAKARTA", "SURABAYA", "YOGYAKARTA");
    }

    public List<Cinema> getCinemasByCity(String city) {
        if (city == null || city.isBlank()) {
            return cinemaRepository.findAll();
        }
        return cinemaRepository.findByCityIgnoreCase(city.trim());
    }

    public List<Studio> getStudiosByCinema(Long cinemaId) {
        return studioRepository.findByCinemaId(cinemaId);
    }

    @Transactional
    public Cinema saveCinema(Cinema cinema) {
        return cinemaRepository.save(cinema);
    }

    @Transactional
    public Studio saveStudio(Studio studio) {
        return studioRepository.save(studio);
    }

    @Transactional
    public Showtime saveShowtime(Showtime showtime) {
        Showtime saved = showtimeRepository.save(showtime);
        
        // Otomatis generate 34 layout kursi untuk jadwal tayang baru
        List<ShowtimeSeat> stSeats = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            stSeats.add(new ShowtimeSeat(saved.getId(), "A" + i, "A", i, SeatTier.SATINE_SUITE, SeatTier.SATINE_SUITE.getBasePrice()));
        }
        for (int i = 1; i <= 6; i++) {
            stSeats.add(new ShowtimeSeat(saved.getId(), "B" + i, "B", i, SeatTier.SWEET_BOX, SeatTier.SWEET_BOX.getBasePrice()));
        }
        for (String r : List.of("C", "D", "E")) {
            for (int i = 1; i <= 8; i++) {
                stSeats.add(new ShowtimeSeat(saved.getId(), r + i, r, i, SeatTier.REGULER, SeatTier.REGULER.getBasePrice()));
            }
        }
        showtimeSeatRepository.saveAll(stSeats);
        return saved;
    }

    public List<Showtime> getShowtimesByMovieAndCinema(Long movieId, Long cinemaId) {
        return showtimeRepository.findByMovieIdAndCinemaId(movieId, cinemaId);
    }

    public List<Showtime> getShowtimesByMovie(Long movieId) {
        return showtimeRepository.findByMovieId(movieId);
    }

    public List<Showtime> getShowtimesByCinema(Long cinemaId) {
        return showtimeRepository.findByCinemaId(cinemaId);
    }

    public List<Showtime> getAllShowtimes() {
        return showtimeRepository.findAll();
    }
}

