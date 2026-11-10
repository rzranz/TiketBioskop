package com.cinema.booking.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record HoldSeatRequest(
    Long showtimeId,
    @NotEmpty List<String> seatCodes,
    String customerName
) {}

