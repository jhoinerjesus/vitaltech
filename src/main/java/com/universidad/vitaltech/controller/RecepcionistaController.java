package com.universidad.vitaltech.controller;

import com.universidad.vitaltech.config.CustomUserDetails;
import com.universidad.vitaltech.model.*;
import com.universidad.vitaltech.model.embedded.Horario;
import com.universidad.vitaltech.service.CitaService;
import com.universidad.vitaltech.service.HorarioDisponibleService;
import com.universidad.vitaltech.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Controlador para funcionalidades de la Recepcionista
 */
@Controller
@RequestMapping("/recepcionista")
public class RecepcionistaController {
    
    @Autowired
    private UsuarioService usuarioService;
    
    @Autowired
    private CitaService citaService;
    
    @Autowired
    private HorarioDisponibleService horarioDisponibleService;
    
    /**
     * Dashboard de la recepcionista
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Estadísticas del día
        List<Cita> citasHoy = citaService.listarPorFecha(LocalDate.now());
        long citasProgramadas = citaService.listarPorEstado(EstadoCita.PROGRAMADA).size();
        long totalPacientes = usuarioService.listarPacientesActivos().size();
        long totalDoctores = usuarioService.listarDoctoresActivos().size();
        
        model.addAttribute("citasHoy", citasHoy.size());
        model.addAttribute("citasProgramadas", citasProgramadas);
        model.addAttribute("totalPacientes", totalPacientes);
        model.addAttribute("totalDoctores", totalDoctores);
        model.addAttribute("citasHoyLista", citasHoy);
        
        return "recepcionista/dashboard";
    }
    
    /**
     * Buscar pacientes
     */
    @GetMapping("/pacientes")
    public String buscarPacientes(@RequestParam(required = false) String termino, Model model) {
        List<Usuario> pacientes;
        
        if (termino != null && !termino.isEmpty()) {
            pacientes = usuarioService.buscarPorNombre(termino);
            // Filtrar solo pacientes
            pacientes = pacientes.stream()
                    .filter(Usuario::esPaciente)
                    .toList();
            model.addAttribute("termino", termino);
        } else {
            pacientes = usuarioService.listarPacientesActivos();
        }
        
        model.addAttribute("pacientes", pacientes);
        return "recepcionista/pacientes";
    }
    
    /**
     * Formulario para registrar paciente
     */
    @GetMapping("/pacientes/nuevo")
    public String nuevoPacienteForm(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "recepcionista/paciente-form";
    }
    
    /**
     * Guardar paciente
     */
    @PostMapping("/pacientes/guardar")
    public String guardarPaciente(@ModelAttribute Usuario usuario,
                                 RedirectAttributes redirectAttributes,
                                 Authentication authentication) {
        try {
            // Validar duplicados
            if (usuarioService.existeUsername(usuario.getUsername())) {
                redirectAttributes.addFlashAttribute("error", "El nombre de usuario ya existe");
                return "redirect:/recepcionista/pacientes/nuevo";
            }
            
            if (usuarioService.existeEmail(usuario.getEmail())) {
                redirectAttributes.addFlashAttribute("error", "El email ya está registrado");
                return "redirect:/recepcionista/pacientes/nuevo";
            }
            
            if (usuarioService.existeNumeroDocumento(usuario.getNumeroDocumento())) {
                redirectAttributes.addFlashAttribute("error", "El número de documento ya está registrado");
                return "redirect:/recepcionista/pacientes/nuevo";
            }
            
            // Registrar como paciente
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            usuario.setRegistradoPor(userDetails.getId());
            
            usuarioService.registrarPaciente(usuario);
            redirectAttributes.addFlashAttribute("mensaje", "Paciente registrado exitosamente");
            return "redirect:/recepcionista/pacientes";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al registrar: " + e.getMessage());
            return "redirect:/recepcionista/pacientes/nuevo";
        }
    }
    
    /**
     * Ver detalles de un paciente
     */
    @GetMapping("/pacientes/{id}")
    public String verPaciente(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Usuario> paciente = usuarioService.buscarPorId(id);
        
        if (paciente.isEmpty() || !paciente.get().esPaciente()) {
            redirectAttributes.addFlashAttribute("error", "Paciente no encontrado");
            return "redirect:/recepcionista/pacientes";
        }
        
        // Obtener citas del paciente
        List<Cita> citas = citaService.listarPorPaciente(id);
        
        model.addAttribute("paciente", paciente.get());
        model.addAttribute("citas", citas);
        
        return "recepcionista/paciente-detalle";
    }
    
    /**
     * Gestionar citas
     */
    @GetMapping("/citas")
    public String verCitas(@RequestParam(required = false) String fecha,
                          @RequestParam(required = false) String estado,
                          Model model) {
        List<Cita> citas;
        
        if (fecha != null && !fecha.isEmpty()) {
            LocalDate fechaBuscar = LocalDate.parse(fecha);
            citas = citaService.listarPorFecha(fechaBuscar);
            model.addAttribute("fechaSeleccionada", fecha);
        } else if (estado != null && !estado.isEmpty()) {
            citas = citaService.listarPorEstado(EstadoCita.valueOf(estado));
            model.addAttribute("estadoSeleccionado", estado);
        } else {
            citas = citaService.listarTodas();
        }
        
        model.addAttribute("citas", citas);
        model.addAttribute("estados", EstadoCita.values());
        
        return "recepcionista/citas";
    }
    
