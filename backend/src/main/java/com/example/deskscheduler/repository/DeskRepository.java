package com.example.deskscheduler.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.deskscheduler.entity.Desk;

public interface DeskRepository extends JpaRepository<Desk, Long> {}
