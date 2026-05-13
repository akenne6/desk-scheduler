package com.example.deskscheduler.dto;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Current occupant of a desk; null when the desk is free")
public record OccupiedBy(
        @Schema(example = "Alice Johnson") String employeeName,
        @Schema(example = "2026-05-13T14:30:00Z") Instant checkedInAt) {}