    /**
     * Formulario para crear cita
     */
    @GetMapping("/citas/nueva")
    public String nuevaCitaForm(Model model) {
        List<Usuario> pacientes = usuarioService.listarPacientesActivos();
        List<Usuario> doctores = usuarioService.listarDoctoresActivos();
        
        model.addAttribute("cita", new Cita());
        model.addAttribute("pacientes", pacientes);
        model.addAttribute("doctores", doctores);
        
        return "recepcionista/cita-form";
    }
    
    /**
     * Guardar cita
     */
    @PostMapping("/citas/guardar")
    public String guardarCita(@RequestParam String pacienteId,
                             @RequestParam String doctorId,
                             @RequestParam String fecha,
                             @RequestParam String horaInicio,
                             @RequestParam String horaFin,
                             @RequestParam String motivoConsulta,
                             RedirectAttributes redirectAttributes,
                             Authentication authentication) {
        try {
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
            
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            cita.setCreadaPor(userDetails.getId());
            
            // Validar disponibilidad
            if (!citaService.existeDisponibilidad(doctorId, horario.getFecha(), horaInicio)) {
                redirectAttributes.addFlashAttribute("error", "El horario seleccionado no está disponible");
                return "redirect:/recepcionista/citas/nueva";
            }
            
            citaService.guardar(cita);
            redirectAttributes.addFlashAttribute("mensaje", "Cita creada exitosamente");
            return "redirect:/recepcionista/citas";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear cita: " + e.getMessage());
            return "redirect:/recepcionista/citas/nueva";
        }
    }
    
    /**
     * Ver detalles de una cita
     */
    @GetMapping("/citas/{id}")
    public String verCita(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Cita> citaOpt = citaService.buscarPorId(id);
        
        if (citaOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Cita no encontrada");
            return "redirect:/recepcionista/citas";
        }
        
        Cita cita = citaOpt.get();
        
        // Obtener información adicional
        Optional<Usuario> paciente = usuarioService.buscarPorId(cita.getPacienteId());
        Optional<Usuario> doctor = usuarioService.buscarPorId(cita.getDoctorId());
        
        model.addAttribute("cita", cita);
        model.addAttribute("paciente", paciente.orElse(null));
        model.addAttribute("doctor", doctor.orElse(null));
        
        return "recepcionista/cita-detalle";
    }
    
    /**
     * Confirmar cita
     */
    @GetMapping("/citas/confirmar/{id}")
    public String confirmarCita(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            citaService.confirmarCita(id);
            redirectAttributes.addFlashAttribute("mensaje", "Cita confirmada exitosamente");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al confirmar cita: " + e.getMessage());
        }
        
        return "redirect:/recepcionista/citas/" + id;
    }
    
    /**
     * Cancelar cita
     */
    @GetMapping("/citas/cancelar/{id}")
    public String cancelarCita(@PathVariable String id,
                              @RequestParam(required = false) String motivo,
                              RedirectAttributes redirectAttributes,
                              Authentication authentication) {
        try {
            Optional<Cita> citaOpt = citaService.buscarPorId(id);
            
            if (citaOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Cita no encontrada");
                return "redirect:/recepcionista/citas";
            }
            
            Cita cita = citaOpt.get();
            
            // Verificar que la cita no esté completada
            if (cita.estaCompletada()) {
                redirectAttributes.addFlashAttribute("error", "No se puede cancelar una cita completada");
                return "redirect:/recepcionista/citas/" + id;
            }
            
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String motivoCancelacion = motivo != null ? motivo : "Cancelada por recepción";
            
            citaService.cancelarCita(id, userDetails.getId(), motivoCancelacion);
            redirectAttributes.addFlashAttribute("mensaje", "Cita cancelada exitosamente");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cancelar cita: " + e.getMessage());
        }
        
        return "redirect:/recepcionista/citas";
    }
    
    /**
     * Buscar citas por paciente
     */
    @GetMapping("/buscar-citas")
    public String buscarCitasPorPaciente(@RequestParam(required = false) String numeroDocumento,
                                        Model model,
                                        RedirectAttributes redirectAttributes) {
        if (numeroDocumento == null || numeroDocumento.isEmpty()) {
            return "recepcionista/buscar-citas";
        }
        
        try {
            Optional<Usuario> paciente = usuarioService.buscarPorNumeroDocumento(numeroDocumento);
            
            if (paciente.isEmpty() || !paciente.get().esPaciente()) {
                model.addAttribute("error", "No se encontró ningún paciente con ese número de documento");
                return "recepcionista/buscar-citas";
            }
            
            List<Cita> citas = citaService.listarPorPaciente(paciente.get().getId());
            
            model.addAttribute("paciente", paciente.get());
            model.addAttribute("citas", citas);
            
            return "recepcionista/buscar-citas";
            
        } catch (Exception e) {
            model.addAttribute("error", "Error al buscar citas: " + e.getMessage());
            return "recepcionista/buscar-citas";
        }
    }
    
    /**
     * Validar usuario (verificar si existe)
     */
    @GetMapping("/validar-usuario")
    public String validarUsuario() {
        return "recepcionista/validar-usuario";
    }
    
    /**
     * Procesar validación de usuario
     */
    @PostMapping("/validar-usuario")
    public String procesarValidacion(@RequestParam String numeroDocumento,
                                    Model model) {
        try {
            Optional<Usuario> usuario = usuarioService.buscarPorNumeroDocumento(numeroDocumento);
            
            if (usuario.isEmpty()) {
                model.addAttribute("mensaje", "Usuario no encontrado en el sistema");
                model.addAttribute("encontrado", false);
            } else {
                model.addAttribute("usuario", usuario.get());
                model.addAttribute("encontrado", true);
            }
            
        } catch (Exception e) {
            model.addAttribute("error", "Error al validar usuario: " + e.getMessage());
        }
        
        return "recepcionista/validar-usuario";
    }
}
