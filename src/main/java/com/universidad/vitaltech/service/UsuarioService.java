package com.universidad.vitaltech.service;

import com.universidad.vitaltech.model.Rol;
import com.universidad.vitaltech.model.Usuario;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz del servicio de Usuario
 */
public interface UsuarioService {
    
    // CRUD básico
    Usuario guardar(Usuario usuario);
    Optional<Usuario> buscarPorId(String id);
    List<Usuario> listarTodos();
    void eliminar(String id);
    
    // Búsquedas específicas
    Optional<Usuario> buscarPorUsername(String username);
    Optional<Usuario> buscarPorEmail(String email);
    Optional<Usuario> buscarPorNumeroDocumento(String numeroDocumento);
    
    // Búsquedas por rol
    List<Usuario> listarPorRol(Rol rol);
    List<Usuario> listarDoctoresActivos();
    List<Usuario> listarPacientesActivos();
    List<Usuario> listarDoctoresPorEspecialidad(String especialidad);
    
    // Validaciones
    boolean existeUsername(String username);
    boolean existeEmail(String email);
    boolean existeNumeroDocumento(String numeroDocumento);
    
    // Operaciones especiales
    Usuario registrarPaciente(Usuario paciente);
    Usuario actualizarPerfil(Usuario usuario);
    void cambiarEstado(String id, boolean activo);
    
    // Búsqueda
    List<Usuario> buscarPorNombre(String nombre);
}