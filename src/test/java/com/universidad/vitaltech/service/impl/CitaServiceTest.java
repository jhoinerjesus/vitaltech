package com.universidad.vitaltech.service.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.universidad.vitaltech.model.Cita;
import com.universidad.vitaltech.model.EstadoCita;
import com.universidad.vitaltech.model.embedded.Horario;
import com.universidad.vitaltech.repository.CitaRepository;

public class CitaServiceTest {

    private CitaServiceImpl citaService;
    private CitaRepository citaRepository;

    @BeforeEach
    public void setUp() {
        citaRepository = mock(CitaRepository.class);
        citaService = new CitaServiceImpl();
        ReflectionTestUtils.setField(citaService, "citaRepository", citaRepository);
        ReflectionTestUtils.setField(citaService, "appTimezone", "America/Bogota");
    }

    @Test
    public void testPuedeSerAtendida() {
        // Setup current time in Bogota
        ZoneId zoneId = ZoneId.of("America/Bogota");
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        LocalDate today = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime();

        // Create a cita that is happening NOW
        Cita cita = new Cita();
        cita.setId("1");
        cita.setEstado(EstadoCita.CONFIRMADA);

        Horario horario = new Horario();
        horario.setFecha(today);
        // Start 10 mins ago, end in 20 mins
        horario.setHoraInicio(currentTime.minusMinutes(10));
        horario.setHoraFin(currentTime.plusMinutes(20));
        cita.setHorario(horario);

        // Should be true
        assertTrue(citaService.puedeSerAtendida(cita), "Should be attendable now");
    }

    @Test
    public void testNoPuedeSerAtendidaFuturo() {
        ZoneId zoneId = ZoneId.of("America/Bogota");
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        LocalDate today = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime();

        Cita cita = new Cita();
        cita.setId("2");
        cita.setEstado(EstadoCita.CONFIRMADA);

        Horario horario = new Horario();
        horario.setFecha(today);
        // Starts in 2 hours
        horario.setHoraInicio(currentTime.plusHours(2));
        horario.setHoraFin(currentTime.plusHours(2).plusMinutes(30));
        cita.setHorario(horario);

        // Should be false
        assertFalse(citaService.puedeSerAtendida(cita), "Should NOT be attendable (future)");
    }

    @Test
    public void testNoPuedeSerAtendidaPasado() {
        ZoneId zoneId = ZoneId.of("America/Bogota");
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        LocalDate today = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime();

        Cita cita = new Cita();
        cita.setId("3");
        cita.setEstado(EstadoCita.CONFIRMADA);

        Horario horario = new Horario();
        horario.setFecha(today);
        // Ended 3 hours ago
        horario.setHoraInicio(currentTime.minusHours(4));
        horario.setHoraFin(currentTime.minusHours(3).minusMinutes(30));
        cita.setHorario(horario);

        // Should be false (window is +2 hours after end)
        assertFalse(citaService.puedeSerAtendida(cita), "Should NOT be attendable (past)");
    }
}
