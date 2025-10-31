package com.universidad.vitaltech.service.impl;

import com.universidad.vitaltech.model.Rol;
import com.universidad.vitaltech.model.Usuario;
import com.universidad.vitaltech.repository.UsuarioRepository;
import com.universidad.vitaltech.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio de Usuario
 */
@Service
public class UsuarioServiceImpl implements UsuarioService {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public Usuario guardar(Usuario usuario) {
        // Encriptar contraseña si es nueva o cambió
        if (usuario.getId() == null || !usuario.getPassword().startsWith("$2a$")) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }
        
        // Establecer fecha de registro si es nuevo
        if (usuario.getId() == null) {
            usuario.setFechaRegistro(LocalDateTime.now());
        } else {
            usuario.setUltimaActualizacion(LocalDateTime.now());
        }
        
        return usuarioRepository.save(usuario);
    }
    
    @Override
    public Optional<Usuario> buscarPorId(String id) {
        return usuarioRepository.findById(id);
    }
    
    @Override
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }
    
    @Override
    public void eliminar(String id) {
        usuarioRepository.deleteById(id);
    }
    
    @Override
    public Optional<Usuario> buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }
    
    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }
    
    @Override
    public Optional<Usuario> buscarPorNumeroDocumento(String numeroDocumento) {
        return usuarioRepository.findByNumeroDocumento(numeroDocumento);
    }
    
    @Override
    public List<Usuario> listarPorRol(Rol rol) {
        return usuarioRepository.findByRol(rol);
    }
    
    @Override
    public List<Usuario> listarDoctoresActivos() {
        return usuarioRepository.findDoctoresActivos();
    }
    
    @Override
    public List<Usuario> listarPacientesActivos() {
        return usuarioRepository.findPacientesActivos();
    }
    
    @Override
    public List<Usuario> listarDoctoresPorEspecialidad(String especialidad) {
        return usuarioRepository.findDoctoresByEspecialidad(especialidad);
    }
    
    @Override
    public boolean existeUsername(String username) {
        return usuarioRepository.existsByUsername(username);
    }
    
    @Override
    public boolean existeEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }
    
    @Override
    public boolean existeNumeroDocumento(String numeroDocumento) {
        return usuarioRepository.existsByNumeroDocumento(numeroDocumento);
    }
    
    @Override
    public Usuario registrarPaciente(Usuario paciente) {
        // Validar que sea rol paciente
        paciente.setRol(Rol.PACIENTE);
        paciente.setActivo(true);
        return guardar(paciente);
    }
    
    @Override
    public Usuario actualizarPerfil(Usuario usuario) {
        Optional<Usuario> usuarioExistente = buscarPorId(usuario.getId());
        if (usuarioExistente.isPresent()) {
            Usuario usuarioActual = usuarioExistente.get();
            
            // Actualizar solo campos permitidos
            usuarioActual.setNombre(usuario.getNombre());
            usuarioActual.setApellido(usuario.getApellido());
            usuarioActual.setTelefono(usuario.getTelefono());
            usuarioActual.setDireccion(usuario.getDireccion());
            usuarioActual.setUltimaActualizacion(LocalDateTime.now());
            
            // Si es paciente, actualizar información médica
            if (usuarioActual.esPaciente() && usuario.getInformacionMedica() != null) {
                usuarioActual.setInformacionMedica(usuario.getInformacionMedica());
            }
            
            return usuarioRepository.save(usuarioActual);
        }
        
        throw new RuntimeException("Usuario no encontrado");
    }
    
    @Override
    public void cambiarEstado(String id, boolean activo) {
        Optional<Usuario> usuario = buscarPorId(id);
        if (usuario.isPresent()) {
            Usuario user = usuario.get();
            user.setActivo(activo);
            user.setUltimaActualizacion(LocalDateTime.now());
            usuarioRepository.save(user);
        }
    }
    
    @Override
    public List<Usuario> buscarPorNombre(String nombre) {
        return usuarioRepository.buscarPorNombre(nombre);
    }
}