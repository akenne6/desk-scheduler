package com.example.deskscheduler.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.deskscheduler.entity.Desk;

public interface DeskRepository extends JpaRepository<Desk, Long> {

    /**
     * Fetches every desk with its room and floor pre-joined. Avoids N+1 selects when mapping to
     * DeskResponse, which always reads room.floor.
     */
    @Query("SELECT d FROM Desk d JOIN FETCH d.room r JOIN FETCH r.floor")
    List<Desk> findAllWithRoomAndFloor();
}
