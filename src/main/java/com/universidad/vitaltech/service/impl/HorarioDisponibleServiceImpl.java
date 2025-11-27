package com.universidad.vitaltech.service.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.universidad.vitaltech.model.HorarioDisponible;
import com.universidad.vitaltech.repository.HorarioDisponibleRepository;
import com.universidad.vitaltech.service.HorarioDisponibleService;

@Service
public class HorarioDisponibleServiceImpl implements HorarioDisponibleService {

    @Autowired
    private HorarioDisponibleRepository horarioDisponibleRepository;

    // CRUD

    @Override
    public HorarioDisponible guardar(HorarioDisponible horario) {
        if (!horario.validarHorario()) {
            throw new RuntimeException("El horario de inicio debe ser antes del horario de fin");
        }

        if (horario.getDuracionCita() == null || horario.getDuracionCita() > 30) {
            throw new RuntimeException("La duración de la cita debe ser máximo 30 minutos");
        }

        return horarioDisponibleRepository.save(horario);
    }

    @Override
    public Optional<HorarioDisponible> buscarPorId(String id) {
        return horarioDisponibleRepository.findById(id);
    }

    @Override
    public List<HorarioDisponible> listarTodos() {
        return horarioDisponibleRepository.findAll();
    }

    @Override
    public void eliminar(String id) {
        horarioDisponibleRepository.deleteById(id);
    }

    // CONSULTAS POR DOCTOR Y DÍA

    @Override
    public List<HorarioDisponible> listarPorDoctor(String doctorId) {
        return horarioDisponibleRepository.findByDoctorId(doctorId);
    }

    @Override
    public List<HorarioDisponible> listarActivosPorDoctor(String doctorId) {
        List<HorarioDisponible> lista = horarioDisponibleRepository.findByDoctorIdAndActivoTrue(doctorId);

        // Ordenar por día (1=lunes, 7=domingo)
        lista.sort((a, b) -> Integer.compare(
                a.getDiaSemana().getValue(),
                b.getDiaSemana().getValue()));

        return lista;
    }

    @Override
    public Optional<HorarioDisponible> buscarPorDoctorYDia(String doctorId, DayOfWeek diaSemana) {
        List<HorarioDisponible> horarios = horarioDisponibleRepository.findByDoctorIdAndDiaSemanaAndActivoTrue(doctorId,
                diaSemana);
        return horarios.isEmpty() ? Optional.empty() : Optional.of(horarios.get(0));
    }

    @Override
    public List<HorarioDisponible> listarPorDia(DayOfWeek diaSemana) {
        return horarioDisponibleRepository.findByDiaSemanaAndActivoTrue(diaSemana);
    }

    @Override
    public List<HorarioDisponible> listarActivosPorDia(DayOfWeek diaSemana) {
        return horarioDisponibleRepository.findByDiaSemanaAndActivoTrue(diaSemana);
    }

    @Override
    public List<HorarioDisponible> listarActivos() {
        return horarioDisponibleRepository.findByActivoTrue();
    }

    // --------------------------------------------------
    // ACCIONES DE ESTADO Y FECHAS BLOQUEADAS
    // --------------------------------------------------

    @Override
    public void cambiarEstado(String id, boolean activo) {
        buscarPorId(id).ifPresent(horario -> {
            horario.setActivo(activo);
            horarioDisponibleRepository.save(horario);
        });
    }

    @Override
    public void agregarFechaNoDisponible(String id, LocalDate fecha) {
        buscarPorId(id).ifPresent(horario -> {
            horario.agregarFechaNoDisponible(fecha);
            horarioDisponibleRepository.save(horario);
        });
    }

    // --------------------------------------------------
    // HORARIOS GENERADOS
    // --------------------------------------------------

    @Override
    public List<LocalTime> obtenerHorariosDisponibles(String horarioId) {
        return buscarPorId(horarioId)
                .map(HorarioDisponible::generarHorariosDisponibles)
                .orElse(List.of());
    }

    @Override
    public boolean existeHorarioPorDoctorYDia(String doctorId, DayOfWeek diaSemana) {
        return horarioDisponibleRepository.existsByDoctorIdAndDiaSemana(doctorId, diaSemana);
    }

    // CONSULTAS CON DETALLES

    @Override
    public List<Object> obtenerHorariosActivosConDetalles() {
        return horarioDisponibleRepository.findHorariosActivosConDetallesDoctor();
    }

    @Override
    public List<Object> obtenerHorariosPorDoctorConDetalles(String doctorId) {
        return horarioDisponibleRepository.findHorariosConDetallesByDoctorId(doctorId);
    }

    @Override
    public List<Object> obtenerHorariosPorDiaConDetalles(DayOfWeek diaSemana) {
        return horarioDisponibleRepository.findHorariosConDetallesByDiaSemana(diaSemana.toString());
    }

    @Override
    public long contarHorariosActivosPorDoctor(String doctorId) {
        return horarioDisponibleRepository.countByDoctorIdAndActivoTrue(doctorId);
    }

    // VALIDACIONES PARA CITAS

    @Override
    public List<LocalTime> obtenerHorariosDisponibles(String doctorId, LocalDate fecha) {
        List<LocalTime> horariosDisponibles = new ArrayList<>();
        DayOfWeek diaSemana = fecha.getDayOfWeek();

        Optional<HorarioDisponible> horarioOpt = buscarPorDoctorYDia(doctorId, diaSemana);

        if (horarioOpt.isEmpty()) {
            return horariosDisponibles;
        }

        HorarioDisponible horario = horarioOpt.get();

        if (!horario.estaDisponibleEnFecha(fecha)) {
            return horariosDisponibles;
        }

        return horario.generarHorariosDisponibles();
    }

    @Override
    public boolean existeHorarioDisponible(String doctorId, LocalDate fecha, LocalTime horaInicio) {
        DayOfWeek diaSemana = fecha.getDayOfWeek();
        Optional<HorarioDisponible> horarioOpt = buscarPorDoctorYDia(doctorId, diaSemana);

        if (horarioOpt.isEmpty()) {
            return false;
        }

        HorarioDisponible horario = horarioOpt.get();

        if (!horario.estaDisponibleEnFecha(fecha)) {
            return false;
        }

        if (horaInicio.isBefore(horario.getHoraInicio()) || horaInicio.isAfter(horario.getHoraFin())) {
            return false;
        }

        return horario.generarHorariosDisponibles().contains(horaInicio);
    }

    @Override
    public List<HorarioDisponible> listarActivosPorDoctorYDia(String doctorId, DayOfWeek dia) {
        List<HorarioDisponible> lista = horarioDisponibleRepository.findByDoctorIdAndDiaSemanaAndActivoTrue(doctorId,
                dia);

        return lista != null ? lista : List.of();
    }

    @Override
    public boolean validarHorarioCita(
            String doctorId, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {

        if (!existeHorarioDisponible(doctorId, fecha, horaInicio)) {
            return false;
        }

        long duracion = java.time.Duration.between(horaInicio, horaFin).toMinutes();
        if (duracion <= 0 || duracion > 30) {
            return false;
        }

        DayOfWeek diaSemana = fecha.getDayOfWeek();
        Optional<HorarioDisponible> horarioOpt = buscarPorDoctorYDia(doctorId, diaSemana);

        if (horarioOpt.isEmpty()) {
            return false;
        }

        HorarioDisponible horario = horarioOpt.get();

        return !horaFin.isAfter(horario.getHoraFin());
    }
}
