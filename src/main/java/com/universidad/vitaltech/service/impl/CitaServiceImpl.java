package com.universidad.vitaltech.service.impl;

import com.universidad.vitaltech.model.Cita;
import com.universidad.vitaltech.model.EstadoCita;
import com.universidad.vitaltech.repository.CitaRepository;
import com.universidad.vitaltech.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio de Cita
 */
@Service
public class CitaServiceImpl implements CitaService {
    
    @Autowired
    private CitaRepository citaRepository;
    
    @Override
    public Cita guardar(Cita cita) {
        // Validar que la duración de la cita no exceda 30 minutos
        if (!cita.getHorario().validarDuracion()) {
            throw new RuntimeException("La duración de la cita debe ser máximo 30 minutos");
        }
        
        // Establecer fecha de creación si es nueva
        if (cita.getId() == null) {
            cita.setFechaCreacion(LocalDateTime.now());
            cita.setEstado(EstadoCita.PROGRAMADA);
        } else {
            cita.setFechaActualizacion(LocalDateTime.now());
        }
        
        return citaRepository.save(cita);
    }
    
    @Override
    public Optional<Cita> buscarPorId(String id) {
        return citaRepository.findById(id);
    }
    
    @Override
    public List<Cita> listarTodas() {
        return citaRepository.findAll();
    }
    
    @Override
    public void eliminar(String id) {
        citaRepository.deleteById(id);
    }
    
    @Override
    public List<Cita> listarPorPaciente(String pacienteId) {
        return citaRepository.findByPacienteId(pacienteId);
    }
    
    @Override
    public List<Cita> listarPorPacienteYEstado(String pacienteId, EstadoCita estado) {
        return citaRepository.findByPacienteIdAndEstado(pacienteId, estado);
    }
    
    @Override
    public List<Cita> listarPorDoctor(String doctorId) {
        return citaRepository.findByDoctorId(doctorId);
    }
    
    @Override
    public List<Cita> listarPorDoctorYEstado(String doctorId, EstadoCita estado) {
        return citaRepository.findByDoctorIdAndEstado(doctorId, estado);
    }
    
    @Override
    public List<Cita> listarPorDoctorYFecha(String doctorId, LocalDate fecha) {
        return citaRepository.findByDoctorIdAndFecha(doctorId, fecha);
    }
    
    @Override
    public List<Cita> listarCitasProgramadasPorDoctorYFecha(String doctorId, LocalDate fecha) {
        return citaRepository.findCitasProgramadasByDoctorAndFecha(doctorId, fecha);
    }
    
    @Override
    public List<Cita> listarPorFecha(LocalDate fecha) {
        return citaRepository.findByFecha(fecha);
    }
    
    @Override
    public List<Cita> listarPorEstado(EstadoCita estado) {
        return citaRepository.findByEstado(estado);
    }
    
    @Override
    public Cita confirmarCita(String citaId) {
        Optional<Cita> citaOpt = buscarPorId(citaId);
        if (citaOpt.isPresent()) {
            Cita cita = citaOpt.get();
            cita.confirmar();
            return citaRepository.save(cita);
        }
        throw new RuntimeException("Cita no encontrada");
    }
    
    @Override
    public Cita completarCita(String citaId) {
        Optional<Cita> citaOpt = buscarPorId(citaId);
        if (citaOpt.isPresent()) {
            Cita cita = citaOpt.get();
            cita.completar();
            return citaRepository.save(cita);
        }
        throw new RuntimeException("Cita no encontrada");
    }
    
    @Override
    public Cita cancelarCita(String citaId, String usuarioId, String motivo) {
        Optional<Cita> citaOpt = buscarPorId(citaId);
        if (citaOpt.isPresent()) {
            Cita cita = citaOpt.get();
            cita.cancelar(usuarioId, motivo);
            return citaRepository.save(cita);
        }
        throw new RuntimeException("Cita no encontrada");
    }
    
    @Override
    public boolean existeDisponibilidad(String doctorId, LocalDate fecha, String horaInicio) {
        List<Cita> citasExistentes = listarCitasProgramadasPorDoctorYFecha(doctorId, fecha);
        
        // Verificar si existe conflicto de horario
        for (Cita cita : citasExistentes) {
            String horaInicioCita = cita.getHorario().getHoraInicio().toString();
            if (horaInicioCita.equals(horaInicio)) {
                return false; // Ya existe una cita en ese horario
            }
        }
        
        return true; // Está disponible
    }
    
    @Override
    public List<Object> obtenerCitasConDetallesPorPaciente(String pacienteId) {
        return citaRepository.findCitasConDetallesByPacienteId(pacienteId);
    }
    
    @Override
    public List<Object> obtenerCitasConDetallesPorDoctor(String doctorId) {
        return citaRepository.findCitasConDetallesByDoctorId(doctorId);
    }
    
    @Override
    public List<Object> obtenerCitasConDetallesPorFecha(LocalDate fecha) {
        return citaRepository.findCitasConDetallesByFecha(fecha.toString());
    }
    
    @Override
    public long contarCitasPorDoctorYEstado(String doctorId, EstadoCita estado) {
        return citaRepository.countByDoctorIdAndEstado(doctorId, estado);
    }
    
    @Override
    public long contarCitasPorPacienteYEstado(String pacienteId, EstadoCita estado) {
        return citaRepository.countByPacienteIdAndEstado(pacienteId, estado);
    }
}