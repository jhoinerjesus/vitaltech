package com.universidad.vitaltech.controller;

import com.universidad.vitaltech.config.CustomUserDetails;
import com.universidad.vitaltech.model.*;
import com.universidad.vitaltech.service.CitaService;
import com.universidad.vitaltech.service.DiagnosticoService;
import com.universidad.vitaltech.service.HorarioDisponibleService;
import com.universidad.vitaltech.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Controlador para funcionalidades del Doctor
 */
@Controller
@RequestMapping("/doctor")
public class DoctorController {
    
    @Autowired
    private UsuarioService usuarioService;
    
    @Autowired
    private CitaService citaService;
    
    @Autowired
    private DiagnosticoService diagnosticoService;
    
    @Autowired
    private HorarioDisponibleService horarioDisponibleService;
    
    /**
     * Dashboard del doctor
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String doctorId = userDetails.getId();
        
        // Estadísticas
        long citasHoy = citaService.listarPorDoctorYFecha(doctorId, LocalDate.now()).size();
        long citasPendientes = citaService.contarCitasPorDoctorYEstado(doctorId, EstadoCita.PROGRAMADA) +
                               citaService.contarCitasPorDoctorYEstado(doctorId, EstadoCita.CONFIRMADA);
        long totalDiagnosticos = diagnosticoService.contarDiagnosticosPorDoctor(doctorId);
        
        // Citas de hoy
        List<Cita> citasHoyLista = citaService.listarPorDoctorYFecha(doctorId, LocalDate.now());
        
        model.addAttribute("citasHoy", citasHoy);
        model.addAttribute("citasPendientes", citasPendientes);
        model.addAttribute("totalDiagnosticos", totalDiagnosticos);
        model.addAttribute("citasHoyLista", citasHoyLista);
        
        return "doctor/dashboard";
    }
    
    /**
     * Ver citas del doctor
     */
    @GetMapping("/citas")
    public String verCitas(@RequestParam(required = false) String fecha,
                          Model model, 
                          Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String doctorId = userDetails.getId();
        
        List<Cita> citas;
        
        if (fecha != null && !fecha.isEmpty()) {
            LocalDate fechaBuscar = LocalDate.parse(fecha);
            citas = citaService.listarPorDoctorYFecha(doctorId, fechaBuscar);
            model.addAttribute("fechaSeleccionada", fecha);
        } else {
            citas = citaService.listarPorDoctor(doctorId);
        }
        
        model.addAttribute("citas", citas);
        return "doctor/citas";
    }
    
    /**
     * Ver detalles de una cita
     */
    @GetMapping("/citas/{id}")
    public String verCita(@PathVariable String id, 
                         Model model, 
                         RedirectAttributes redirectAttributes,
                         Authentication authentication) {
        Optional<Cita> citaOpt = citaService.buscarPorId(id);
        
        if (citaOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Cita no encontrada");
            return "redirect:/doctor/citas";
        }
        
        Cita cita = citaOpt.get();
        
        // Verificar que la cita pertenece al doctor
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        if (!cita.getDoctorId().equals(userDetails.getId())) {
            redirectAttributes.addFlashAttribute("error", "No tienes permiso para ver esta cita");
            return "redirect:/doctor/citas";
        }
        
        // Obtener información del paciente
        Optional<Usuario> paciente = usuarioService.buscarPorId(cita.getPacienteId());
        
        // Verificar si ya existe diagnóstico
        Optional<Diagnostico> diagnostico = diagnosticoService.buscarPorCita(id);
        
        model.addAttribute("cita", cita);
        model.addAttribute("paciente", paciente.orElse(null));
        model.addAttribute("diagnostico", diagnostico.orElse(null));
        
        return "doctor/cita-detalle";
    }
    
