package com.example.deskscheduler.dto;

import com.example.deskscheduler.entity.Desk;
import com.example.deskscheduler.entity.DeskType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Desk with current occupancy and its parent room/floor")
public record DeskResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "Desk 101-A") String name,
        @Schema(example = "STANDARD") DeskType type,
        @Schema(description = "Current occupant; null when the desk is free") OccupiedBy occupiedBy,
        RoomSummary room) {

    /**
     * Phase 1 mapping: occupiedBy is always null. Phase 2 will replace this with a query that
     * left-joins the bookings table filtered by checked_out_at IS NULL.
     */
    public static DeskResponse from(Desk desk) {
        return new DeskResponse(
                desk.getId(),
                desk.getName(),
                desk.getType(),
                null,
                RoomSummary.from(desk.getRoom()));
    }
}
