package com.cinema.booking.repository;

import com.cinema.booking.model.Studio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudioRepository extends JpaRepository<Studio, Long> {
    List<Studio> findByCinemaId(Long cinemaId);
}

