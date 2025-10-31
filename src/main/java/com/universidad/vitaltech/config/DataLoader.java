package com.universidad.vitaltech.config;

import com.universidad.vitaltech.model.*;
import com.universidad.vitaltech.model.embedded.*;
import com.universidad.vitaltech.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;

/**
 * Carga datos de prueba en la base de datos al iniciar la aplicación
 */
@Component
public class DataLoader implements CommandLineRunner {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private CitaRepository citaRepository;
    
    @Autowired
    private DiagnosticoRepository diagnosticoRepository;
    
    @Autowired
    private HorarioDisponibleRepository horarioDisponibleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // Limpiar datos existentes (solo para desarrollo)
        if (usuarioRepository.count() == 0) {
            System.out.println("=== Cargando datos de prueba en VitalTech ===");
            
            cargarUsuarios();
            cargarHorariosDisponibles();
            cargarCitas();
            cargarDiagnosticos();
            
            System.out.println("=== Datos de prueba cargados exitosamente ===");
        }
    }
    
    private void cargarUsuarios() {
        // ===== ADMINISTRADOR =====
        Usuario admin = new Usuario();
        admin.setNumeroDocumento("1098765432");
        admin.setNombre("Carlos");
        admin.setApellido("Rodríguez");
        admin.setEmail("carlos.rodriguez@vitaltech.com");
        admin.setTelefono("3201234567");
        admin.setFechaNacimiento(LocalDate.of(1985, 3, 15));
        admin.setGenero("Masculino");
        admin.setUsername("crodriguez");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRol(Rol.ADMIN);
        admin.setActivo(true);
        
        Direccion direccionAdmin = new Direccion();
        direccionAdmin.setCalle("Carrera 10");
        direccionAdmin.setNumero("45-67");
        direccionAdmin.setCiudad("Santa Rosa");
        direccionAdmin.setDepartamento("Bolívar");
        direccionAdmin.setCodigoPostal("130001");
        admin.setDireccion(direccionAdmin);
        
        usuarioRepository.save(admin);
        System.out.println("✓ Administrador creado: " + admin.getNombreCompleto());
        
        // ===== DOCTORES =====
        // Doctora - Cardiología
        Usuario doctora1 = new Usuario();
        doctora1.setNumeroDocumento("1087654321");
        doctora1.setNombre("María");
        doctora1.setApellido("González");
        doctora1.setEmail("maria.gonzalez@vitaltech.com");
        doctora1.setTelefono("3109876543");
        doctora1.setFechaNacimiento(LocalDate.of(1980, 7, 22));
        doctora1.setGenero("Femenino");
        doctora1.setUsername("mgonzalez");
        doctora1.setPassword(passwordEncoder.encode("doctor123"));
        doctora1.setRol(Rol.DOCTOR);
        doctora1.setEspecialidad("Cardiología");
        doctora1.setLicenciaMedica("MP-12345");
        doctora1.setActivo(true);
        
        Direccion direccionDoc1 = new Direccion();
        direccionDoc1.setCalle("Avenida 5");
        direccionDoc1.setNumero("23-45");
        direccionDoc1.setCiudad("Santa Rosa");
        direccionDoc1.setDepartamento("Bolívar");
        direccionDoc1.setCodigoPostal("130001");
        doctora1.setDireccion(direccionDoc1);
        
        usuarioRepository.save(doctora1);
        System.out.println("✓ Doctor creado: Dra. " + doctora1.getNombreCompleto() + " - " + doctora1.getEspecialidad());
        
        // Doctor - Medicina General
        Usuario doctor2 = new Usuario();
        doctor2.setNumeroDocumento("1076543210");
        doctor2.setNombre("Juan");
        doctor2.setApellido("Pérez");
        doctor2.setEmail("juan.perez@vitaltech.com");
        doctor2.setTelefono("3158765432");
        doctor2.setFechaNacimiento(LocalDate.of(1978, 11, 10));
        doctor2.setGenero("Masculino");
        doctor2.setUsername("jperez");
        doctor2.setPassword(passwordEncoder.encode("doctor123"));
        doctor2.setRol(Rol.DOCTOR);
        doctor2.setEspecialidad("Medicina General");
        doctor2.setLicenciaMedica("MP-67890");
        doctor2.setActivo(true);
        
        Direccion direccionDoc2 = new Direccion();
        direccionDoc2.setCalle("Calle 8");
        direccionDoc2.setNumero("12-34");
        direccionDoc2.setCiudad("Santa Rosa");
        direccionDoc2.setDepartamento("Bolívar");
        direccionDoc2.setCodigoPostal("130001");
        doctor2.setDireccion(direccionDoc2);
        
        usuarioRepository.save(doctor2);
        System.out.println("✓ Doctor creado: Dr. " + doctor2.getNombreCompleto() + " - " + doctor2.getEspecialidad());
        
        // Doctora - Pediatría
        Usuario doctora3 = new Usuario();
        doctora3.setNumeroDocumento("1065432109");
        doctora3.setNombre("Carmen");
        doctora3.setApellido("Morales");
        doctora3.setEmail("carmen.morales@vitaltech.com");
        doctora3.setTelefono("3187654321");
        doctora3.setFechaNacimiento(LocalDate.of(1983, 5, 18));
        doctora3.setGenero("Femenino");
        doctora3.setUsername("cmorales");
        doctora3.setPassword(passwordEncoder.encode("doctor123"));
        doctora3.setRol(Rol.DOCTOR);
        doctora3.setEspecialidad("Pediatría");
        doctora3.setLicenciaMedica("MP-54321");
        doctora3.setActivo(true);
        
        Direccion direccionDoc3 = new Direccion();
        direccionDoc3.setCalle("Carrera 15");
        direccionDoc3.setNumero("30-12");
        direccionDoc3.setCiudad("Santa Rosa");
        direccionDoc3.setDepartamento("Bolívar");
        direccionDoc3.setCodigoPostal("130001");
        doctora3.setDireccion(direccionDoc3);
        
        usuarioRepository.save(doctora3);
        System.out.println("✓ Doctor creado: Dra. " + doctora3.getNombreCompleto() + " - " + doctora3.getEspecialidad());
        
        // ===== RECEPCIONISTA =====
        Usuario recepcionista = new Usuario();
        recepcionista.setNumeroDocumento("1054321098");
        recepcionista.setNombre("Laura");
        recepcionista.setApellido("Martínez");
        recepcionista.setEmail("laura.martinez@vitaltech.com");
        recepcionista.setTelefono("3176543210");
        recepcionista.setFechaNacimiento(LocalDate.of(1995, 9, 5));
        recepcionista.setGenero("Femenino");
        recepcionista.setUsername("lmartinez");
        recepcionista.setPassword(passwordEncoder.encode("recep123"));
        recepcionista.setRol(Rol.RECEPCIONISTA);
        recepcionista.setActivo(true);
        
        Direccion direccionRecep = new Direccion();
        direccionRecep.setCalle("Calle 20");
        direccionRecep.setNumero("15-30");
        direccionRecep.setCiudad("Santa Rosa");
        direccionRecep.setDepartamento("Bolívar");
        direccionRecep.setCodigoPostal("130001");
        recepcionista.setDireccion(direccionRecep);
        
        usuarioRepository.save(recepcionista);
        System.out.println("✓ Recepcionista creado: " + recepcionista.getNombreCompleto());
        
        // ===== PACIENTES =====
        // Paciente 1
        Usuario paciente1 = new Usuario();
        paciente1.setNumeroDocumento("1043210987");
        paciente1.setNombre("Ana");
        paciente1.setApellido("Silva");
        paciente1.setEmail("ana.silva@email.com");
        paciente1.setTelefono("3165432109");
        paciente1.setFechaNacimiento(LocalDate.of(1990, 4, 12));
        paciente1.setGenero("Femenino");
        paciente1.setUsername("asilva");
        paciente1.setPassword(passwordEncoder.encode("paciente123"));
        paciente1.setRol(Rol.PACIENTE);
        paciente1.setActivo(true);
        
        Direccion direccionPac1 = new Direccion();
        direccionPac1.setCalle("Carrera 7");
        direccionPac1.setNumero("25-40");
        direccionPac1.setCiudad("Santa Rosa");
        direccionPac1.setDepartamento("Bolívar");
        direccionPac1.setCodigoPostal("130001");
        paciente1.setDireccion(direccionPac1);
        
        InformacionMedica infoMed1 = new InformacionMedica();
        infoMed1.setTipoSangre("O+");
        infoMed1.setAlergias(Arrays.asList("Penicilina"));
        infoMed1.setContactoEmergencia("Roberto Silva");
        infoMed1.setTelefonoEmergencia("3154321098");
        paciente1.setInformacionMedica(infoMed1);
        
        usuarioRepository.save(paciente1);
        System.out.println("✓ Paciente creado: " + paciente1.getNombreCompleto());
        
        // Paciente 2
        Usuario paciente2 = new Usuario();
        paciente2.setNumeroDocumento("1032109876");
        paciente2.setNombre("Pedro");
        paciente2.setApellido("Ramírez");
        paciente2.setEmail("pedro.ramirez@email.com");
        paciente2.setTelefono("3143210987");
        paciente2.setFechaNacimiento(LocalDate.of(1988, 8, 25));
        paciente2.setGenero("Masculino");
        paciente2.setUsername("pramirez");
        paciente2.setPassword(passwordEncoder.encode("paciente123"));
        paciente2.setRol(Rol.PACIENTE);
        paciente2.setActivo(true);
        
        Direccion direccionPac2 = new Direccion();
        direccionPac2.setCalle("Calle 12");
        direccionPac2.setNumero("8-15");
        direccionPac2.setCiudad("Santa Rosa");
        direccionPac2.setDepartamento("Bolívar");
        direccionPac2.setCodigoPostal("130001");
        paciente2.setDireccion(direccionPac2);
        
        InformacionMedica infoMed2 = new InformacionMedica();
        infoMed2.setTipoSangre("A+");
        infoMed2.setEnfermedadesCronicas(Arrays.asList("Diabetes tipo 2"));
        infoMed2.setContactoEmergencia("María Ramírez");
        infoMed2.setTelefonoEmergencia("3132109876");
        paciente2.setInformacionMedica(infoMed2);
        
        usuarioRepository.save(paciente2);
        System.out.println("✓ Paciente creado: " + paciente2.getNombreCompleto());
        
        // Paciente 3
        Usuario paciente3 = new Usuario();
        paciente3.setNumeroDocumento("1021098765");
        paciente3.setNombre("Sofía");
        paciente3.setApellido("Torres");
        paciente3.setEmail("sofia.torres@email.com");
        paciente3.setTelefono("3121098765");
        paciente3.setFechaNacimiento(LocalDate.of(1992, 12, 8));
        paciente3.setGenero("Femenino");
        paciente3.setUsername("storres");
        paciente3.setPassword(passwordEncoder.encode("paciente123"));
        paciente3.setRol(Rol.PACIENTE);
        paciente3.setActivo(true);
        
        Direccion direccionPac3 = new Direccion();
        direccionPac3.setCalle("Avenida 3");
        direccionPac3.setNumero("18-22");
        direccionPac3.setCiudad("Santa Rosa");
        direccionPac3.setDepartamento("Bolívar");
        direccionPac3.setCodigoPostal("130001");
        paciente3.setDireccion(direccionPac3);
        
        InformacionMedica infoMed3 = new InformacionMedica();
        infoMed3.setTipoSangre("AB+");
        infoMed3.setAlergias(Arrays.asList("Polen", "Ácaros"));
        infoMed3.setContactoEmergencia("Luis Torres");
        infoMed3.setTelefonoEmergencia("3101098765");
        paciente3.setInformacionMedica(infoMed3);
        
        usuarioRepository.save(paciente3);
        System.out.println("✓ Paciente creado: " + paciente3.getNombreCompleto());
        
        // Paciente 4
        Usuario paciente4 = new Usuario();
        paciente4.setNumeroDocumento("1010987654");
        paciente4.setNombre("Miguel");
        paciente4.setApellido("Castro");
        paciente4.setEmail("miguel.castro@email.com");
        paciente4.setTelefono("3190987654");
        paciente4.setFechaNacimiento(LocalDate.of(1985, 6, 30));
        paciente4.setGenero("Masculino");
        paciente4.setUsername("mcastro");
        paciente4.setPassword(passwordEncoder.encode("paciente123"));
        paciente4.setRol(Rol.PACIENTE);
        paciente4.setActivo(true);
        
        Direccion direccionPac4 = new Direccion();
        direccionPac4.setCalle("Carrera 12");
        direccionPac4.setNumero("30-50");
        direccionPac4.setCiudad("Santa Rosa");
        direccionPac4.setDepartamento("Bolívar");
        direccionPac4.setCodigoPostal("130001");
        paciente4.setDireccion(direccionPac4);
        
        InformacionMedica infoMed4 = new InformacionMedica();
        infoMed4.setTipoSangre("B+");
        infoMed4.setEnfermedadesCronicas(Arrays.asList("Hipertensión"));
        infoMed4.setMedicamentosActuales(Arrays.asList("Losartán 50mg"));
        infoMed4.setContactoEmergencia("Elena Castro");
        infoMed4.setTelefonoEmergencia("3180987654");
        paciente4.setInformacionMedica(infoMed4);
        
        usuarioRepository.save(paciente4);
        System.out.println("✓ Paciente creado: " + paciente4.getNombreCompleto());
    }
    
    private void cargarHorariosDisponibles() {
        // Obtener doctores
        Usuario doctora1 = usuarioRepository.findByUsername("mgonzalez").orElse(null);
        Usuario doctor2 = usuarioRepository.findByUsername("jperez").orElse(null);
        Usuario doctora3 = usuarioRepository.findByUsername("cmorales").orElse(null);
        
        if (doctora1 != null) {
            // Horarios Dra. González - Lunes a Viernes
            for (DayOfWeek dia : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, 
                                                DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, 
                                                DayOfWeek.FRIDAY)) {
                HorarioDisponible horario = new HorarioDisponible();
                horario.setDoctorId(doctora1.getId());
                horario.setDiaSemana(dia);
                horario.setHoraInicio(LocalTime.of(8, 0));
                horario.setHoraFin(LocalTime.of(12, 0));
                horario.setDuracionCita(30);
                horario.setActivo(true);
                horarioDisponibleRepository.save(horario);
            }
            System.out.println("✓ Horarios creados para Dra. González");
        }
        
        if (doctor2 != null) {
            // Horarios Dr. Pérez - Lunes a Viernes
            for (DayOfWeek dia : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, 
                                                DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, 
                                                DayOfWeek.FRIDAY)) {
                HorarioDisponible horario = new HorarioDisponible();
                horario.setDoctorId(doctor2.getId());
                horario.setDiaSemana(dia);
                horario.setHoraInicio(LocalTime.of(14, 0));
                horario.setHoraFin(LocalTime.of(18, 0));
                horario.setDuracionCita(30);
                horario.setActivo(true);
                horarioDisponibleRepository.save(horario);
            }
            System.out.println("✓ Horarios creados para Dr. Pérez");
        }
        
        if (doctora3 != null) {
            // Horarios Dra. Morales - Lunes, Miércoles, Viernes
            for (DayOfWeek dia : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, 
                                                DayOfWeek.FRIDAY)) {
                HorarioDisponible horario = new HorarioDisponible();
                horario.setDoctorId(doctora3.getId());
                horario.setDiaSemana(dia);
                horario.setHoraInicio(LocalTime.of(9, 0));
                horario.setHoraFin(LocalTime.of(13, 0));
                horario.setDuracionCita(30);
                horario.setActivo(true);
                horarioDisponibleRepository.save(horario);
            }
            System.out.println("✓ Horarios creados para Dra. Morales");
        }
    }
    
    private void cargarCitas() {
        // Obtener usuarios
        Usuario doctora1 = usuarioRepository.findByUsername("mgonzalez").orElse(null);
        Usuario doctor2 = usuarioRepository.findByUsername("jperez").orElse(null);
        Usuario paciente1 = usuarioRepository.findByUsername("asilva").orElse(null);
        Usuario paciente2 = usuarioRepository.findByUsername("pramirez").orElse(null);
        Usuario paciente3 = usuarioRepository.findByUsername("storres").orElse(null);
        
        if (doctora1 != null && paciente1 != null) {
            // Cita completada con la Dra. González
            Cita cita1 = new Cita();
            cita1.setPacienteId(paciente1.getId());
            cita1.setDoctorId(doctora1.getId());
            
            Horario horario1 = new Horario();
            horario1.setFecha(LocalDate.now().minusDays(7));
            horario1.setHoraInicio(LocalTime.of(9, 0));
            horario1.setHoraFin(LocalTime.of(9, 30));
            cita1.setHorario(horario1);
            
            cita1.setEstado(EstadoCita.COMPLETADA);
            cita1.setMotivoConsulta("Control cardiológico anual");
            cita1.setFechaCreacion(LocalDateTime.now().minusDays(14));
            citaRepository.save(cita1);
            System.out.println("✓ Cita completada creada");
        }
        
        if (doctor2 != null && paciente2 != null) {
            // Cita programada con Dr. Pérez
            Cita cita2 = new Cita();
            cita2.setPacienteId(paciente2.getId());
            cita2.setDoctorId(doctor2.getId());
            
            Horario horario2 = new Horario();
            horario2.setFecha(LocalDate.now().plusDays(3));
            horario2.setHoraInicio(LocalTime.of(14, 30));
            horario2.setHoraFin(LocalTime.of(15, 0));
            cita2.setHorario(horario2);
            
            cita2.setEstado(EstadoCita.PROGRAMADA);
            cita2.setMotivoConsulta("Control de diabetes");
            citaRepository.save(cita2);
            System.out.println("✓ Cita programada creada");
        }
        
        if (doctora1 != null && paciente3 != null) {
            // Cita confirmada
            Cita cita3 = new Cita();
            cita3.setPacienteId(paciente3.getId());
            cita3.setDoctorId(doctora1.getId());
            
            Horario horario3 = new Horario();
            horario3.setFecha(LocalDate.now().plusDays(1));
            horario3.setHoraInicio(LocalTime.of(10, 0));
            horario3.setHoraFin(LocalTime.of(10, 30));
            cita3.setHorario(horario3);
            
            cita3.setEstado(EstadoCita.CONFIRMADA);
            cita3.setMotivoConsulta("Consulta por alergias");
            citaRepository.save(cita3);
            System.out.println("✓ Cita confirmada creada");
        }
    }
    
    private void cargarDiagnosticos() {
        // Obtener la cita completada
        Usuario paciente1 = usuarioRepository.findByUsername("asilva").orElse(null);
        Usuario doctora1 = usuarioRepository.findByUsername("mgonzalez").orElse(null);
        
        if (paciente1 != null && doctora1 != null) {
            // Buscar citas completadas
            java.util.List<Cita> citasCompletadas = citaRepository.findByPacienteIdAndEstado(
                paciente1.getId(), EstadoCita.COMPLETADA
            );
            
            if (!citasCompletadas.isEmpty()) {
                Cita cita = citasCompletadas.get(0);
                
                Diagnostico diagnostico = new Diagnostico();
                diagnostico.setCitaId(cita.getId());
                diagnostico.setPacienteId(paciente1.getId());
                diagnostico.setDoctorId(doctora1.getId());
                diagnostico.setDiagnostico("Ritmo cardíaco normal. Presión arterial controlada.");
                diagnostico.setSintomas("Control de rutina, paciente asintomático");
                diagnostico.setTratamiento("Continuar con hábitos saludables");
                diagnostico.setRecomendaciones("Ejercicio cardiovascular 30 min diarios, dieta baja en sodio");
                diagnostico.setPresionArterial("120/80");
                diagnostico.setFrecuenciaCardiaca(72);
                diagnostico.setPeso(65.5);
                diagnostico.setAltura(1.65);
                diagnostico.setFechaCreacion(LocalDateTime.now().minusDays(7));
                
                diagnosticoRepository.save(diagnostico);
                System.out.println("✓ Diagnóstico creado para Ana Silva");
            }
        }
    }
}