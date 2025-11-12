package com.universidad.vitaltech.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.universidad.vitaltech.model.embedded.Direccion;
import com.universidad.vitaltech.model.embedded.InformacionMedica;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Document(collection = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    private String id;

    @NotBlank(message = "El número de documento es obligatorio")
    @Indexed(unique = true) // unico inidce
    private String numeroDocumento;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email inválido")
    @Indexed(unique = true) 
    private String email;

    @NotBlank(message = "El teléfono es obligatorio")
    private String telefono;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    private LocalDate fechaNacimiento;

    private String genero;

    // Documento embebido
    private Direccion direccion;

    // Credenciales
    @NotBlank(message = "El usuario es obligatorio")
    @Indexed(unique = true) 
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    @NotNull(message = "El rol es obligatorio")
    @Indexed // indices no unicos para busquedas por rol
    private Rol rol;

    private boolean activo = true;

    // Campos  para DOCTOR
    private String especialidad;
    private String licenciaMedica;

    // Campos para PACIENTE 
    private InformacionMedica informacionMedica;

    // Auditoria
    private LocalDateTime fechaRegistro = LocalDateTime.now();
    private LocalDateTime ultimaActualizacion;
    private String registradoPor; // ID del usuario que lo registró

    // Métodos de utilidad
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    public boolean esDoctor() {
        return this.rol == Rol.DOCTOR;
    }

    public boolean esPaciente() {
        return this.rol == Rol.PACIENTE;
    }

    public boolean esAdmin() {
        return this.rol == Rol.ADMIN;
    }

    public boolean esRecepcionista() {
        return this.rol == Rol.RECEPCIONISTA;
    }

    public int getEdad() {
        if (fechaNacimiento == null) {
            return 0;
        }
        return LocalDate.now().getYear() - fechaNacimiento.getYear();
    }
}
