package com.cinema.booking.model;

import jakarta.persistence.*;

/**
 * Entity Bioskop / Teater.
 * Menyimpan nama bioskop, grup waralaba (XXI, CGV, Cinepolis), dan kota/daerahnya.
 */
@Entity
@Table(name = "cinemas")
public class Cinema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 30)
    private String brand; // "XXI", "CGV", "CINEPOLIS"

    @Column(nullable = false, length = 50)
    private String city; // "BANDUNG", "JAKARTA", "SURABAYA", "YOGYAKARTA"

    @Column(length = 255)
    private String address;

    public Cinema() {
    }

    public Cinema(String name, String brand, String city, String address) {
        this.name = name;
        this.brand = brand;
        this.city = city;
        this.address = address;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBrand() {
        return brand;
    }

    public String getCity() {
        return city;
    }

    public String getAddress() {
        return address;
    }
}

