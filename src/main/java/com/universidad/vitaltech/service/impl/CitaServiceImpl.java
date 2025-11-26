package com.universidad.vitaltech.service.impl;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.universidad.vitaltech.model.Cita;
import com.universidad.vitaltech.model.EstadoCita;
import com.universidad.vitaltech.repository.CitaRepository;
import com.universidad.vitaltech.service.CitaService;

@Service
public class CitaServiceImpl implements CitaService {

    private static final Logger log = LoggerFactory.getLogger(CitaServiceImpl.class);

    // Constantes configurables
    private static final int MINUTOS_ANTES_PERMITIDOS = 30;
    private static final int HORAS_DESPUES_PERMITIDAS = 2;

    // Zona horaria desde configuración
    @Value("${app.timezone:America/Bogota}")
    private String appTimezone;

    @Autowired
    private CitaRepository citaRepository;

    /**
     * Obtiene la hora actual en la zona horaria configurada
     */
    private LocalDateTime obtenerHoraActual() {
        ZoneId zoneId = ZoneId.of(appTimezone);
        LocalDateTime ahora = LocalDateTime.now(zoneId);
        log.debug("Hora actual en zona {}: {}", appTimezone, ahora);
        return ahora;
    }

    @Override
    public Cita guardar(Cita cita) {
        // Validar que la duración de la cita no exceda 30 minutos
        if (!cita.getHorario().validarDuracion()) {
            throw new RuntimeException("La duración de la cita debe ser máximo 30 minutos");
        }

        // Establecer fecha de creación si es nueva
        if (cita.getId() == null) {
            cita.setFechaCreacion(obtenerHoraActual());
            cita.setEstado(EstadoCita.PROGRAMADA);
        } else {
            cita.setFechaActualizacion(obtenerHoraActual());
        }

        return citaRepository.save(cita);
    }

    @Override
    public Optional<Cita> buscarPorId(String id) {
        return citaRepository.findById(id);
    }

    @Override
    public List<Cita> listarTodas() {
        return citaRepository.findAll();
    }

    @Override
    public void eliminar(String id) {
        citaRepository.deleteById(id);
    }

    @Override
    public List<Cita> listarPorPaciente(String pacienteId) {
        return citaRepository.findByPacienteId(pacienteId);
    }

    @Override
    public List<Cita> listarPorPacienteYEstado(String pacienteId, EstadoCita estado) {
        return citaRepository.findByPacienteIdAndEstado(pacienteId, estado);
    }

    @Override
    public List<Cita> listarPorDoctor(String doctorId) {
        return citaRepository.findByDoctorId(doctorId);
    }

    @Override
    public List<Cita> listarPorDoctorYEstado(String doctorId, EstadoCita estado) {
        return citaRepository.findByDoctorIdAndEstado(doctorId, estado);
    }

    @Override
    public List<Cita> listarPorDoctorYFecha(String doctorId, LocalDate fecha) {
        return citaRepository.findByDoctorIdAndFecha(doctorId, fecha);
    }

    @Override
    public List<Cita> listarCitasProgramadasPorDoctorYFecha(String doctorId, LocalDate fecha) {
        return citaRepository.findCitasProgramadasByDoctorAndFecha(doctorId, fecha);
    }

    @Override
    public List<Cita> listarPorFecha(LocalDate fecha) {
        return citaRepository.findByFecha(fecha);
    }

    @Override
    public List<Cita> listarPorEstado(EstadoCita estado) {
        return citaRepository.findByEstado(estado);
    }

    @Override
    public Cita confirmarCita(String citaId) {
        Optional<Cita> citaOpt = buscarPorId(citaId);
        if (citaOpt.isPresent()) {
            Cita cita = citaOpt.get();
            cita.confirmar();
            return citaRepository.save(cita);
        }
        throw new RuntimeException("Cita no encontrada");
    }

    @Override
    public Cita completarCita(String citaId) {
        Optional<Cita> citaOpt = buscarPorId(citaId);
        if (citaOpt.isPresent()) {
            Cita cita = citaOpt.get();
            cita.completar();
            return citaRepository.save(cita);
        }
        throw new RuntimeException("Cita no encontrada");
    }

    @Override
    public Cita cancelarCita(String citaId, String usuarioId, String motivo) {
        Optional<Cita> citaOpt = buscarPorId(citaId);
        if (citaOpt.isPresent()) {
            Cita cita = citaOpt.get();
            cita.cancelar(usuarioId, motivo);
            return citaRepository.save(cita);
        }
        throw new RuntimeException("Cita no encontrada");
    }

