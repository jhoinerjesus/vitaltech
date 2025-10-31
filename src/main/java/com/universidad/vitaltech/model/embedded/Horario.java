package com.universidad.vitaltech.model.embedded;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Documento embebido para horarios de citas
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Horario {
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    
    /**
     * Valida que la duración de la cita no exceda 30 minutos
     */
    public boolean validarDuracion() {
        if (horaInicio == null || horaFin == null) {
            return false;
        }
        long minutos = java.time.Duration.between(horaInicio, horaFin).toMinutes();
        return minutos > 0 && minutos <= 30;
    }
    
    @Override
    public String toString() {
        return String.format("%s de %s a %s", fecha, horaInicio, horaFin);
    }
}