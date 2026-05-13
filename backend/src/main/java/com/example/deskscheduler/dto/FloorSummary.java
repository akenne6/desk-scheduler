package com.example.deskscheduler.dto;

import com.example.deskscheduler.entity.Floor;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Floor summary embedded inside a room/desk response")
public record FloorSummary(
        @Schema(example = "1") Long id, @Schema(example = "Floor 1") String name) {

    public static FloorSummary from(Floor floor) {
        return new FloorSummary(floor.getId(), floor.getName());
    }
}
