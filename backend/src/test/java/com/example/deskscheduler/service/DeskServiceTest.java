package com.example.deskscheduler.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.deskscheduler.dto.DeskResponse;
import com.example.deskscheduler.entity.Desk;
import com.example.deskscheduler.entity.DeskType;
import com.example.deskscheduler.entity.Floor;
import com.example.deskscheduler.entity.Room;
import com.example.deskscheduler.repository.DeskRepository;

@ExtendWith(MockitoExtension.class)
class DeskServiceTest {

    @Mock DeskRepository deskRepository;

    @InjectMocks DeskService deskService;

    @Test
    void listDesks_mapsEntityTreeToDtoWithNullOccupiedBy() {
        Floor floor = new Floor();
        floor.setId(1L);
        floor.setName("Floor 1");

        Room room = new Room();
        room.setId(10L);
        room.setName("Room 101");
        room.setFloor(floor);

        Desk desk = new Desk();
        desk.setId(100L);
        desk.setName("Desk 101-A");
        desk.setType(DeskType.STANDARD);
        desk.setRoom(room);

        when(deskRepository.findAllWithRoomAndFloor()).thenReturn(List.of(desk));

        List<DeskResponse> result = deskService.listDesks();

        assertThat(result).hasSize(1);
        DeskResponse response = result.get(0);
        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.name()).isEqualTo("Desk 101-A");
        assertThat(response.type()).isEqualTo(DeskType.STANDARD);
        assertThat(response.occupiedBy()).isNull();
        assertThat(response.room().id()).isEqualTo(10L);
        assertThat(response.room().name()).isEqualTo("Room 101");
        assertThat(response.room().floor().id()).isEqualTo(1L);
        assertThat(response.room().floor().name()).isEqualTo("Floor 1");
    }
}
