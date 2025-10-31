package com.universidad.vitaltech.repository;

import com.universidad.vitaltech.model.Rol;
import com.universidad.vitaltech.model.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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
    
    // Buscar por nombre completo (búsqueda parcial)
    @Query("{ $or: [ { 'nombre': { $regex: ?0, $options: 'i' } }, { 'apellido': { $regex: ?0, $options: 'i' } } ] }")
    List<Usuario> buscarPorNombre(String nombre);
}