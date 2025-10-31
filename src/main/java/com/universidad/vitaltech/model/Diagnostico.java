package com.universidad.vitaltech.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Diagnostico - Almacena diagnósticos médicos
 * Usa lookup para relacionarse con Cita, Paciente y Doctor
 */
@Document(collection = "diagnosticos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Diagnostico {
    
    @Id
    private String id;
    
    // Referencias (se usará $lookup en queries)
    @NotNull(message = "La cita es obligatoria")
    private String citaId;
    
    @NotNull(message = "El paciente es obligatorio")
    private String pacienteId;
    
    @NotNull(message = "El doctor es obligatorio")
    private String doctorId;
    
    // Datos del diagnóstico
    @NotBlank(message = "El diagnóstico es obligatorio")
    private String diagnostico;
    
    private String sintomas;
    private String tratamiento;
    private List<String> medicamentos = new ArrayList<>();
    private String observaciones;
    private String recomendaciones;
    
    // Signos vitales
    private String presionArterial;
    private Double temperatura;
    private Integer frecuenciaCardiaca;
    private Integer frecuenciaRespiratoria;
    private Double peso;
    private Double altura;
    
    // Exámenes y procedimientos
    private List<String> examenesOrdenados = new ArrayList<>();
    private String proximaConsulta; // Fecha sugerida
    
    // Auditoría
    private LocalDateTime fechaCreacion = LocalDateTime.now();
    private LocalDateTime fechaActualizacion;
    
    // Métodos de utilidad
    public void agregarMedicamento(String medicamento) {
        if (this.medicamentos == null) {
            this.medicamentos = new ArrayList<>();
        }
        this.medicamentos.add(medicamento);
    }
    
    public void agregarExamen(String examen) {
        if (this.examenesOrdenados == null) {
            this.examenesOrdenados = new ArrayList<>();
        }
        this.examenesOrdenados.add(examen);
    }
    
    public Double calcularIMC() {
        if (peso != null && altura != null && altura > 0) {
            return peso / (altura * altura);
        }
        return null;
    }
}