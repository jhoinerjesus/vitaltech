package com.universidad.vitaltech.model.embedded;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Documento embebido para información médica del paciente
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InformacionMedica {
    private String tipoSangre;
    private List<String> alergias = new ArrayList<>();
    private List<String> enfermedadesCronicas = new ArrayList<>();
    private List<String> medicamentosActuales = new ArrayList<>();
    private String contactoEmergencia;
    private String telefonoEmergencia;
    
    public void agregarAlergia(String alergia) {
        if (this.alergias == null) {
            this.alergias = new ArrayList<>();
        }
        this.alergias.add(alergia);
    }
    
    public void agregarEnfermedadCronica(String enfermedad) {
        if (this.enfermedadesCronicas == null) {
            this.enfermedadesCronicas = new ArrayList<>();
        }
        this.enfermedadesCronicas.add(enfermedad);
    }
}