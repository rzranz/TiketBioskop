package com.cinema.booking.controller;

import com.cinema.booking.dto.ApiResponse;
import com.cinema.booking.model.Cinema;
import com.cinema.booking.model.Showtime;
import com.cinema.booking.service.CinemaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cinemas")
@CrossOrigin(origins = "*")
public class CinemaController {

    private final CinemaService cinemaService;

    public CinemaController(CinemaService cinemaService) {
        this.cinemaService = cinemaService;
    }

    @GetMapping("/cities")
    public ResponseEntity<ApiResponse<List<String>>> getCities() {
        return ResponseEntity.ok(ApiResponse.ok(cinemaService.getAllCities()));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Cinema>>> getCinemas(@RequestParam(required = false) String city) {
        return ResponseEntity.ok(ApiResponse.ok(cinemaService.getCinemasByCity(city)));
    }

    @GetMapping("/showtimes")
    public ResponseEntity<ApiResponse<List<Showtime>>> getShowtimes(
        @RequestParam Long movieId,
        @RequestParam(required = false) Long cinemaId
    ) {
        if (cinemaId != null) {
            return ResponseEntity.ok(ApiResponse.ok(cinemaService.getShowtimesByMovieAndCinema(movieId, cinemaId)));
        }
        return ResponseEntity.ok(ApiResponse.ok(cinemaService.getShowtimesByMovie(movieId)));
    }
}

