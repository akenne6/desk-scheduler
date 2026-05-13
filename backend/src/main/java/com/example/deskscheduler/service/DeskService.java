package com.example.deskscheduler.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.deskscheduler.dto.DeskResponse;
import com.example.deskscheduler.repository.DeskRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeskService {

    private final DeskRepository deskRepository;

    @Transactional(readOnly = true)
    public List<DeskResponse> listDesks() {
        return deskRepository.findAllWithRoomAndFloor().stream().map(DeskResponse::from).toList();
    }
}
