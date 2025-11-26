package com.universidad.vitaltech.repository;

import java.time.DayOfWeek;
import java.util.List;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.universidad.vitaltech.model.HorarioDisponible;

@Repository
public interface HorarioDisponibleRepository extends MongoRepository<HorarioDisponible, String> {
    
    // Buscar horarios por doctor
    List<HorarioDisponible> findByDoctorId(String doctorId);
    
    // Buscar horarios activos por doctor
    List<HorarioDisponible> findByDoctorIdAndActivoTrue(String doctorId);
    
    // Buscar horarios por doctor y día de la semana
    List<HorarioDisponible> findByDoctorIdAndDiaSemana(String doctorId, DayOfWeek diaSemana);
    
    // CAMBIO: Ahora devuelve List en lugar de Optional
    List<HorarioDisponible> findByDoctorIdAndDiaSemanaAndActivoTrue(String doctorId, DayOfWeek diaSemana);
    
    // Buscar todos los horarios activos por día de la semana
    List<HorarioDisponible> findByDiaSemanaAndActivoTrue(DayOfWeek diaSemana);
    
    // Buscar horarios activos
    List<HorarioDisponible> findByActivoTrue();
    
    // LOOKUP: Obtener horarios con información del doctor
    @Aggregation(pipeline = {
        "{ $match: { 'activo': true } }",
        "{ $lookup: { from: 'usuarios', localField: 'doctorId', foreignField: '_id', as: 'doctor' } }",
        "{ $unwind: '$doctor' }",
        "{ $match: { 'doctor.rol': 'DOCTOR', 'doctor.activo': true } }",
        "{ $sort: { 'diaSemana': 1, 'horaInicio': 1 } }"
    })
    List<Object> findHorariosActivosConDetallesDoctor();
    
    // LOOKUP: Obtener horarios de un doctor específico con sus detalles
    @Aggregation(pipeline = {
        "{ $match: { 'doctorId': ?0, 'activo': true } }",
        "{ $lookup: { from: 'usuarios', localField: 'doctorId', foreignField: '_id', as: 'doctor' } }",
        "{ $unwind: '$doctor' }",
        "{ $sort: { 'diaSemana': 1, 'horaInicio': 1 } }"
    })
    List<Object> findHorariosConDetallesByDoctorId(String doctorId);
    
    // LOOKUP: Obtener horarios disponibles por día de la semana con información del doctor
    @Aggregation(pipeline = {
        "{ $match: { 'diaSemana': ?0, 'activo': true } }",
        "{ $lookup: { from: 'usuarios', localField: 'doctorId', foreignField: '_id', as: 'doctor' } }",
        "{ $unwind: '$doctor' }",
        "{ $match: { 'doctor.rol': 'DOCTOR', 'doctor.activo': true } }",
        "{ $sort: { 'doctor.nombre': 1, 'horaInicio': 1 } }"
    })
    List<Object> findHorariosConDetallesByDiaSemana(String diaSemana);
    
    // Verificar si existe horario para un doctor en un día específico
    boolean existsByDoctorIdAndDiaSemana(String doctorId, DayOfWeek diaSemana);
    
    // Eliminar horarios de un doctor
    void deleteByDoctorId(String doctorId);
    
    // Contar horarios activos de un doctor
    long countByDoctorIdAndActivoTrue(String doctorId);
}