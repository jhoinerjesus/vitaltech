package com.universidad.vitaltech.model;

/**
 * Enumeración de los roles del sistema VitalTech
 */
public enum Rol {
    ADMIN("Administrador"),
    DOCTOR("Doctor"),
    PACIENTE("Paciente"),
    RECEPCIONISTA("Recepcionista");
    
    private final String displayName;
    
    Rol(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}