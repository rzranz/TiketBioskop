package com.cinema.booking.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Entity Menu Makanan & Minuman (F&B / Cinema Concessions).
 */
@Entity
@Table(name = "food_items")
public class FoodItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String category; // "POPCORN", "MINUMAN", "SNACK", "COMBO"

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(length = 255)
    private String description;

    public FoodItem() {
    }

    public FoodItem(String name, String category, BigDecimal price, String imageUrl, String description) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.imageUrl = imageUrl;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDescription() {
        return description;
    }
}

