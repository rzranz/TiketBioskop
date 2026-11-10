package com.cinema.booking.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Entity Jadwal Tayang Film Bioskop.
 * Menghubungkan Film, Bioskop, Studio, Tanggal, Jam Tayang, dan Harga Tiket.
 */
@Entity
@Table(name = "showtimes")
public class Showtime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "movie_id", nullable = false)
    private Long movieId;

    @Column(name = "cinema_id", nullable = false)
    private Long cinemaId;

    @Column(name = "studio_id", nullable = false)
    private Long studioId;

    @Column(name = "studio_type", nullable = false, length = 50)
    private String studioType; // REGULAR 2D, GOLD CLASS 2D, VELVET 2D

    @Column(name = "show_date", nullable = false, length = 20)
    private String showDate; // format: "2026-11-10"

    @Column(name = "start_time", nullable = false, length = 10)
    private String startTime; // format: "12:30", "15:15", "18:00"

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    public Showtime() {
    }

    public Showtime(Long movieId, Long cinemaId, Long studioId, String studioType, 
                    String showDate, String startTime, BigDecimal price) {
        this.movieId = movieId;
        this.cinemaId = cinemaId;
        this.studioId = studioId;
        this.studioType = studioType;
        this.showDate = showDate;
        this.startTime = startTime;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public Long getMovieId() {
        return movieId;
    }

    public Long getCinemaId() {
        return cinemaId;
    }

    public Long getStudioId() {
        return studioId;
    }

    public String getStudioType() {
        return studioType;
    }

    public String getShowDate() {
        return showDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public BigDecimal getPrice() {
        return price;
    }
}

