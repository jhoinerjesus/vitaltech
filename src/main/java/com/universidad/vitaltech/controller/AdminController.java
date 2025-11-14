package com.universidad.vitaltech.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.universidad.vitaltech.model.Rol;
import com.universidad.vitaltech.model.Usuario;
import com.universidad.vitaltech.service.CitaService;
import com.universidad.vitaltech.service.DiagnosticoService;
import com.universidad.vitaltech.service.UsuarioService;

/**
 * Controlador para funcionalidades del Administrador
 */
@Controller
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private UsuarioService usuarioService;
    
    @Autowired
    private CitaService citaService;
    
    @Autowired
    private DiagnosticoService diagnosticoService;
    
    /**
     * Dashboard del administrador
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        // Estadísticas generales
        long totalUsuarios = usuarioService.listarTodos().size();
        long totalDoctores = usuarioService.listarDoctoresActivos().size();
        long totalPacientes = usuarioService.listarPacientesActivos().size();
        long totalCitas = citaService.listarTodas().size();
        
        model.addAttribute("totalUsuarios", totalUsuarios);
        model.addAttribute("totalDoctores", totalDoctores);
        model.addAttribute("totalPacientes", totalPacientes);
        model.addAttribute("totalCitas", totalCitas);
        
        return "admin/dashboard";
    }
    
    /**
     * Listar todos los usuarios
     */
    @GetMapping("/usuarios")
    public String listarUsuarios(Model model) {
        List<Usuario> usuarios = usuarioService.listarTodos();
        model.addAttribute("usuarios", usuarios);
        return "admin/usuarios";
    }
    
    /**
     * Formulario para crear nuevo usuario
     */
    @GetMapping("/usuarios/nuevo")
    public String nuevoUsuarioForm(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("roles", Rol.values());
        return "admin/usuario-form";
    }
    
    /**
     * Guardar nuevo usuario
     */
    @PostMapping("/usuarios/guardar")
    public String guardarUsuario(@ModelAttribute Usuario usuario, 
                                 RedirectAttributes redirectAttributes,
                                 Authentication authentication) {
        try {
            // Validar duplicados
            if (usuario.getId() == null) {
                if (usuarioService.existeUsername(usuario.getUsername())) {
                    redirectAttributes.addFlashAttribute("error", "El nombre de usuario ya existe");
                    return "redirect:/admin/usuarios/nuevo";
                }
                if (usuarioService.existeEmail(usuario.getEmail())) {
                    redirectAttributes.addFlashAttribute("error", "El email ya está registrado");
                    return "redirect:/admin/usuarios/nuevo";
                }
                if (usuarioService.existeNumeroDocumento(usuario.getNumeroDocumento())) {
                    redirectAttributes.addFlashAttribute("error", "El número de documento ya está registrado");
                    return "redirect:/admin/usuarios/nuevo";
                }
                
                // Establecer quien lo registró
                usuario.setRegistradoPor(authentication.getName());
            }
            
            usuarioService.guardar(usuario);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario guardado exitosamente");
            return "redirect:/admin/usuarios";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar: " + e.getMessage());
            return "redirect:/admin/usuarios/nuevo";
        }
    }
    
    /**
     Formulario para editar usuario
     */
    @GetMapping("/usuarios/editar/{id}")
    public String editarUsuarioForm(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Usuario> usuario = usuarioService.buscarPorId(id);
        
        if (usuario.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
            return "redirect:/admin/usuarios";
        }
        
        model.addAttribute("usuario", usuario.get());
        model.addAttribute("roles", Rol.values());
        return "admin/usuario-form";
    }
    
    /**
     Actualizar usuario
     */
    @PostMapping("/usuarios/actualizar")
    public String actualizarUsuario(@ModelAttribute Usuario usuario, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.guardar(usuario);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario actualizado exitosamente");
            return "redirect:/admin/usuarios";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar: " + e.getMessage());
            return "redirect:/admin/usuarios/editar/" + usuario.getId();
        }
    }
    
    /**
     Eliminar usuario
     */
    @GetMapping("/usuarios/eliminar/{id}")
    public String eliminarUsuario(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.eliminar(id);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario eliminado exitosamente");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar: " + e.getMessage());
        }
        
        return "redirect:/admin/usuarios";
    }
    
    /**
     Cambiar estado de usuario (activar/desactivar)
     */
    @GetMapping("/usuarios/cambiar-estado/{id}")
    public String cambiarEstado(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(id);
            
            if (usuarioOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
                return "redirect:/admin/usuarios";
            }
            
            Usuario usuario = usuarioOpt.get();
            usuarioService.cambiarEstado(id, !usuario.isActivo());
            
            String estado = usuario.isActivo() ? "desactivado" : "activado";
            redirectAttributes.addFlashAttribute("mensaje", "Usuario " + estado + " exitosamente");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cambiar estado: " + e.getMessage());
        }
        
        return "redirect:/admin/usuarios";
    }
    
    /**
     * Ver detalles de un usuario
     */
    @GetMapping("/usuarios/ver/{id}")
    public String verUsuario(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Usuario> usuario = usuarioService.buscarPorId(id);
        
        if (usuario.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
            return "redirect:/admin/usuarios";
        }
        
        model.addAttribute("usuario", usuario.get());
        
        // Si es doctor, mostrar estadísticas
        if (usuario.get().esDoctor()) {
            long totalCitas = citaService.listarPorDoctor(id).size();
            long totalDiagnosticos = diagnosticoService.contarDiagnosticosPorDoctor(id);
            model.addAttribute("totalCitas", totalCitas);
            model.addAttribute("totalDiagnosticos", totalDiagnosticos);
        }
        
        // Si es paciente, mostrar estadísticas
        if (usuario.get().esPaciente()) {
            long totalCitas = citaService.listarPorPaciente(id).size();
            long totalDiagnosticos = diagnosticoService.contarDiagnosticosPorPaciente(id);
            model.addAttribute("totalCitas", totalCitas);
            model.addAttribute("totalDiagnosticos", totalDiagnosticos);
        }
        
        return "admin/usuario-detalle";
    }
    
    /**
     * Buscar usuarios
     */
    @GetMapping("/usuarios/buscar")
    public String buscarUsuarios(@RequestParam(required = false) String termino, Model model) {
        List<Usuario> usuarios;
        
        if (termino != null && !termino.isEmpty()) {
            usuarios = usuarioService.buscarPorTermino(termino);  // ← CAMBIAR AQUÍ
            model.addAttribute("termino", termino);
        }else {
            usuarios = usuarioService.listarTodos();
        }
    
        model.addAttribute("usuarios", usuarios);
        return "admin/usuarios";
    }
}