package com.universidad.vitaltech.model.embedded;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Documento embebido para almacenar direcciones
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Direccion {
    private String calle;
    private String numero;
    private String ciudad;
    private String departamento;
    private String codigoPostal;
    
    @Override
    public String toString() {
        return String.format("%s %s, %s, %s", calle, numero, ciudad, departamento);
    }
}