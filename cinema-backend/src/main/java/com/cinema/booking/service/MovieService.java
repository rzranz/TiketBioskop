package com.cinema.booking.service;

import com.cinema.booking.exception.ResourceNotFoundException;
import com.cinema.booking.model.Movie;
import com.cinema.booking.repository.MovieRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class MovieService {

    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public List<Movie> getNowShowing() {
        return movieRepository.findByNowShowingTrue();
    }

    public List<Movie> getComingSoon() {
        return movieRepository.findByNowShowingFalse();
    }

    public Movie getById(Long id) {
        return movieRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Film dengan ID " + id + " tidak ditemukan."));
    }
}

