package com.cinema.booking.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record HoldSeatRequest(
    @NotNull Long showtimeId,
    @NotEmpty List<String> seatCodes,
    String customerName
) {}

