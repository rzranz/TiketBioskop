package com.cinema.booking.controller;

import com.cinema.booking.dto.ApiResponse;
import com.cinema.booking.model.FoodItem;
import com.cinema.booking.service.FoodService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/foods")
@CrossOrigin(origins = "*")
public class FoodController {

    private final FoodService foodService;

    public FoodController(FoodService foodService) {
        this.foodService = foodService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FoodItem>>> getAllFood(@RequestParam(required = false) String category) {
        if (category != null && !category.isBlank()) {
            return ResponseEntity.ok(ApiResponse.ok(foodService.getByCategory(category)));
        }
        return ResponseEntity.ok(ApiResponse.ok(foodService.getAllFood()));
    }
}

