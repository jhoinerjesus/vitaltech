package com.universidad.vitaltech.service;

import com.universidad.vitaltech.model.Diagnostico;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz del servicio de Diagnostico
 */
public interface DiagnosticoService {
    
    // CRUD básico
    Diagnostico guardar(Diagnostico diagnostico);
    Optional<Diagnostico> buscarPorId(String id);
    List<Diagnostico> listarTodos();
    void eliminar(String id);
    
    // Búsquedas específicas
    Optional<Diagnostico> buscarPorCita(String citaId);
    List<Diagnostico> listarPorPaciente(String pacienteId);
    List<Diagnostico> listarPorDoctor(String doctorId);
    Optional<Diagnostico> buscarUltimoDiagnosticoPorPaciente(String pacienteId);
    
    // Validaciones
    boolean existeDiagnosticoParaCita(String citaId);
    
    // Queries con lookup
    Optional<Object> obtenerDiagnosticoConDetalles(String id);
    List<Object> obtenerDiagnosticosConDetallesPorPaciente(String pacienteId);
    List<Object> obtenerDiagnosticosConDetallesPorDoctor(String doctorId);
    Optional<Object> obtenerUltimoDiagnosticoConDetallesPorPaciente(String pacienteId);
    
    // Estadísticas
    long contarDiagnosticosPorDoctor(String doctorId);
    long contarDiagnosticosPorPaciente(String pacienteId);
}