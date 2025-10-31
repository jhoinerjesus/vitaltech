package com.universidad.vitaltech.repository;

import com.universidad.vitaltech.model.Cita;
import com.universidad.vitaltech.model.EstadoCita;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio para la entidad Cita
 * Incluye queries con $lookup para relacionar con Usuario
 */
@Repository
public interface CitaRepository extends MongoRepository<Cita, String> {
    
    // Buscar citas por paciente
    List<Cita> findByPacienteId(String pacienteId);
    
    // Buscar citas por doctor
    List<Cita> findByDoctorId(String doctorId);
    
    // Buscar citas por estado
    List<Cita> findByEstado(EstadoCita estado);
    
    // Buscar citas por paciente y estado
    List<Cita> findByPacienteIdAndEstado(String pacienteId, EstadoCita estado);
    
    // Buscar citas por doctor y estado
    List<Cita> findByDoctorIdAndEstado(String doctorId, EstadoCita estado);
    
    // Buscar citas por fecha
    @Query("{ 'horario.fecha': ?0 }")
    List<Cita> findByFecha(LocalDate fecha);
    
    // Buscar citas por doctor y fecha
    @Query("{ 'doctorId': ?0, 'horario.fecha': ?1 }")
    List<Cita> findByDoctorIdAndFecha(String doctorId, LocalDate fecha);
    
    // Buscar citas programadas de un doctor en una fecha
    @Query("{ 'doctorId': ?0, 'horario.fecha': ?1, 'estado': { $in: ['PROGRAMADA', 'CONFIRMADA'] } }")
    List<Cita> findCitasProgramadasByDoctorAndFecha(String doctorId, LocalDate fecha);
    
    // LOOKUP: Obtener citas con información del paciente y doctor
    @Aggregation(pipeline = {
        "{ $match: { 'pacienteId': ?0 } }",
        "{ $lookup: { from: 'usuarios', localField: 'pacienteId', foreignField: '_id', as: 'paciente' } }",
        "{ $lookup: { from: 'usuarios', localField: 'doctorId', foreignField: '_id', as: 'doctor' } }",
        "{ $unwind: '$paciente' }",
        "{ $unwind: '$doctor' }"
    })
    List<Object> findCitasConDetallesByPacienteId(String pacienteId);
    
    // LOOKUP: Obtener citas con información del paciente y doctor por doctorId
    @Aggregation(pipeline = {
        "{ $match: { 'doctorId': ?0 } }",
        "{ $lookup: { from: 'usuarios', localField: 'pacienteId', foreignField: '_id', as: 'paciente' } }",
        "{ $lookup: { from: 'usuarios', localField: 'doctorId', foreignField: '_id', as: 'doctor' } }",
        "{ $unwind: '$paciente' }",
        "{ $unwind: '$doctor' }",
        "{ $sort: { 'horario.fecha': -1, 'horario.horaInicio': -1 } }"
    })
    List<Object> findCitasConDetallesByDoctorId(String doctorId);
    
    // LOOKUP: Obtener citas por fecha con detalles
    @Aggregation(pipeline = {
        "{ $match: { 'horario.fecha': ?0 } }",
        "{ $lookup: { from: 'usuarios', localField: 'pacienteId', foreignField: '_id', as: 'paciente' } }",
        "{ $lookup: { from: 'usuarios', localField: 'doctorId', foreignField: '_id', as: 'doctor' } }",
        "{ $unwind: '$paciente' }",
        "{ $unwind: '$doctor' }",
        "{ $sort: { 'horario.horaInicio': 1 } }"
    })
    List<Object> findCitasConDetallesByFecha(String fecha);
    
    // Contar citas por doctor y estado
    long countByDoctorIdAndEstado(String doctorId, EstadoCita estado);
    
    // Contar citas por paciente y estado
    long countByPacienteIdAndEstado(String pacienteId, EstadoCita estado);
}