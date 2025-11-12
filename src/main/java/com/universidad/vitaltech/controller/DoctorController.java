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

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String doctorId = userDetails.getId();

        long citasHoy = citaService.listarPorDoctorYFecha(doctorId, LocalDate.now()).size();
        long citasPendientes = citaService.contarCitasPorDoctorYEstado(doctorId, EstadoCita.PROGRAMADA)
                + citaService.contarCitasPorDoctorYEstado(doctorId, EstadoCita.CONFIRMADA);
        long totalDiagnosticos = diagnosticoService.contarDiagnosticosPorDoctor(doctorId);

        model.addAttribute("citasHoy", citasHoy);
        model.addAttribute("citasPendientes", citasPendientes);
        model.addAttribute("totalDiagnosticos", totalDiagnosticos);
        model.addAttribute("citasHoyLista", citaService.listarPorDoctorYFecha(doctorId, LocalDate.now()));

        return "doctor/dashboard";
    }

    @GetMapping("/citas")
    public String verCitas(@RequestParam(required = false) String fecha,
                           Model model,
                           Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String doctorId = userDetails.getId();

        List<Cita> citas = (fecha != null && !fecha.isEmpty())
                ? citaService.listarPorDoctorYFecha(doctorId, LocalDate.parse(fecha))
                : citaService.listarPorDoctor(doctorId);

        model.addAttribute("citas", citas);
        model.addAttribute("fechaSeleccionada", fecha);

        return "doctor/citas";
    }

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
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        if (!cita.getDoctorId().equals(userDetails.getId())) {
            redirectAttributes.addFlashAttribute("error", "No tienes permiso para ver esta cita");
            return "redirect:/doctor/citas";
        }

        Optional<Usuario> paciente = usuarioService.buscarPorId(cita.getPacienteId());
        Optional<Diagnostico> diagnostico = diagnosticoService.buscarPorCita(id);

        model.addAttribute("cita", cita);
        model.addAttribute("paciente", paciente.orElse(null));
        model.addAttribute("diagnostico", diagnostico.orElse(null));

        return "doctor/cita-detalle";
    }

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
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        if (!cita.getDoctorId().equals(userDetails.getId())) {
            redirectAttributes.addFlashAttribute("error", "No tienes permiso para crear diagnóstico en esta cita");
            return "redirect:/doctor/citas";
        }

        // ✅ CORRECCIÓN IMPORTANTE
        if (cita.getEstado() != EstadoCita.CONFIRMADA) {
            redirectAttributes.addFlashAttribute("error", "La cita debe estar CONFIRMADA para registrar diagnóstico");
            return "redirect:/doctor/citas/" + id;
        }

        if (diagnosticoService.existeDiagnosticoParaCita(id)) {
            redirectAttributes.addFlashAttribute("error", "Ya existe un diagnóstico para esta cita");
            return "redirect:/doctor/citas/" + id;
        }

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

    @PostMapping("/diagnostico/guardar")
    public String guardarDiagnostico(@ModelAttribute Diagnostico diagnostico,
                                     RedirectAttributes redirectAttributes) {
        try {
            diagnosticoService.guardar(diagnostico);
            citaService.completarCita(diagnostico.getCitaId());

            redirectAttributes.addFlashAttribute("mensaje", "Diagnóstico guardado exitosamente");
            return "redirect:/doctor/citas/" + diagnostico.getCitaId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar diagnóstico: " + e.getMessage());
            return "redirect:/doctor/citas/" + diagnostico.getCitaId() + "/diagnostico";
        }
    }

    @GetMapping("/diagnosticos")
    public String verDiagnosticos(Model model, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        model.addAttribute("diagnosticos", diagnosticoService.listarPorDoctor(userDetails.getId()));
        return "doctor/diagnosticos";
    }

    @GetMapping("/horarios")
    public String verHorarios(Model model, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        model.addAttribute("horarios", horarioDisponibleService.listarActivosPorDoctor(userDetails.getId()));
        return "doctor/horarios";
    }

    @GetMapping("/horarios/nuevo")
    public String nuevoHorarioForm(Model model) {
        model.addAttribute("horario", new HorarioDisponible());
        model.addAttribute("diasSemana", DayOfWeek.values());
        return "doctor/horario-form";
    }

    @PostMapping("/horarios/guardar")
    public String guardarHorario(@ModelAttribute HorarioDisponible horario,
                                 RedirectAttributes redirectAttributes,
                                 Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            horario.setDoctorId(userDetails.getId());
            horarioDisponibleService.guardar(horario);
            redirectAttributes.addFlashAttribute("mensaje", "Horario guardado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar horario: " + e.getMessage());
            return "redirect:/doctor/horarios/nuevo";
        }
        return "redirect:/doctor/horarios";
    }

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
