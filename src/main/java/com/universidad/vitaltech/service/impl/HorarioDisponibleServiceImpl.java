package com.universidad.vitaltech.service.impl;

import com.universidad.vitaltech.model.HorarioDisponible;
import com.universidad.vitaltech.repository.HorarioDisponibleRepository;
import com.universidad.vitaltech.service.HorarioDisponibleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio de HorarioDisponible
 */
@Service
public class HorarioDisponibleServiceImpl implements HorarioDisponibleService {
    
    @Autowired
    private HorarioDisponibleRepository horarioDisponibleRepository;
    
    @Override
    public HorarioDisponible guardar(HorarioDisponible horario) {
        // Validar que el horario sea coherente
        if (!horario.validarHorario()) {
            throw new RuntimeException("El horario de inicio debe ser antes del horario de fin");
        }
        
        // Validar duración de cita
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
    
    @Override
    public List<HorarioDisponible> listarPorDoctor(String doctorId) {
        return horarioDisponibleRepository.findByDoctorId(doctorId);
    }
    
    @Override
    public List<HorarioDisponible> listarActivosPorDoctor(String doctorId) {
        return horarioDisponibleRepository.findByDoctorIdAndActivoTrue(doctorId);
    }
    
    @Override
    public Optional<HorarioDisponible> buscarPorDoctorYDia(String doctorId, DayOfWeek diaSemana) {
        return horarioDisponibleRepository.findByDoctorIdAndDiaSemanaAndActivoTrue(doctorId, diaSemana);
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
    
    @Override
    public void cambiarEstado(String id, boolean activo) {
        Optional<HorarioDisponible> horarioOpt = buscarPorId(id);
        if (horarioOpt.isPresent()) {
            HorarioDisponible horario = horarioOpt.get();
            horario.setActivo(activo);
            horarioDisponibleRepository.save(horario);
        }
    }
    
    @Override
    public void agregarFechaNoDisponible(String id, LocalDate fecha) {
        Optional<HorarioDisponible> horarioOpt = buscarPorId(id);
        if (horarioOpt.isPresent()) {
            HorarioDisponible horario = horarioOpt.get();
            horario.agregarFechaNoDisponible(fecha);
            horarioDisponibleRepository.save(horario);
        }
    }
    
    @Override
    public List<LocalTime> obtenerHorariosDisponibles(String horarioId) {
        Optional<HorarioDisponible> horarioOpt = buscarPorId(horarioId);
        if (horarioOpt.isPresent()) {
            return horarioOpt.get().generarHorariosDisponibles();
        }
        return List.of();
    }
    
    @Override
    public boolean existeHorarioPorDoctorYDia(String doctorId, DayOfWeek diaSemana) {
        return horarioDisponibleRepository.existsByDoctorIdAndDiaSemana(doctorId, diaSemana);
    }
    
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
}
