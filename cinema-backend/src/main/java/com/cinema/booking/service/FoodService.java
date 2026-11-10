package com.cinema.booking.service;

import com.cinema.booking.model.FoodItem;
import com.cinema.booking.repository.FoodItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class FoodService {

    private final FoodItemRepository foodItemRepository;

    public FoodService(FoodItemRepository foodItemRepository) {
        this.foodItemRepository = foodItemRepository;
    }

    public List<FoodItem> getAllFood() {
        return foodItemRepository.findAll();
    }

    public List<FoodItem> getByCategory(String category) {
        return foodItemRepository.findByCategory(category);
    }
}

