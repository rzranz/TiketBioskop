package com.cinema.booking.model;

import jakarta.persistence.*;

/**
 * Entity Katalog Film Bioskop.
 * Menyimpan data film, poster berkualitas tinggi, rating IMDb, usia, sinopsis, dan trailer.
 */
@Entity
@Table(name = "movies")
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(name = "poster_url", nullable = false, length = 500)
    private String posterUrl;

    @Column(name = "backdrop_url", length = 500)
    private String backdropUrl;

    @Column(nullable = false, length = 100)
    private String genre;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "imdb_rating", length = 10)
    private String imdbRating;

    @Column(name = "rotten_score", length = 10)
    private String rottenScore;

    @Column(name = "age_rating", length = 10)
    private String ageRating;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String synopsis;

    @Column(name = "trailer_url", length = 500)
    private String trailerUrl;

    @Column(name = "is_now_showing", nullable = false)
    private boolean nowShowing = true;

    @Column(name = "rank_badge")
    private Integer rankBadge;

    public Movie() {
    }

    public Movie(String title, String posterUrl, String backdropUrl, String genre, 
                 Integer durationMinutes, String imdbRating, String rottenScore, 
                 String ageRating, String synopsis, String trailerUrl, boolean nowShowing, Integer rankBadge) {
        this.title = title;
        this.posterUrl = posterUrl;
        this.backdropUrl = backdropUrl;
        this.genre = genre;
        this.durationMinutes = durationMinutes;
        this.imdbRating = imdbRating;
        this.rottenScore = rottenScore;
        this.ageRating = ageRating;
        this.synopsis = synopsis;
        this.trailerUrl = trailerUrl;
        this.nowShowing = nowShowing;
        this.rankBadge = rankBadge;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public String getBackdropUrl() {
        return backdropUrl;
    }

    public String getGenre() {
        return genre;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public String getImdbRating() {
        return imdbRating;
    }

    public String getRottenScore() {
        return rottenScore;
    }

    public String getAgeRating() {
        return ageRating;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public String getTrailerUrl() {
        return trailerUrl;
    }

    public boolean isNowShowing() {
        return nowShowing;
    }

    public Integer getRankBadge() {
        return rankBadge;
    }
}

