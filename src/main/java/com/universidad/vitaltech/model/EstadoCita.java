package com.universidad.vitaltech.model;

/**
 * Enumeración de estados de una cita médica
 */
public enum EstadoCita {
    PROGRAMADA("Programada"),
    CONFIRMADA("Confirmada"),
    EN_CURSO("En Curso"),
    COMPLETADA("Completada"),
    CANCELADA("Cancelada"),
    NO_ASISTIO("No Asistió");
    
    private final String displayName;
    
    EstadoCita(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}