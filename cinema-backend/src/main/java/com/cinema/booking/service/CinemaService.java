package com.cinema.booking.service;

import com.cinema.booking.model.Cinema;
import com.cinema.booking.model.Showtime;
import com.cinema.booking.repository.CinemaRepository;
import com.cinema.booking.repository.ShowtimeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CinemaService {

    private final CinemaRepository cinemaRepository;
    private final ShowtimeRepository showtimeRepository;

    public CinemaService(CinemaRepository cinemaRepository, ShowtimeRepository showtimeRepository) {
        this.cinemaRepository = cinemaRepository;
        this.showtimeRepository = showtimeRepository;
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

    public List<Showtime> getShowtimesByMovieAndCinema(Long movieId, Long cinemaId) {
        return showtimeRepository.findByMovieIdAndCinemaId(movieId, cinemaId);
    }

    public List<Showtime> getShowtimesByMovie(Long movieId) {
        return showtimeRepository.findByMovieId(movieId);
    }
}

