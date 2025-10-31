package com.universidad.vitaltech.repository;

import com.universidad.vitaltech.model.Diagnostico;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Diagnostico
 * Incluye queries con $lookup para relacionar con Cita y Usuario
 */
@Repository
public interface DiagnosticoRepository extends MongoRepository<Diagnostico, String> {
    
    // Buscar diagnóstico por cita
    Optional<Diagnostico> findByCitaId(String citaId);
    
    // Buscar diagnósticos por paciente
    List<Diagnostico> findByPacienteId(String pacienteId);
    
    // Buscar diagnósticos por doctor
    List<Diagnostico> findByDoctorId(String doctorId);
    
    // Buscar diagnósticos por paciente ordenados por fecha (más reciente primero)
    @Query(value = "{ 'pacienteId': ?0 }", sort = "{ 'fechaCreacion': -1 }")
    List<Diagnostico> findByPacienteIdOrderByFechaCreacionDesc(String pacienteId);
    
    // Obtener el último diagnóstico de un paciente
    @Query(value = "{ 'pacienteId': ?0 }", sort = "{ 'fechaCreacion': -1 }")
    Optional<Diagnostico> findUltimoDiagnosticoByPacienteId(String pacienteId);
    
    // LOOKUP: Obtener diagnóstico con información de la cita y usuarios
    @Aggregation(pipeline = {
        "{ $match: { '_id': { $oid: ?0 } } }",
        "{ $lookup: { from: 'citas', localField: 'citaId', foreignField: '_id', as: 'cita' } }",
        "{ $lookup: { from: 'usuarios', localField: 'pacienteId', foreignField: '_id', as: 'paciente' } }",
        "{ $lookup: { from: 'usuarios', localField: 'doctorId', foreignField: '_id', as: 'doctor' } }",
        "{ $unwind: { path: '$cita', preserveNullAndEmptyArrays: true } }",
        "{ $unwind: '$paciente' }",
        "{ $unwind: '$doctor' }"
    })
    Optional<Object> findDiagnosticoConDetallesById(String id);
    
    // LOOKUP: Obtener diagnósticos de un paciente con información completa
    @Aggregation(pipeline = {
        "{ $match: { 'pacienteId': ?0 } }",
        "{ $lookup: { from: 'citas', localField: 'citaId', foreignField: '_id', as: 'cita' } }",
        "{ $lookup: { from: 'usuarios', localField: 'pacienteId', foreignField: '_id', as: 'paciente' } }",
        "{ $lookup: { from: 'usuarios', localField: 'doctorId', foreignField: '_id', as: 'doctor' } }",
        "{ $unwind: { path: '$cita', preserveNullAndEmptyArrays: true } }",
        "{ $unwind: '$paciente' }",
        "{ $unwind: '$doctor' }",
        "{ $sort: { 'fechaCreacion': -1 } }"
    })
    List<Object> findDiagnosticosConDetallesByPacienteId(String pacienteId);
    
    // LOOKUP: Obtener diagnósticos de un doctor con información completa
    @Aggregation(pipeline = {
        "{ $match: { 'doctorId': ?0 } }",
        "{ $lookup: { from: 'citas', localField: 'citaId', foreignField: '_id', as: 'cita' } }",
        "{ $lookup: { from: 'usuarios', localField: 'pacienteId', foreignField: '_id', as: 'paciente' } }",
        "{ $lookup: { from: 'usuarios', localField: 'doctorId', foreignField: '_id', as: 'doctor' } }",
        "{ $unwind: { path: '$cita', preserveNullAndEmptyArrays: true } }",
        "{ $unwind: '$paciente' }",
        "{ $unwind: '$doctor' }",
        "{ $sort: { 'fechaCreacion': -1 } }"
    })
    List<Object> findDiagnosticosConDetallesByDoctorId(String doctorId);
    
    // LOOKUP: Obtener el último diagnóstico de un paciente con detalles completos
    @Aggregation(pipeline = {
        "{ $match: { 'pacienteId': ?0 } }",
        "{ $sort: { 'fechaCreacion': -1 } }",
        "{ $limit: 1 }",
        "{ $lookup: { from: 'citas', localField: 'citaId', foreignField: '_id', as: 'cita' } }",
        "{ $lookup: { from: 'usuarios', localField: 'pacienteId', foreignField: '_id', as: 'paciente' } }",
        "{ $lookup: { from: 'usuarios', localField: 'doctorId', foreignField: '_id', as: 'doctor' } }",
        "{ $unwind: { path: '$cita', preserveNullAndEmptyArrays: true } }",
        "{ $unwind: '$paciente' }",
        "{ $unwind: '$doctor' }"
    })
    Optional<Object> findUltimoDiagnosticoConDetallesByPacienteId(String pacienteId);
    
    // Verificar si existe diagnóstico para una cita
    boolean existsByCitaId(String citaId);
    
    // Contar diagnósticos por doctor
    long countByDoctorId(String doctorId);
    
    // Contar diagnósticos por paciente
    long countByPacienteId(String pacienteId);
}