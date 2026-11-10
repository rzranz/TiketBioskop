package com.cinema.booking.model;

import jakarta.persistence.*;

/**
 * Entity Studio Bioskop (Auditorium).
 * Contoh tipe: "REGULAR 2D", "GOLD CLASS 2D", "VELVET 2D".
 */
@Entity
@Table(name = "studios")
public class Studio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cinema_id", nullable = false)
    private Long cinemaId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "studio_type", nullable = false, length = 50)
    private String studioType; // "REGULAR 2D", "GOLD CLASS 2D", "VELVET 2D"

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats = 34;

    public Studio() {
    }

    public Studio(Long cinemaId, String name, String studioType, Integer totalSeats) {
        this.cinemaId = cinemaId;
        this.name = name;
        this.studioType = studioType;
        this.totalSeats = totalSeats;
    }

    public Long getId() {
        return id;
    }

    public Long getCinemaId() {
        return cinemaId;
    }

    public String getName() {
        return name;
    }

    public String getStudioType() {
        return studioType;
    }

    public Integer getTotalSeats() {
        return totalSeats;
    }
}

