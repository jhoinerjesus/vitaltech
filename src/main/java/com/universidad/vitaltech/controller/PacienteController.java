package com.universidad.vitaltech.controller;

import com.universidad.vitaltech.config.CustomUserDetails;
import com.universidad.vitaltech.model.*;
import com.universidad.vitaltech.model.embedded.Horario;
import com.universidad.vitaltech.service.CitaService;
import com.universidad.vitaltech.service.DiagnosticoService;
import com.universidad.vitaltech.service.HorarioDisponibleService;
import com.universidad.vitaltech.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador para funcionalidades del Paciente
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

    @Autowired
    private HorarioDisponibleService horarioDisponibleService;

    /**
     * Panel del paciente
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
     * Obtener horarios disponibles de un doctor en una fecha específica
     * Endpoint para cargar los horarios disponibles
     */
    @GetMapping("/api/horarios-disponibles")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerHorariosDisponibles(
            @RequestParam String doctorId,
            @RequestParam String fecha) {
        try {
            LocalDate fechaSeleccionada = LocalDate.parse(fecha);

            // Validar que la fecha sea futura
            if (fechaSeleccionada.isBefore(LocalDate.now())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "No se pueden agendar citas en fechas pasadas"));
            }

            // Obtener horarios disponibles del doctor para ese día
            List<LocalTime> horariosDisponibles = horarioDisponibleService
                    .obtenerHorariosDisponibles(doctorId, fechaSeleccionada);

            if (horariosDisponibles.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "horarios", List.of(),
                        "mensaje", "El doctor no tiene horarios disponibles para esta fecha"));
            }

            // Filtrar horarios que ya tienen citas agendadas
            List<Cita> citasExistentes = citaService.listarPorDoctorYFecha(doctorId, fechaSeleccionada);
            List<LocalTime> horariosOcupados = citasExistentes.stream()
                    .filter(c -> !c.estaCancelada())
                    .map(c -> c.getHorario().getHoraInicio())
                    .toList();

            List<LocalTime> horariosLibres = horariosDisponibles.stream()
                    .filter(h -> !horariosOcupados.contains(h))
                    .toList();

            return ResponseEntity.ok(Map.of(
                    "horarios", horariosLibres,
                    "duracionCita", 30 // Duración en minutos
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error al obtener horarios: " + e.getMessage()));
        }
    }

    /**
     * Guardar nueva cita con VALIDACIONES
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

            LocalDate fechaCita = LocalDate.parse(fecha);
            LocalTime horaInicioCita = LocalTime.parse(horaInicio);
            LocalTime horaFinCita = LocalTime.parse(horaFin);

            // validacion

            // Validar que la fecha no sea pasada
            if (fechaCita.isBefore(LocalDate.now())) {
                redirectAttributes.addFlashAttribute("error",
                        "No se pueden agendar citas en fechas pasadas");
                return "redirect:/paciente/citas/nueva";
            }

            // Validar que el doctor exista y esté activo
            Optional<Usuario> doctorOpt = usuarioService.buscarPorId(doctorId);
            if (doctorOpt.isEmpty() || !doctorOpt.get().isActivo()) {
                redirectAttributes.addFlashAttribute("error",
                        "El doctor seleccionado no está disponible");
                return "redirect:/paciente/citas/nueva";
            }

            // validacion Verificar que el doctor tenga ese horario disponible
            if (!horarioDisponibleService.existeHorarioDisponible(doctorId, fechaCita, horaInicioCita)) {
                redirectAttributes.addFlashAttribute("error",
                        "⚠️ El horario seleccionado NO está disponible. " +
                                "El doctor no atiende en ese día u horario. " +
                                "Por favor, seleccione un horario de la lista de disponibles.");
                return "redirect:/paciente/citas/nueva";
            }

            // Validar que no exista otra cita en ese horario
            if (!citaService.existeDisponibilidad(doctorId, fechaCita, horaInicio)) {
                redirectAttributes.addFlashAttribute("error",
                        "El horario seleccionado ya está ocupado. " +
                                "Por favor, elija otro horario disponible.");
                return "redirect:/paciente/citas/nueva";
            }

            // Validar duración de la cita
            long duracionMinutos = java.time.Duration.between(horaInicioCita, horaFinCita).toMinutes();
            if (duracionMinutos > 30 || duracionMinutos <= 0) {
                redirectAttributes.addFlashAttribute("error",
                        "La duración de la cita debe ser máximo 30 minutos");
                return "redirect:/paciente/citas/nueva";
            }

            // crear la cita
            Cita cita = new Cita();
            cita.setPacienteId(pacienteId);
            cita.setDoctorId(doctorId);
            cita.setMotivoConsulta(motivoConsulta);

            // Crear horario
            Horario horario = new Horario();
            horario.setFecha(fechaCita);
            horario.setHoraInicio(horaInicioCita);
            horario.setHoraFin(horaFinCita);

            cita.setHorario(horario);
            cita.setCreadaPor(pacienteId);

            citaService.guardar(cita);

            redirectAttributes.addFlashAttribute("mensaje",
                    "Cita agendada exitosamente para el " +
                            fechaCita.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                            " a las " + horaInicioCita);
            return "redirect:/paciente/citas";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error",
                    "Formato de fecha u hora inválido: " + e.getMessage());
            return "redirect:/paciente/citas/nueva";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error al agendar cita: " + e.getMessage());
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

            // Verificar que la cita no este completada
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