package com.universidad.vitaltech.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.universidad.vitaltech.model.Rol;
import com.universidad.vitaltech.model.Usuario;

/**
 * Repositorio para la entidad Usuario
 */
@Repository
public interface UsuarioRepository extends MongoRepository<Usuario, String> {
    
    // Buscar por username
    Optional<Usuario> findByUsername(String username);
    
    // Buscar por email
    Optional<Usuario> findByEmail(String email);
    
    // Buscar por número de documento
    Optional<Usuario> findByNumeroDocumento(String numeroDocumento);
    
    // Buscar por rol
    List<Usuario> findByRol(Rol rol);
    
    // Buscar usuarios activos por rol
    List<Usuario> findByRolAndActivoTrue(Rol rol);
    
    // Buscar doctores activos
    @Query("{ 'rol': 'DOCTOR', 'activo': true }")
    List<Usuario> findDoctoresActivos();
    
    // Buscar doctor por especialidad
    @Query("{ 'rol': 'DOCTOR', 'especialidad': ?0, 'activo': true }")
    List<Usuario> findDoctoresByEspecialidad(String especialidad);
    
    // Buscar pacientes activos
    @Query("{ 'rol': 'PACIENTE', 'activo': true }")
    List<Usuario> findPacientesActivos();
    
    // Verificar si existe username
    boolean existsByUsername(String username);
    
    // Verificar si existe email
    boolean existsByEmail(String email);
    
    // Verificar si existe documento
    boolean existsByNumeroDocumento(String numeroDocumento);
    
    // Buscar por nombre o apellido 
    @Query("{ $or: [ { 'nombre': { $regex: ?0, $options: 'i' } }, { 'apellido': { $regex: ?0, $options: 'i' } } ] }")
    List<Usuario> buscarPorNombre(String nombre);
    
    //Buscar por nombre apellido correo o numero de documento 
    @Query("{ $or: [ " +
            "{ 'nombre': { $regex: ?0, $options: 'i' } }, " +
            "{ 'apellido': { $regex: ?0, $options: 'i' } }, " +
            "{ 'email': { $regex: ?0, $options: 'i' } }, " +
            "{ 'numeroDocumento': { $regex: ?0, $options: 'i' } } " +
            "] }")
    List<Usuario> buscarPorNombreCorreoODocumento(String termino);
}
