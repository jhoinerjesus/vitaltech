package com.universidad.vitaltech.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import com.universidad.vitaltech.config.CustomUserDetails;
import com.universidad.vitaltech.model.Cita;
import com.universidad.vitaltech.model.Diagnostico;
import com.universidad.vitaltech.model.EstadoCita;
import com.universidad.vitaltech.model.HorarioDisponible;
import com.universidad.vitaltech.model.Usuario;
import com.universidad.vitaltech.service.CitaService;
import com.universidad.vitaltech.service.DiagnosticoService;
import com.universidad.vitaltech.service.HorarioDisponibleService;
import com.universidad.vitaltech.service.UsuarioService;

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
            @RequestParam(required = false) String filtro,
            Model model,
            Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String doctorId = userDetails.getId();

        System.out.println("========================================");
        System.out.println("🚀 INICIO DEL MÉTODO verCitas()");
        System.out.println("   Doctor ID: " + doctorId);
        System.out.println("   Fecha: " + fecha);
        System.out.println("   Filtro: " + filtro);
        System.out.println("========================================");

        List<Cita> todasLasCitas;

        // Si hay filtro de fecha específica
        if (fecha != null && !fecha.isEmpty()) {
            todasLasCitas = citaService.listarPorDoctorYFecha(doctorId, LocalDate.parse(fecha));
        } else {
            todasLasCitas = citaService.listarPorDoctor(doctorId);
        }

        // IMPORTANTE: Validar que no sea null
        if (todasLasCitas == null) {
            todasLasCitas = new ArrayList<>();
        }

        System.out.println("📊 Total citas del doctor: " + todasLasCitas.size());

        LocalDate hoy = LocalDate.now();

        // Clasificar las citas
        List<Cita> citasHoy = new ArrayList<>();
        List<Cita> citasProximas = new ArrayList<>();
        List<Cita> citasAntiguas = new ArrayList<>();

        for (Cita cita : todasLasCitas) {
            LocalDate fechaCita = cita.getHorario().getFecha();

            if (fechaCita.equals(hoy)) {
                citasHoy.add(cita);
            } else if (fechaCita.isAfter(hoy)) {
                citasProximas.add(cita);
            } else {
                citasAntiguas.add(cita);
            }
        }

        citasHoy.sort((c1, c2) -> c1.getHorario().getHoraInicio().compareTo(c2.getHorario().getHoraInicio()));
        citasProximas.sort((c1, c2) -> c1.getHorario().getFecha().compareTo(c2.getHorario().getFecha()));
        citasAntiguas.sort((c1, c2) -> c2.getHorario().getFecha().compareTo(c1.getHorario().getFecha()));

        System.out.println("📅 Citas HOY: " + citasHoy.size());
        System.out.println("📅 Citas PRÓXIMAS: " + citasProximas.size());
        System.out.println("📅 Citas ANTIGUAS: " + citasAntiguas.size());

        List<Cita> citasMostrar;
        String filtroActivo = filtro != null ? filtro : "hoy";

        switch (filtroActivo) {
            case "proximas":
                citasMostrar = citasProximas;
                break;
            case "antiguas":
                citasMostrar = citasAntiguas;
                break;
            case "todas":
                citasMostrar = todasLasCitas;
                break;
            default:
                citasMostrar = citasHoy;
                break;
        }

        System.out.println("========================================");
        System.out.println("🔍 Filtro activo: " + filtroActivo);
        System.out.println("🔍 Citas a mostrar: " + citasMostrar.size());
        System.out.println("========================================");

        // CARGAR INFORMACIÓN COMPLETA DE PACIENTES (nombre Y documento)
        Map<String, String> nombresPacientes = new HashMap<>();
        Map<String, String> documentosPacientes = new HashMap<>();

        System.out.println("👥 Iniciando carga de información de pacientes...");

        for (Cita cita : citasMostrar) {
            System.out.println("📋 Procesando cita ID: " + cita.getId());
            System.out.println("   - Paciente ID: " + cita.getPacienteId());
            System.out.println("   - Es null?: " + (cita.getPacienteId() == null));

            if (cita.getPacienteId() != null && !nombresPacientes.containsKey(cita.getPacienteId())) {
                try {
                    System.out.println("🔍 Buscando paciente con ID: " + cita.getPacienteId());
                    Optional<Usuario> pacienteOpt = usuarioService.buscarPorId(cita.getPacienteId());

                    if (pacienteOpt.isPresent()) {
                        Usuario paciente = pacienteOpt.get();
                        System.out.println("✅ Paciente encontrado: " + paciente.getNombreCompleto());
                        System.out.println("   - Documento: " + paciente.getNumeroDocumento());
                        // Guardar nombre completo
                        nombresPacientes.put(paciente.getId(), paciente.getNombreCompleto());
                        // Guardar número de documento
                        documentosPacientes.put(paciente.getId(), paciente.getNumeroDocumento());
                    } else {
                        System.out.println("❌ Paciente NO encontrado con ID: " + cita.getPacienteId());
                        // Fallback si no se encuentra el paciente
                        nombresPacientes.put(cita.getPacienteId(), "Paciente no encontrado");
                        documentosPacientes.put(cita.getPacienteId(), "N/A");
                    }
                } catch (Exception e) {
                    System.out.println("💥 Error al buscar paciente: " + e.getMessage());
                    e.printStackTrace();
                    // Si falla, poner valores por defecto
                    nombresPacientes.put(cita.getPacienteId(), "Error al cargar paciente");
                    documentosPacientes.put(cita.getPacienteId(), "N/A");
                }
            } else {
                System.out.println("⚠️ PacienteId es NULL o ya está en cache");
            }
        }

        System.out.println("========================================");
        System.out.println("📊 Total pacientes cargados: " + nombresPacientes.size());
        System.out.println("📊 Nombres: " + nombresPacientes);
        System.out.println("📊 Documentos: " + documentosPacientes);
        System.out.println("========================================");

        // SIEMPRE agregar todos los atributos al modelo
        model.addAttribute("citas", citasMostrar != null ? citasMostrar : new ArrayList<>());
        model.addAttribute("nombresPacientes", nombresPacientes);
        model.addAttribute("documentosPacientes", documentosPacientes);
        model.addAttribute("fechaSeleccionada", fecha != null ? fecha : "");
        model.addAttribute("filtroActivo", filtroActivo);
        model.addAttribute("totalHoy", citasHoy.size());
        model.addAttribute("totalProximas", citasProximas.size());
        model.addAttribute("totalAntiguas", citasAntiguas.size());
        model.addAttribute("totalTodas", todasLasCitas.size());

        System.out.println("🏁 FIN DEL MÉTODO verCitas()");
        System.out.println("========================================");

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

        if (!citaService.puedeSerAtendida(cita)) {
            String mensajeBloqueo = citaService.obtenerMensajeBloqueo(cita);
            redirectAttributes.addFlashAttribute("error", mensajeBloqueo);
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
        List<HorarioDisponible> horarios = horarioDisponibleService.listarActivosPorDoctor(userDetails.getId());

        // Crear un mapa para determinar el estado de cada horario
        Map<String, String> estadosHorarios = new HashMap<>();
        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();
        DayOfWeek diaActual = hoy.getDayOfWeek();

        for (HorarioDisponible horario : horarios) {
            String estado = "Activo";

            // Si el horario es para hoy y ya pasó la hora de fin
            if (horario.getDiaSemana().equals(diaActual) && ahora.isAfter(horario.getHoraFin())) {
                estado = "Finalizado";
            }

            estadosHorarios.put(horario.getId(), estado);
        }

        model.addAttribute("horarios", horarios);
        model.addAttribute("estadosHorarios", estadosHorarios);

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
            // Debug: Imprimir datos recibidos
            System.out.println("========================================");
            System.out.println("🕐 GUARDANDO HORARIO");
            System.out.println("   Día Semana: " + horario.getDiaSemana());
            System.out.println("   Hora Inicio: " + horario.getHoraInicio());
            System.out.println("   Hora Fin: " + horario.getHoraFin());
            System.out.println("   Duración Cita: " + horario.getDuracionCita());
            System.out.println("   Activo: " + horario.isActivo());
            System.out.println("========================================");

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            horario.setDoctorId(userDetails.getId());

            // Validaciones básicas
            if (horario.getDiaSemana() == null) {
                redirectAttributes.addFlashAttribute("error", "Debe seleccionar un día de la semana");
                return "redirect:/doctor/horarios/nuevo";
            }

            if (horario.getHoraInicio() == null || horario.getHoraFin() == null) {
                redirectAttributes.addFlashAttribute("error", "Las horas son obligatorias");
                return "redirect:/doctor/horarios/nuevo";
            }

            if (horario.getHoraInicio().isAfter(horario.getHoraFin()) ||
                    horario.getHoraInicio().equals(horario.getHoraFin())) {
                redirectAttributes.addFlashAttribute("error", "La hora de inicio debe ser antes de la hora de fin");
                return "redirect:/doctor/horarios/nuevo";
            }

            if (horario.getDuracionCita() == null || horario.getDuracionCita() <= 0) {
                redirectAttributes.addFlashAttribute("error", "La duración de la cita es obligatoria");
                return "redirect:/doctor/horarios/nuevo";
            }

            horarioDisponibleService.guardar(horario);

            System.out.println("✅ Horario guardado exitosamente con ID: " + horario.getId());
            redirectAttributes.addFlashAttribute("mensaje", "Horario guardado exitosamente");

        } catch (Exception e) {
            System.out.println("❌ ERROR al guardar horario:");
            e.printStackTrace();
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