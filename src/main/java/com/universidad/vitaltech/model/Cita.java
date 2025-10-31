package com.universidad.vitaltech.model;

import com.universidad.vitaltech.model.embedded.Horario;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Entidad Cita - Almacena las citas médicas
 * Usa lookup para relacionarse con Usuario (paciente y doctor)
 */
@Document(collection = "citas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cita {
    
    @Id
    private String id;
    
    // Referencias a usuarios (se usará $lookup en queries)
    @NotNull(message = "El paciente es obligatorio")
    private String pacienteId;
    
    @NotNull(message = "El doctor es obligatorio")
    private String doctorId;
    
    // Documento embebido - Horario de la cita
    @NotNull(message = "El horario es obligatorio")
    private Horario horario;
    
    @NotNull(message = "El estado es obligatorio")
    private EstadoCita estado = EstadoCita.PROGRAMADA;
    
    private String motivoConsulta;
    private String observaciones;
    
    // Auditoría
    private LocalDateTime fechaCreacion = LocalDateTime.now();
    private LocalDateTime fechaActualizacion;
    private String creadaPor; // ID del usuario que creó la cita
    private String canceladaPor; // ID del usuario que canceló (si aplica)
    private String motivoCancelacion;
    
    // Métodos de utilidad
    public boolean estaProgramada() {
        return this.estado == EstadoCita.PROGRAMADA || this.estado == EstadoCita.CONFIRMADA;
    }
    
    public boolean estaCompletada() {
        return this.estado == EstadoCita.COMPLETADA;
    }
    
    public boolean estaCancelada() {
        return this.estado == EstadoCita.CANCELADA;
    }
    
    public void cancelar(String usuarioId, String motivo) {
        this.estado = EstadoCita.CANCELADA;
        this.canceladaPor = usuarioId;
        this.motivoCancelacion = motivo;
        this.fechaActualizacion = LocalDateTime.now();
    }
    
    public void completar() {
        this.estado = EstadoCita.COMPLETADA;
        this.fechaActualizacion = LocalDateTime.now();
    }
    
    public void confirmar() {
        this.estado = EstadoCita.CONFIRMADA;
        this.fechaActualizacion = LocalDateTime.now();
    }
}