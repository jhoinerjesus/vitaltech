package com.universidad.vitaltech.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.universidad.vitaltech.model.embedded.Horario;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad Cita para Almacena las citas medicas
 * y uso un lookup manual para hacer la relaciones con Usuario del paiente y el doctor
 */
@Document(collection = "citas")
@CompoundIndex(name = "paciente_fecha_idx", def = "{'pacienteId': 1, 'horario.fecha': -1}")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cita {
    
    @Id
    private String id;
    
    // Referencia para los usuarios 
    @NotNull(message = "El paciente es obligatorio")
    @Indexed
    private String pacienteId;
    
    @NotNull(message = "El doctor es obligatorio")
    @Indexed
    private String doctorId;
    
    // Documento embebido para los horario de la cita
    @NotNull(message = "El horario es obligatorio")
    private Horario horario;
    
    @NotNull(message = "El estado es obligatorio")
    @Indexed
    private EstadoCita estado = EstadoCita.PROGRAMADA;
    
    private String motivoConsulta;
    private String observaciones;
    
    // Auditoría
    private LocalDateTime fechaCreacion = LocalDateTime.now();
    private LocalDateTime fechaActualizacion;
    private String creadaPor; // ID del usuario que creo la cita
    private String canceladaPor; // ID del usuario que canceló (si aplica)
    private String motivoCancelacion;
    
    // Metodos de utilidad
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