package com.cinema.booking.controller;

import com.cinema.booking.dto.ApiResponse;
import com.cinema.booking.model.Movie;
import com.cinema.booking.service.MovieService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@CrossOrigin(origins = "*")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Movie>>> getNowShowing() {
        return ResponseEntity.ok(ApiResponse.ok("Daftar film sedang tayang", movieService.getNowShowing()));
    }

    @GetMapping("/coming-soon")
    public ResponseEntity<ApiResponse<List<Movie>>> getComingSoon() {
        return ResponseEntity.ok(ApiResponse.ok("Daftar film akan hadir", movieService.getComingSoon()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Movie>> getMovieById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(movieService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Movie>> addMovie(@RequestBody Movie movie) {
        Movie saved = movieService.save(movie);
        return ResponseEntity.ok(ApiResponse.ok("Film berhasil ditambahkan ke katalog!", saved));
    }
}

