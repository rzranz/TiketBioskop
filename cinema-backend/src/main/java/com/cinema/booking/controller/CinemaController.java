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

    @PostMapping
    public ResponseEntity<ApiResponse<Cinema>> addCinema(@RequestBody Cinema cinema) {
        Cinema saved = cinemaService.saveCinema(cinema);
        return ResponseEntity.ok(ApiResponse.ok("Bioskop berhasil ditambahkan!", saved));
    }

    @GetMapping("/{id}/studios")
    public ResponseEntity<ApiResponse<List<com.cinema.booking.model.Studio>>> getStudios(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(cinemaService.getStudiosByCinema(id)));
    }

    @PostMapping("/{id}/studios")
    public ResponseEntity<ApiResponse<com.cinema.booking.model.Studio>> addStudio(
        @PathVariable Long id, 
        @RequestBody com.cinema.booking.model.Studio studio
    ) {
        com.cinema.booking.model.Studio toSave = new com.cinema.booking.model.Studio(
            id, studio.getName(), studio.getStudioType(), studio.getTotalSeats() != null ? studio.getTotalSeats() : 34
        );
        com.cinema.booking.model.Studio saved = cinemaService.saveStudio(toSave);
        return ResponseEntity.ok(ApiResponse.ok("Studio berhasil ditambahkan!", saved));
    }

    @PostMapping("/showtimes")
    public ResponseEntity<ApiResponse<Showtime>> addShowtime(@RequestBody Showtime showtime) {
        Showtime saved = cinemaService.saveShowtime(showtime);
        return ResponseEntity.ok(ApiResponse.ok("Jadwal tayang berhasil ditambahkan beserta 34 kursi!", saved));
    }
}

