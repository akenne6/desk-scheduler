package com.example.deskscheduler.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.deskscheduler.dto.DeskResponse;
import com.example.deskscheduler.service.DeskService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/desks")
@RequiredArgsConstructor
@Tag(name = "Desks", description = "Browse desks and their current occupancy")
@CrossOrigin(origins = "http://localhost:4200")
public class DeskController {

    private final DeskService deskService;

    @Operation(
            summary = "List all desks",
            description =
                    "Returns every desk with its parent room and floor. In Phase 1, occupiedBy"
                            + " is always null; Phase 2 will populate it from the bookings table.")
    @ApiResponse(responseCode = "200", description = "List of desks returned")
    @GetMapping
    public List<DeskResponse> list() {
        return deskService.listDesks();
    }
}
