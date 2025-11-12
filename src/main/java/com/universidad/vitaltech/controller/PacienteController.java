package com.universidad.vitaltech.controller;

import com.universidad.vitaltech.config.CustomUserDetails;
import com.universidad.vitaltech.model.*;
import com.universidad.vitaltech.model.embedded.Horario;
import com.universidad.vitaltech.service.CitaService;
import com.universidad.vitaltech.service.DiagnosticoService;
import com.universidad.vitaltech.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/*
  Controlador para funcionalidades del Paciente
 */
@Controller
@RequestMapping("/paciente")
public class PacienteController {
    
    @Autowired
    private UsuarioService usuarioService;
    
    @Autowired
    private CitaService citaService;
    
    @Autowired
    private DiagnosticoService diagnosticoService;
    
    /*
     panel del paciente
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String pacienteId = userDetails.getId();
        String nombrePaciente = userDetails.getNombreCompleto();
        
        // Obtener citas
        List<Cita> proximasCitas = citaService.listarPorPaciente(pacienteId).stream()
            .filter(c -> c.getHorario().getFecha().isAfter(LocalDate.now()) || 
                        c.getHorario().getFecha().isEqual(LocalDate.now()))
            .filter(c -> !c.estaCancelada())
            .limit(5)
            .toList();
        
        // Estadisticas
        long totalCitas = citaService.listarPorPaciente(pacienteId).size();
        long citasPendientes = citaService.listarPorPaciente(pacienteId).stream()
            .filter(c -> c.estaProgramada())
            .count();
        
        model.addAttribute("nombrePaciente", nombrePaciente);
        model.addAttribute("proximasCitas", proximasCitas);
        model.addAttribute("totalCitas", totalCitas);
        model.addAttribute("citasPendientes", citasPendientes);
        
        return "paciente/dashboard";
    }
    
    /**
     * Ver mis citas
     */
    @GetMapping("/citas")
    public String misCitas(Model model, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String pacienteId = userDetails.getId();
        
        List<Cita> citas = citaService.listarPorPaciente(pacienteId);
        
        // Lookup para cargar nombres de doctores
        Map<String, String> nombresUsuarios = new HashMap<>();
        
        for (Cita cita : citas) {
            if (!nombresUsuarios.containsKey(cita.getDoctorId())) {
                usuarioService.buscarPorId(cita.getDoctorId())
                    .ifPresent(u -> nombresUsuarios.put(u.getId(), u.getNombreCompleto()));
            }
        }
        
        model.addAttribute("citas", citas);
        model.addAttribute("nombresUsuarios", nombresUsuarios);
        
        return "paciente/mis-citas";
    }
    
    /**
     * Formulario para agendar nueva cita
     */
    @GetMapping("/citas/nueva")
    public String nuevaCitaForm(Model model) {
        List<Usuario> doctores = usuarioService.listarDoctoresActivos();
        
        model.addAttribute("doctores", doctores);
        
        return "paciente/agendar-cita";
    }
    
    /**
     * Guardar nueva cita
     */
    @PostMapping("/citas/guardar")
    public String guardarCita(@RequestParam String doctorId,
                             @RequestParam String fecha,
                             @RequestParam String horaInicio,
                             @RequestParam String horaFin,
                             @RequestParam String motivoConsulta,
                             RedirectAttributes redirectAttributes,
                             Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String pacienteId = userDetails.getId();
            
            // Crear la cita
            Cita cita = new Cita();
            cita.setPacienteId(pacienteId);
            cita.setDoctorId(doctorId);
            cita.setMotivoConsulta(motivoConsulta);
            
            // Crear horario
            Horario horario = new Horario();
            horario.setFecha(LocalDate.parse(fecha));
            horario.setHoraInicio(LocalTime.parse(horaInicio));
            horario.setHoraFin(LocalTime.parse(horaFin));
            
            cita.setHorario(horario);
            cita.setCreadaPor(pacienteId);
            
            citaService.guardar(cita);
            redirectAttributes.addFlashAttribute("mensaje", "Cita agendada exitosamente");
            return "redirect:/paciente/citas";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al agendar cita: " + e.getMessage());
            return "redirect:/paciente/citas/nueva";
        }
    }
    
    /**
     * Ver detalle de una cita
     */
    @GetMapping("/citas/{id}")
    public String verCita(@PathVariable String id, 
                         Model model, 
                         Authentication authentication,
                         RedirectAttributes redirectAttributes) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String pacienteId = userDetails.getId();
        
        Optional<Cita> citaOpt = citaService.buscarPorId(id);
        
        if (citaOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Cita no encontrada");
            return "redirect:/paciente/citas";
        }
        
        Cita cita = citaOpt.get();
        
        // Verificar que la cita pertenezca al paciente
        if (!cita.getPacienteId().equals(pacienteId)) {
            redirectAttributes.addFlashAttribute("error", "No tienes permiso para ver esta cita");
            return "redirect:/paciente/citas";
        }
        
        // Obtener doctor
        Optional<Usuario> doctor = usuarioService.buscarPorId(cita.getDoctorId());
        
        model.addAttribute("cita", cita);
        model.addAttribute("doctor", doctor.orElse(null));
        
        return "paciente/cita-detalle";
    }
    
    /**
     * Cancelar una cita
     */
    @GetMapping("/citas/cancelar/{id}")
    public String cancelarCita(@PathVariable String id,
                              @RequestParam(required = false) String motivo,
                              RedirectAttributes redirectAttributes,
                              Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String pacienteId = userDetails.getId();
            
            Optional<Cita> citaOpt = citaService.buscarPorId(id);
            
            if (citaOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Cita no encontrada");
                return "redirect:/paciente/citas";
            }
            
            Cita cita = citaOpt.get();
            
            // Verificar que la cita pertenezca al paciente
            if (!cita.getPacienteId().equals(pacienteId)) {
                redirectAttributes.addFlashAttribute("error", "No tienes permiso para cancelar esta cita");
                return "redirect:/paciente/citas";
            }
            
            // Verificar que la cita no esté completada
            if (cita.estaCompletada()) {
                redirectAttributes.addFlashAttribute("error", "No se puede cancelar una cita completada");
                return "redirect:/paciente/citas/" + id;
            }
            
            String motivoCancelacion = motivo != null ? motivo : "Cancelada por el paciente";
            citaService.cancelarCita(id, pacienteId, motivoCancelacion);
            
            redirectAttributes.addFlashAttribute("mensaje", "Cita cancelada exitosamente");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cancelar cita: " + e.getMessage());
        }
        
        return "redirect:/paciente/citas";
    }
    
    /**
     * Ver mis diagnósticos
     */
    @GetMapping("/diagnosticos")
    public String misDiagnosticos(Model model, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String pacienteId = userDetails.getId();
        
        List<Diagnostico> diagnosticos = diagnosticoService.listarPorPaciente(pacienteId);
        
        model.addAttribute("diagnosticos", diagnosticos);
        
        return "paciente/mis-diagnosticos";
    }
}