package com.example.deskscheduler.dto;

import com.example.deskscheduler.entity.Room;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Room summary embedded inside a desk response")
public record RoomSummary(
        @Schema(example = "1") Long id,
        @Schema(example = "Room 101") String name,
        FloorSummary floor) {

    public static RoomSummary from(Room room) {
        return new RoomSummary(room.getId(), room.getName(), FloorSummary.from(room.getFloor()));
    }
}
