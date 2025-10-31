package com.universidad.vitaltech.service;

import com.universidad.vitaltech.model.Cita;
import com.universidad.vitaltech.model.EstadoCita;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz del servicio de Cita
 */
public interface CitaService {
    
    // CRUD básico
    Cita guardar(Cita cita);
    Optional<Cita> buscarPorId(String id);
    List<Cita> listarTodas();
    void eliminar(String id);
    
    // Búsquedas por paciente
    List<Cita> listarPorPaciente(String pacienteId);
    List<Cita> listarPorPacienteYEstado(String pacienteId, EstadoCita estado);
    
    // Búsquedas por doctor
    List<Cita> listarPorDoctor(String doctorId);
    List<Cita> listarPorDoctorYEstado(String doctorId, EstadoCita estado);
    List<Cita> listarPorDoctorYFecha(String doctorId, LocalDate fecha);
    List<Cita> listarCitasProgramadasPorDoctorYFecha(String doctorId, LocalDate fecha);
    
    // Búsquedas por fecha
    List<Cita> listarPorFecha(LocalDate fecha);
    
    // Búsquedas por estado
    List<Cita> listarPorEstado(EstadoCita estado);
    
    // Operaciones de estado
    Cita confirmarCita(String citaId);
    Cita completarCita(String citaId);
    Cita cancelarCita(String citaId, String usuarioId, String motivo);
    
    // Validaciones
    boolean existeDisponibilidad(String doctorId, LocalDate fecha, String horaInicio);
    
    // Queries con lookup
    List<Object> obtenerCitasConDetallesPorPaciente(String pacienteId);
    List<Object> obtenerCitasConDetallesPorDoctor(String doctorId);
    List<Object> obtenerCitasConDetallesPorFecha(LocalDate fecha);
    
    // Estadísticas
    long contarCitasPorDoctorYEstado(String doctorId, EstadoCita estado);
    long contarCitasPorPacienteYEstado(String pacienteId, EstadoCita estado);
}