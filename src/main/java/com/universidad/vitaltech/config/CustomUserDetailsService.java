package com.universidad.vitaltech.config;

import com.universidad.vitaltech.model.Usuario;
import com.universidad.vitaltech.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Servicio personalizado para cargar usuarios desde MongoDB
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException(
                "Usuario no encontrado: " + username
            ));
        
        if (!usuario.isActivo()) {
            throw new UsernameNotFoundException(
                "Usuario inactivo: " + username
            );
        }
        
        return new CustomUserDetails(usuario);
    }
}