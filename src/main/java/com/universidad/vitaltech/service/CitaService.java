package com.universidad.vitaltech.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.universidad.vitaltech.model.Cita;
import com.universidad.vitaltech.model.EstadoCita;

/**
 Interfaz del servicio de Cita
 */
public interface CitaService {
    
    Cita guardar(Cita cita);
    Optional<Cita> buscarPorId(String id);
    List<Cita> listarTodas();
    void eliminar(String id);
    
    // Busquedas por paciente
    List<Cita> listarPorPaciente(String pacienteId);
    List<Cita> listarPorPacienteYEstado(String pacienteId, EstadoCita estado);
    
    // Busquedas por doctor
    List<Cita> listarPorDoctor(String doctorId);
    List<Cita> listarPorDoctorYEstado(String doctorId, EstadoCita estado);
    List<Cita> listarPorDoctorYFecha(String doctorId, LocalDate fecha);
    List<Cita> listarCitasProgramadasPorDoctorYFecha(String doctorId, LocalDate fecha);
    
    // Busquedas por fecha
    List<Cita> listarPorFecha(LocalDate fecha);
    
    // Busquedas por estado
    List<Cita> listarPorEstado(EstadoCita estado);
    
    // Operaciones de estado
    Cita confirmarCita(String citaId);
    Cita completarCita(String citaId);
    Cita cancelarCita(String citaId, String usuarioId, String motivo);
    
    // Validaciones
    boolean existeDisponibilidad(String doctorId, LocalDate fecha, String horaInicio);
    
    boolean puedeSerAtendida(Cita cita);
    String obtenerMensajeBloqueo(Cita cita);
    

    List<Object> obtenerCitasConDetallesPorPaciente(String pacienteId);
    List<Object> obtenerCitasConDetallesPorDoctor(String doctorId);
    List<Object> obtenerCitasConDetallesPorFecha(LocalDate fecha);
    
    // Estadisticas
    long contarCitasPorDoctorYEstado(String doctorId, EstadoCita estado);
    long contarCitasPorPacienteYEstado(String pacienteId, EstadoCita estado);

    
}