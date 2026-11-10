package com.cinema.booking.model;

import jakarta.persistence.*;

/**
 * Entity Pengguna / Akun Pelanggan Bioskop.
 * Menyimpan data profil dan loyalty TIX VIP Coins (seperti pada aplikasi TIX ID).
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "tix_coins", nullable = false)
    private Integer tixCoins = 8399;

    public User() {
    }

    public User(String name, String email, String phone, String avatarUrl, Integer tixCoins) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.avatarUrl = avatarUrl;
        this.tixCoins = tixCoins;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public Integer getTixCoins() {
        return tixCoins;
    }

    public void setTixCoins(Integer tixCoins) {
        this.tixCoins = tixCoins;
    }
}