    @Override
    public boolean existeDisponibilidad(String doctorId, LocalDate fecha, String horaInicio) {
        List<Cita> citasExistentes = listarCitasProgramadasPorDoctorYFecha(doctorId, fecha);

        for (Cita cita : citasExistentes) {
            String horaInicioCita = cita.getHorario().getHoraInicio().toString();
            if (horaInicioCita.equals(horaInicio)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public List<Object> obtenerCitasConDetallesPorPaciente(String pacienteId) {
        return citaRepository.findCitasConDetallesByPacienteId(pacienteId);
    }

    @Override
    public List<Object> obtenerCitasConDetallesPorDoctor(String doctorId) {
        return citaRepository.findCitasConDetallesByDoctorId(doctorId);
    }

    @Override
    public List<Object> obtenerCitasConDetallesPorFecha(LocalDate fecha) {
        return citaRepository.findCitasConDetallesByFecha(fecha.toString());
    }

    @Override
    public long contarCitasPorDoctorYEstado(String doctorId, EstadoCita estado) {
        return citaRepository.countByDoctorIdAndEstado(doctorId, estado);
    }

    @Override
    public long contarCitasPorPacienteYEstado(String pacienteId, EstadoCita estado) {
        return citaRepository.countByPacienteIdAndEstado(pacienteId, estado);
    }

    /**
     * Valida si una cita puede ser atendida en este momento.
     * Ventana permitida: 30 minutos antes hasta 2 horas después.
     */
    @Override
    public boolean puedeSerAtendida(Cita cita) {
        // 1. Validar que la cita esté confirmada
        if (cita.getEstado() != EstadoCita.CONFIRMADA) {
            log.debug("Cita {} rechazada: estado no es CONFIRMADA (actual: {})",
                    cita.getId(), cita.getEstado());
            return false;
        }

        // 2. Obtener fecha/hora actual en la zona horaria correcta
        LocalDateTime ahora = obtenerHoraActual();

        // 3. Construir ventana de tiempo
        LocalDateTime inicioCita = LocalDateTime.of(
                cita.getHorario().getFecha(),
                cita.getHorario().getHoraInicio());
        LocalDateTime finCita = LocalDateTime.of(
                cita.getHorario().getFecha(),
                cita.getHorario().getHoraFin());

        // 4. Calcular ventanas
        LocalDateTime ventanaInicio = inicioCita.minusMinutes(MINUTOS_ANTES_PERMITIDOS);
        LocalDateTime ventanaFin = finCita.plusHours(HORAS_DESPUES_PERMITIDAS);

        // 5. Log detallado para depuración
        log.debug("=== Validación de cita {} (Zona: {}) ===", cita.getId(), appTimezone);
        log.debug("Ahora: {}", ahora);
        log.debug("Inicio cita: {}", inicioCita);
        log.debug("Fin cita: {}", finCita);
        log.debug("Ventana inicio: {}", ventanaInicio);
        log.debug("Ventana fin: {}", ventanaFin);
        log.debug("¿Después del inicio?: {}", ahora.isAfter(ventanaInicio));
        log.debug("¿Antes del fin?: {}", ahora.isBefore(ventanaFin));

        // 6. Validar que estemos dentro de la ventana
        boolean puedeAtender = ahora.isAfter(ventanaInicio) && ahora.isBefore(ventanaFin);

        if (!puedeAtender) {
            log.info("Cita {} NO puede ser atendida. Ahora: {}, Ventana: {} - {}",
                    cita.getId(), ahora, ventanaInicio, ventanaFin);
        } else {
            log.info("✅ Cita {} SÍ puede ser atendida. Ahora: {} está dentro de {} - {}",
                    cita.getId(), ahora, ventanaInicio, ventanaFin);
        }

        return puedeAtender;
    }

    @Override
    public String obtenerMensajeBloqueo(Cita cita) {
        // 1. Validar estado
        if (cita.getEstado() != EstadoCita.CONFIRMADA) {
            return "La cita debe estar en estado CONFIRMADA para registrar un diagnóstico. " +
                    "Estado actual: " + cita.getEstado();
        }

        // 2. Calcular ventanas de tiempo (usando zona horaria correcta)
        LocalDateTime ahora = obtenerHoraActual();
        LocalDateTime inicioCita = LocalDateTime.of(
                cita.getHorario().getFecha(),
                cita.getHorario().getHoraInicio());
        LocalDateTime finCita = LocalDateTime.of(
                cita.getHorario().getFecha(),
                cita.getHorario().getHoraFin());

        LocalDateTime ventanaInicio = inicioCita.minusMinutes(MINUTOS_ANTES_PERMITIDOS);
        LocalDateTime ventanaFin = finCita.plusHours(HORAS_DESPUES_PERMITIDAS);

        // 3. Formateador para mostrar horas
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Log para depuración
        log.warn("Cita {} bloqueada. Ahora: {} ({}), Ventana: {} - {}",
                cita.getId(), ahora, appTimezone, ventanaInicio, ventanaFin);

        // 4. Mensajes específicos según el momento
        if (ahora.isBefore(ventanaInicio)) {
            long minutosRestantes = Duration.between(ahora, ventanaInicio).toMinutes();

            if (minutosRestantes > 60) {
                long horasRestantes = minutosRestantes / 60;
                long minutosExtra = minutosRestantes % 60;
                return String.format(
                        "⏰ La cita aún no puede ser atendida. Podrá registrar el diagnóstico en %d hora(s) y %d minuto(s), "
                                +
                                "a partir de las %s del día %s (%d minutos antes de la cita programada).",
                        horasRestantes,
                        minutosExtra,
                        ventanaInicio.format(timeFormatter),
                        ventanaInicio.format(dateFormatter),
                        MINUTOS_ANTES_PERMITIDOS);
            } else {
                return String.format(
                        "⏰ La cita aún no puede ser atendida. Podrá registrar el diagnóstico en %d minuto(s), " +
                                "a partir de las %s (%d minutos antes de la cita programada).",
                        minutosRestantes,
                        ventanaInicio.format(timeFormatter),
                        MINUTOS_ANTES_PERMITIDOS);
            }
        }

        if (ahora.isAfter(ventanaFin)) {
            return String.format(
                    "⌛ El tiempo para registrar el diagnóstico de esta cita ha expirado. " +
                            "Solo se pueden registrar diagnósticos hasta %d horas después de finalizada la cita. " +
                            "La ventana de registro cerró el %s a las %s.",
                    HORAS_DESPUES_PERMITIDAS,
                    ventanaFin.format(dateFormatter),
                    ventanaFin.format(timeFormatter));
        }

        return "❌ No se puede registrar el diagnóstico en este momento. Por favor, contacte al administrador.";
    }
}