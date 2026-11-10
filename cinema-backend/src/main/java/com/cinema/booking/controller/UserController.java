package com.cinema.booking.controller;

import com.cinema.booking.dto.ApiResponse;
import com.cinema.booking.model.User;
import com.cinema.booking.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<User>> getProfile() {
        User user = userRepository.findAll().stream().findFirst()
            .orElse(new User("Randi Pratama", "randi@tixid.example.com", "081234567890", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=150&q=80", 8399));
        return ResponseEntity.ok(ApiResponse.ok("Data profil pengguna", user));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<User>> login(@RequestBody Map<String, String> body) {
        String name = body.getOrDefault("name", "Randi Pratama");
        String email = body.getOrDefault("email", "randi@tixid.example.com");

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User(name, email, "081234567890", "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&q=80", 8399);
            return userRepository.save(newUser);
        });

        return ResponseEntity.ok(ApiResponse.ok("Login berhasil!", user));
    }
}

