package com.universidad.vitaltech.config;

import com.universidad.vitaltech.model.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Implementación personalizada de UserDetails para Spring Security
 */
public class CustomUserDetails implements UserDetails {
    
    private final Usuario usuario;
    
    public CustomUserDetails(Usuario usuario) {
        this.usuario = usuario;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Retorna el rol del usuario con el prefijo ROLE_
        return Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name())
        );
    }
    
    @Override
    public String getPassword() {
        return usuario.getPassword();
    }
    
    @Override
    public String getUsername() {
        return usuario.getUsername();
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return usuario.isActivo();
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return usuario.isActivo();
    }
    
    // Método para obtener el usuario completo
    public Usuario getUsuario() {
        return usuario;
    }
    
    // Métodos de utilidad
    public String getId() {
        return usuario.getId();
    }
    
    public String getNombreCompleto() {
        return usuario.getNombreCompleto();
    }
    
    public String getRol() {
        return usuario.getRol().name();
    }
}