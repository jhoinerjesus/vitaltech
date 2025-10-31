package com.universidad.vitaltech.service.impl;

import com.universidad.vitaltech.model.Diagnostico;
import com.universidad.vitaltech.repository.DiagnosticoRepository;
import com.universidad.vitaltech.service.DiagnosticoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio de Diagnostico
 */
@Service
public class DiagnosticoServiceImpl implements DiagnosticoService {
    
    @Autowired
    private DiagnosticoRepository diagnosticoRepository;
    
    @Override
    public Diagnostico guardar(Diagnostico diagnostico) {
        // Establecer fecha de creación si es nuevo
        if (diagnostico.getId() == null) {
            diagnostico.setFechaCreacion(LocalDateTime.now());
        } else {
            diagnostico.setFechaActualizacion(LocalDateTime.now());
        }
        
        return diagnosticoRepository.save(diagnostico);
    }
    
    @Override
    public Optional<Diagnostico> buscarPorId(String id) {
        return diagnosticoRepository.findById(id);
    }
    
    @Override
    public List<Diagnostico> listarTodos() {
        return diagnosticoRepository.findAll();
    }
    
    @Override
    public void eliminar(String id) {
        diagnosticoRepository.deleteById(id);
    }
    
    @Override
    public Optional<Diagnostico> buscarPorCita(String citaId) {
        return diagnosticoRepository.findByCitaId(citaId);
    }
    
    @Override
    public List<Diagnostico> listarPorPaciente(String pacienteId) {
        return diagnosticoRepository.findByPacienteIdOrderByFechaCreacionDesc(pacienteId);
    }
    
    @Override
    public List<Diagnostico> listarPorDoctor(String doctorId) {
        return diagnosticoRepository.findByDoctorId(doctorId);
    }
    
    @Override
    public Optional<Diagnostico> buscarUltimoDiagnosticoPorPaciente(String pacienteId) {
        return diagnosticoRepository.findUltimoDiagnosticoByPacienteId(pacienteId);
    }
    
    @Override
    public boolean existeDiagnosticoParaCita(String citaId) {
        return diagnosticoRepository.existsByCitaId(citaId);
    }
    
    @Override
    public Optional<Object> obtenerDiagnosticoConDetalles(String id) {
        return diagnosticoRepository.findDiagnosticoConDetallesById(id);
    }
    
    @Override
    public List<Object> obtenerDiagnosticosConDetallesPorPaciente(String pacienteId) {
        return diagnosticoRepository.findDiagnosticosConDetallesByPacienteId(pacienteId);
    }
    
    @Override
    public List<Object> obtenerDiagnosticosConDetallesPorDoctor(String doctorId) {
        return diagnosticoRepository.findDiagnosticosConDetallesByDoctorId(doctorId);
    }
    
    @Override
    public Optional<Object> obtenerUltimoDiagnosticoConDetallesPorPaciente(String pacienteId) {
        return diagnosticoRepository.findUltimoDiagnosticoConDetallesByPacienteId(pacienteId);
    }
    
    @Override
    public long contarDiagnosticosPorDoctor(String doctorId) {
        return diagnosticoRepository.countByDoctorId(doctorId);
    }
    
    @Override
    public long contarDiagnosticosPorPaciente(String pacienteId) {
        return diagnosticoRepository.countByPacienteId(pacienteId);
    }
}
