package com.cinema.booking.repository;

import com.cinema.booking.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    List<Movie> findByNowShowingTrue();
    List<Movie> findByNowShowingFalse();
}

