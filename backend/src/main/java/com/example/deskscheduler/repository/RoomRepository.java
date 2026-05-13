package com.example.deskscheduler.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.deskscheduler.entity.Room;

public interface RoomRepository extends JpaRepository<Room, Long> {}
