package com.universidad.vitaltech.controller;

import com.universidad.vitaltech.config.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador para redireccionar al dashboard según el rol
 */
@Controller
public class DashboardController {
    
    /**
     * Redirige al dashboard correspondiente según el rol del usuario
     */
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }
        
        // Obtener el rol del usuario autenticado
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String rol = authority.getAuthority();
            
            switch (rol) {
                case "ROLE_ADMIN":
                    return "redirect:/admin/dashboard";
                    
                case "ROLE_DOCTOR":
                    return "redirect:/doctor/dashboard";
                    
                case "ROLE_PACIENTE":
                    return "redirect:/paciente/dashboard";
                    
                case "ROLE_RECEPCIONISTA":
                    return "redirect:/recepcionista/dashboard";
                    
                default:
                    return "redirect:/login";
            }
        }
        
        return "redirect:/login";
    }
}