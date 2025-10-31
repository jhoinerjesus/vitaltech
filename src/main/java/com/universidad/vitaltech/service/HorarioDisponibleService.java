package com.universidad.vitaltech.service;

import com.universidad.vitaltech.model.HorarioDisponible;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz del servicio de HorarioDisponible
 */
public interface HorarioDisponibleService {
    
    // CRUD básico
    HorarioDisponible guardar(HorarioDisponible horario);
    Optional<HorarioDisponible> buscarPorId(String id);
    List<HorarioDisponible> listarTodos();
    void eliminar(String id);
    
    // Búsquedas por doctor
    List<HorarioDisponible> listarPorDoctor(String doctorId);
    List<HorarioDisponible> listarActivosPorDoctor(String doctorId);
    Optional<HorarioDisponible> buscarPorDoctorYDia(String doctorId, DayOfWeek diaSemana);
    
    // Búsquedas por día
    List<HorarioDisponible> listarPorDia(DayOfWeek diaSemana);
    List<HorarioDisponible> listarActivosPorDia(DayOfWeek diaSemana);
    
    // Búsquedas generales
    List<HorarioDisponible> listarActivos();
    
    // Operaciones especiales
    void cambiarEstado(String id, boolean activo);
    void agregarFechaNoDisponible(String id, LocalDate fecha);
    List<LocalTime> obtenerHorariosDisponibles(String horarioId);
    
    // Validaciones
    boolean existeHorarioPorDoctorYDia(String doctorId, DayOfWeek diaSemana);
    
    // Queries con lookup
    List<Object> obtenerHorariosActivosConDetalles();
    List<Object> obtenerHorariosPorDoctorConDetalles(String doctorId);
    List<Object> obtenerHorariosPorDiaConDetalles(DayOfWeek diaSemana);
    
    // Estadísticas
    long contarHorariosActivosPorDoctor(String doctorId);
}