    /**
     * Formulario para crear diagnóstico
     */
    @GetMapping("/citas/{id}/diagnostico")
    public String crearDiagnosticoForm(@PathVariable String id, 
                                       Model model, 
                                       RedirectAttributes redirectAttributes,
                                       Authentication authentication) {
        Optional<Cita> citaOpt = citaService.buscarPorId(id);
        
        if (citaOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Cita no encontrada");
            return "redirect:/doctor/citas";
        }
        
        Cita cita = citaOpt.get();
        
        // Verificar que la cita pertenece al doctor
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        if (!cita.getDoctorId().equals(userDetails.getId())) {
            redirectAttributes.addFlashAttribute("error", "No tienes permiso para crear diagnóstico en esta cita");
            return "redirect:/doctor/citas";
        }
        
        // Verificar si ya existe diagnóstico
        if (diagnosticoService.existeDiagnosticoParaCita(id)) {
            redirectAttributes.addFlashAttribute("error", "Ya existe un diagnóstico para esta cita");
            return "redirect:/doctor/citas/" + id;
        }
        
        // Obtener información del paciente
        Optional<Usuario> paciente = usuarioService.buscarPorId(cita.getPacienteId());
        
        Diagnostico diagnostico = new Diagnostico();
        diagnostico.setCitaId(id);
        diagnostico.setPacienteId(cita.getPacienteId());
        diagnostico.setDoctorId(userDetails.getId());
        
        model.addAttribute("cita", cita);
        model.addAttribute("paciente", paciente.orElse(null));
        model.addAttribute("diagnostico", diagnostico);
        
        return "doctor/diagnostico-form";
    }
    
    /**
     * Guardar diagnóstico
     */
    @PostMapping("/diagnostico/guardar")
    public String guardarDiagnostico(@ModelAttribute Diagnostico diagnostico,
                                    RedirectAttributes redirectAttributes,
                                    Authentication authentication) {
        try {
            // Guardar diagnóstico
            diagnosticoService.guardar(diagnostico);
            
            // Marcar cita como completada
            citaService.completarCita(diagnostico.getCitaId());
            
            redirectAttributes.addFlashAttribute("mensaje", "Diagnóstico guardado exitosamente");
            return "redirect:/doctor/citas/" + diagnostico.getCitaId();
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar diagnóstico: " + e.getMessage());
            return "redirect:/doctor/citas/" + diagnostico.getCitaId() + "/diagnostico";
        }
    }
    
    /**
     * Ver historial de diagnósticos realizados
     */
    @GetMapping("/diagnosticos")
    public String verDiagnosticos(Model model, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String doctorId = userDetails.getId();
        
        List<Diagnostico> diagnosticos = diagnosticoService.listarPorDoctor(doctorId);
        model.addAttribute("diagnosticos", diagnosticos);
        
        return "doctor/diagnosticos";
    }
    
    /**
     * Ver horarios disponibles
     */
    @GetMapping("/horarios")
    public String verHorarios(Model model, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String doctorId = userDetails.getId();
        
        List<HorarioDisponible> horarios = horarioDisponibleService.listarActivosPorDoctor(doctorId);
        model.addAttribute("horarios", horarios);
        
        return "doctor/horarios";
    }
    
    /**
     * Formulario para crear horario
     */
    @GetMapping("/horarios/nuevo")
    public String nuevoHorarioForm(Model model) {
        model.addAttribute("horario", new HorarioDisponible());
        model.addAttribute("diasSemana", DayOfWeek.values());
        return "doctor/horario-form";
    }
    
    /**
     * Guardar horario
     */
    @PostMapping("/horarios/guardar")
    public String guardarHorario(@ModelAttribute HorarioDisponible horario,
                                RedirectAttributes redirectAttributes,
                                Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            horario.setDoctorId(userDetails.getId());
            
            horarioDisponibleService.guardar(horario);
            redirectAttributes.addFlashAttribute("mensaje", "Horario guardado exitosamente");
            return "redirect:/doctor/horarios";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar horario: " + e.getMessage());
            return "redirect:/doctor/horarios/nuevo";
        }
    }
    
    /**
     * Eliminar horario
     */
    @GetMapping("/horarios/eliminar/{id}")
    public String eliminarHorario(@PathVariable String id, 
                                  RedirectAttributes redirectAttributes,
                                  Authentication authentication) {
        try {
            Optional<HorarioDisponible> horarioOpt = horarioDisponibleService.buscarPorId(id);
            
            if (horarioOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Horario no encontrado");
                return "redirect:/doctor/horarios";
            }
            
            // Verificar que el horario pertenece al doctor
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            if (!horarioOpt.get().getDoctorId().equals(userDetails.getId())) {
                redirectAttributes.addFlashAttribute("error", "No tienes permiso para eliminar este horario");
                return "redirect:/doctor/horarios";
            }
            
            horarioDisponibleService.eliminar(id);
            redirectAttributes.addFlashAttribute("mensaje", "Horario eliminado exitosamente");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar horario: " + e.getMessage());
        }
        
        return "redirect:/doctor/horarios";
    }
}