package com.universidad.vitaltech.controller;

import com.universidad.vitaltech.model.Usuario;
import com.universidad.vitaltech.service.DiagnosticoService;
import com.universidad.vitaltech.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.universidad.vitaltech.model.Diagnostico;
import java.util.Optional;

/**
 * Controlador para rutas públicas (home, login, registro)
 */
@Controller
public class HomeController {
    
    @Autowired
    private UsuarioService usuarioService;
    
    @Autowired
    private DiagnosticoService diagnosticoService;
    
    /**
     * Página principal
     */
    @GetMapping("/")
    public String home() {
        return "public/home";
    }
    
    /**
     * Página de login
     */
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                       @RequestParam(value = "logout", required = false) String logout,
                       Model model) {
        if (error != null) {
            model.addAttribute("error", "Usuario o contraseña incorrectos");
        }
        if (logout != null) {
            model.addAttribute("mensaje", "Sesión cerrada exitosamente");
        }
        return "public/login";
    }
    
    /**
     * Formulario de registro de paciente
     */
    @GetMapping("/registro-paciente")
    public String registroPaciente(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "public/registro-paciente";
    }
    
    /**
     * Procesar registro de paciente
     */
    @PostMapping("/registro-paciente")
    public String guardarPaciente(@ModelAttribute Usuario usuario, Model model) {
        try {
            // Validar que no existan duplicados
            if (usuarioService.existeUsername(usuario.getUsername())) {
                model.addAttribute("error", "El nombre de usuario ya existe");
                model.addAttribute("usuario", usuario);
                return "public/registro-paciente";
            }
            
            if (usuarioService.existeEmail(usuario.getEmail())) {
                model.addAttribute("error", "El email ya está registrado");
                model.addAttribute("usuario", usuario);
                return "public/registro-paciente";
            }
            
            if (usuarioService.existeNumeroDocumento(usuario.getNumeroDocumento())) {
                model.addAttribute("error", "El número de documento ya está registrado");
                model.addAttribute("usuario", usuario);
                return "public/registro-paciente";
            }
            
            // Registrar paciente
            usuarioService.registrarPaciente(usuario);
            model.addAttribute("mensaje", "Registro exitoso. Ya puedes iniciar sesión.");
            return "public/login";
            
        } catch (Exception e) {
            model.addAttribute("error", "Error al registrar: " + e.getMessage());
            model.addAttribute("usuario", usuario);
            return "public/registro-paciente";
        }
    }
    
    /**
     * Consultar diagnóstico sin estar registrado
     */
    @GetMapping("/consultar-diagnostico")
    public String consultarDiagnostico() {
        return "public/consultar-diagnostico";
    }
    
    /**
     * Buscar diagnóstico por número de documento
     */
    @PostMapping("/consultar-diagnostico")
    public String buscarDiagnostico(@RequestParam String numeroDocumento, Model model) {
        try {
            Optional<Usuario> usuario = usuarioService.buscarPorNumeroDocumento(numeroDocumento);
            
            if (usuario.isEmpty()) {
                model.addAttribute("error", "No se encontró ningún paciente con ese número de documento");
                return "public/consultar-diagnostico";
            }
            
            if (!usuario.get().esPaciente()) {
                model.addAttribute("error", "El documento no corresponde a un paciente");
                return "public/consultar-diagnostico";
            }
            
            // Buscar último diagnóstico
            Optional<Diagnostico> ultimoDiagnostico = diagnosticoService.buscarUltimoDiagnosticoPorPaciente(usuario.get().getId());
            
            if (ultimoDiagnostico.isEmpty()) {
                model.addAttribute("error", "No se encontraron diagnósticos para este paciente");
                return "public/consultar-diagnostico";
            }
            
            model.addAttribute("paciente", usuario.get());
            model.addAttribute("diagnostico", ultimoDiagnostico.get());
            return "public/ver-diagnostico";
            
        } catch (Exception e) {
            model.addAttribute("error", "Error al buscar diagnóstico: " + e.getMessage());
            return "public/consultar-diagnostico";
        }
    }
    
    /**
     * Página de acceso denegado
     */
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "public/access-denied";
    }
}