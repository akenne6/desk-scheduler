package com.example.deskscheduler.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.deskscheduler.TestcontainersConfig;
import com.example.deskscheduler.dto.DeskResponse;
import com.example.deskscheduler.entity.DeskType;

@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfig.class)
class DeskApiIntegrationTest {

    @Autowired TestRestTemplate restTemplate;

    @Test
    void getDesks_returnsSeededDeskTreeWithNullOccupancy() {
        ResponseEntity<List<DeskResponse>> response =
                restTemplate.exchange(
                        "/api/desks",
                        org.springframework.http.HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<DeskResponse>>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<DeskResponse> desks = response.getBody();
        assertThat(desks).isNotNull().hasSize(6);

        DeskResponse first =
                desks.stream().filter(d -> "Desk 101-A".equals(d.name())).findFirst().orElseThrow();
        assertThat(first.type()).isEqualTo(DeskType.STANDARD);
        assertThat(first.occupiedBy()).isNull();
        assertThat(first.room().name()).isEqualTo("Room 101");
        assertThat(first.room().floor().name()).isEqualTo("Floor 1");

        assertThat(desks).extracting(DeskResponse::occupiedBy).containsOnlyNulls();
        assertThat(desks)
                .extracting(d -> d.type())
                .contains(DeskType.STANDARD, DeskType.STANDING, DeskType.CONFERENCE);
    }
}
