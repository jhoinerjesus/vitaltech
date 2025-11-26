package com.universidad.vitaltech.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import com.universidad.vitaltech.model.HorarioDisponible;


public interface HorarioDisponibleService {
    
    // crud
    HorarioDisponible guardar(HorarioDisponible horario);
    Optional<HorarioDisponible> buscarPorId(String id);
    List<HorarioDisponible> listarTodos();
    void eliminar(String id);
    
    // Busquedas por doctor
    List<HorarioDisponible> listarPorDoctor(String doctorId);
    List<HorarioDisponible> listarActivosPorDoctor(String doctorId);
    Optional<HorarioDisponible> buscarPorDoctorYDia(String doctorId, DayOfWeek diaSemana);
    
    // Búuquedas por día
    List<HorarioDisponible> listarPorDia(DayOfWeek diaSemana);
    List<HorarioDisponible> listarActivosPorDia(DayOfWeek diaSemana);
    
    // Horarios activos
    List<HorarioDisponible> listarActivos();
    
    // Operaciones de estado
    void cambiarEstado(String id, boolean activo);
    void agregarFechaNoDisponible(String id, LocalDate fecha);
    
    // Generación de horarios
    List<LocalTime> obtenerHorariosDisponibles(String horarioId);
    
    // Validaciones
    boolean existeHorarioPorDoctorYDia(String doctorId, DayOfWeek diaSemana);
    
    // Consultas con detalles
    List<Object> obtenerHorariosActivosConDetalles();
    List<Object> obtenerHorariosPorDoctorConDetalles(String doctorId);
    List<Object> obtenerHorariosPorDiaConDetalles(DayOfWeek diaSemana);
    
    // Estadísticas
    long contarHorariosActivosPorDoctor(String doctorId);
    
    // validaciones de citas
    
    /**
     * Obtiene todos los horarios disponibles de un doctor en una fecha específica
     * 
     * @param doctorId ID del doctor
     * @param fecha Fecha para la cual se quieren obtener horarios
     * @return Lista de horas disponibles (ej: [09:00, 09:30, 10:00, ...])
     */
    List<LocalTime> obtenerHorariosDisponibles(String doctorId, LocalDate fecha);
    
    /**
     * Verifica si un doctor tiene disponible un horario específico en una fecha
     * 
     * @param doctorId ID del doctor
     * @param fecha Fecha de la cita
     * @param horaInicio Hora de inicio de la cita
     * @return true si el doctor tiene ese horario disponible, false en caso contrario
     */
    boolean existeHorarioDisponible(String doctorId, LocalDate fecha, LocalTime horaInicio);
    
    /**
     * Obtiene los horarios activos de un doctor para un día específico
     * 
     * @param doctorId ID del doctor
     * @param dia Día de la semana
     * @return Lista de horarios disponibles
     */
    List<HorarioDisponible> listarActivosPorDoctorYDia(String doctorId, DayOfWeek dia);
    
    /**
     * Valida si un horario específico puede ser usado para una cita
     * 
     * @param doctorId ID del doctor
     * @param fecha Fecha de la cita
     * @param horaInicio Hora de inicio
     * @param horaFin Hora de fin
     * @return true si es válido, false en caso contrario
     */
    boolean validarHorarioCita(String doctorId, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin);
}