package com.universidad.vitaltech.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad HorarioDisponible - Gestiona la disponibilidad de los doctores
 */
@Document(collection = "horarios_disponibles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HorarioDisponible {
    
    @Id
    private String id;
    
    // Referencia al doctor
    @NotNull(message = "El doctor es obligatorio")
    private String doctorId;
    
    // Día de la semana (MONDAY, TUESDAY, etc.)
    @NotNull(message = "El día de la semana es obligatorio")
    private DayOfWeek diaSemana;
    
    // Hora de inicio del turno
    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime horaInicio;
    
    // Hora de fin del turno
    @NotNull(message = "La hora de fin es obligatoria")
    private LocalTime horaFin;
    
    // Duración de cada cita en minutos (máximo 30)
    private Integer duracionCita = 30;
    
    // Si está activo o no
    private boolean activo = true;
    
    // Fechas específicas de excepción (vacaciones, días no laborables)
    private List<LocalDate> fechasNoDisponibles = new ArrayList<>();
    
    // Métodos de utilidad
    public boolean validarHorario() {
        if (horaInicio == null || horaFin == null) {
            return false;
        }
        return horaInicio.isBefore(horaFin);
    }
    
    public int calcularCantidadCitas() {
        if (!validarHorario() || duracionCita == null || duracionCita <= 0) {
            return 0;
        }
        long minutosDisponibles = java.time.Duration.between(horaInicio, horaFin).toMinutes();
        return (int) (minutosDisponibles / duracionCita);
    }
    
    public void agregarFechaNoDisponible(LocalDate fecha) {
        if (this.fechasNoDisponibles == null) {
            this.fechasNoDisponibles = new ArrayList<>();
        }
        this.fechasNoDisponibles.add(fecha);
    }
    
    public boolean estaDisponibleEnFecha(LocalDate fecha) {
        if (fechasNoDisponibles == null) {
            return true;
        }
        return !fechasNoDisponibles.contains(fecha);
    }
    
    public List<LocalTime> generarHorariosDisponibles() {
        List<LocalTime> horarios = new ArrayList<>();
        if (!validarHorario() || duracionCita == null) {
            return horarios;
        }
        
        LocalTime horaActual = horaInicio;
        while (horaActual.plusMinutes(duracionCita).isBefore(horaFin) || 
               horaActual.plusMinutes(duracionCita).equals(horaFin)) {
            horarios.add(horaActual);
            horaActual = horaActual.plusMinutes(duracionCita);
        }
        
        return horarios;
    }
}