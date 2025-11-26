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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Carga datos de prueba masivos en la base de datos al iniciar la aplicación
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

    private Random random = new Random();

    @Override
    public void run(String... args) throws Exception {
        // Solo cargar si la BD está vacía o tiene pocos datos
        if (usuarioRepository.count() < 10) {
            System.out.println("========================================");
            System.out.println("🚀 CARGANDO DATOS DE PRUEBA MASIVOS");
            System.out.println("========================================");

            List<Usuario> admins = cargarAdministradores();
            List<Usuario> doctores = cargarDoctores();
            List<Usuario> pacientes = cargarPacientes();

            System.out.println("✅ Usuarios creados: " + (admins.size() + doctores.size() + pacientes.size()));

            List<HorarioDisponible> horarios = cargarHorariosDisponibles(doctores);
            System.out.println("✅ Horarios creados: " + horarios.size());

            List<Cita> citas = cargarCitas(doctores, pacientes, horarios);
            System.out.println("✅ Citas creadas: " + citas.size());

            List<Diagnostico> diagnosticos = cargarDiagnosticos(citas, doctores, pacientes);
            System.out.println("✅ Diagnósticos creados: " + diagnosticos.size());

            System.out.println("========================================");
            System.out.println("🎉 CARGA COMPLETADA EXITOSAMENTE");
            System.out.println("   Total registros: " +
                    (admins.size() + doctores.size() + pacientes.size() +
                            horarios.size() + citas.size() + diagnosticos.size()));
            System.out.println("========================================");
        } else {
            System.out.println("⚠️ La base de datos ya contiene datos. Saltando inicialización.");
        }
    }

    // ==================== ADMINISTRADORES ====================
    private List<Usuario> cargarAdministradores() {
        List<Usuario> admins = new ArrayList<>();

        // Admin principal
        Usuario admin1 = new Usuario();
        admin1.setNumeroDocumento("1000000001");
        admin1.setNombre("Carlos");
        admin1.setApellido("Administrador");
        admin1.setEmail("admin@vitaltech.com");
        admin1.setTelefono("3201234567");
        admin1.setFechaNacimiento(LocalDate.of(1985, 3, 15));
        admin1.setGenero("Masculino");
        admin1.setUsername("admin");
        admin1.setPassword(passwordEncoder.encode("admin123"));
        admin1.setRol(Rol.ADMIN);
        admin1.setActivo(true);

        Direccion direccionAdmin1 = new Direccion();
        direccionAdmin1.setCalle("Carrera 10");
        direccionAdmin1.setNumero("45-67");
        direccionAdmin1.setCiudad("Santa Rosa");
        direccionAdmin1.setDepartamento("Bolívar");
        direccionAdmin1.setCodigoPostal("130001");
        admin1.setDireccion(direccionAdmin1);

        admins.add(usuarioRepository.save(admin1));

        // Admin soporte
        Usuario admin2 = new Usuario();
        admin2.setNumeroDocumento("1000000002");
        admin2.setNombre("Laura");
        admin2.setApellido("Soporte");
        admin2.setEmail("soporte@vitaltech.com");
        admin2.setTelefono("3201234568");
        admin2.setFechaNacimiento(LocalDate.of(1988, 7, 20));
        admin2.setGenero("Femenino");
        admin2.setUsername("lsoporte");
        admin2.setPassword(passwordEncoder.encode("soporte123"));
        admin2.setRol(Rol.ADMIN);
        admin2.setActivo(true);

        Direccion direccionAdmin2 = new Direccion();
        direccionAdmin2.setCalle("Carrera 12");
        direccionAdmin2.setNumero("30-45");
        direccionAdmin2.setCiudad("Santa Rosa");
        direccionAdmin2.setDepartamento("Bolívar");
        direccionAdmin2.setCodigoPostal("130001");
        admin2.setDireccion(direccionAdmin2);

        admins.add(usuarioRepository.save(admin2));

        return admins;
    }

    // ==================== DOCTORES ====================
    private List<Usuario> cargarDoctores() {
        List<Usuario> doctores = new ArrayList<>();

        String[] nombres = { "Carlos", "Ana", "Luis", "María", "Jorge", "Patricia", "Ricardo", "Carmen",
                "Miguel", "Elena", "Fernando", "Sofía", "Andrés", "Isabel", "Diego",
                "Valentina", "Gabriel", "Camila", "Sebastián", "Paula" };

        String[] apellidos = { "Martínez", "López", "Rodríguez", "González", "Pérez", "García",
                "Fernández", "Díaz", "Torres", "Ramírez", "Flores", "Castro",
                "Morales", "Jiménez", "Ruiz", "Herrera", "Mendoza", "Silva",
                "Vargas", "Rojas" };

        String[] especialidades = { "Medicina General", "Cardiología", "Pediatría", "Dermatología",
                "Traumatología", "Ginecología", "Oftalmología", "Neurología",
                "Psiquiatría", "Endocrinología" };

        for (int i = 0; i < 20; i++) {
            Usuario doctor = new Usuario();
            doctor.setNumeroDocumento(String.format("100%07d", i + 1));
            doctor.setNombre(nombres[i]);
            doctor.setApellido(apellidos[i]);

            // Username con nombre real: cmartinez, alopez, etc.
            String username = nombres[i].substring(0, 1).toLowerCase() +
                    apellidos[i].toLowerCase().replace("á", "a").replace("é", "e")
                            .replace("í", "i").replace("ó", "o").replace("ú", "u");

            doctor.setEmail(username + "@vitaltech.com");
            doctor.setTelefono(String.format("320%07d", 1000000 + i));
            doctor.setFechaNacimiento(
                    LocalDate.of(1975 + random.nextInt(15), 1 + random.nextInt(12), 1 + random.nextInt(28)));
            doctor.setGenero(i % 2 == 0 ? "Masculino" : "Femenino");
            doctor.setUsername(username);
            doctor.setPassword(passwordEncoder.encode("doctor123"));
            doctor.setRol(Rol.DOCTOR);
            doctor.setEspecialidad(especialidades[i % especialidades.length]);
            doctor.setLicenciaMedica("MP-" + (10000 + i));
            doctor.setActivo(true);

            Direccion direccion = new Direccion();
            direccion.setCalle(
                    random.nextBoolean() ? "Carrera " + (5 + random.nextInt(20)) : "Calle " + (5 + random.nextInt(20)));
            direccion.setNumero((10 + random.nextInt(90)) + "-" + (10 + random.nextInt(90)));
            direccion.setCiudad("Santa Rosa");
            direccion.setDepartamento("Bolívar");
            direccion.setCodigoPostal("130001");
            doctor.setDireccion(direccion);

            doctores.add(usuarioRepository.save(doctor));
        }

        return doctores;
    }

    // ==================== PACIENTES ====================
    private List<Usuario> cargarPacientes() {
        List<Usuario> pacientes = new ArrayList<>();

        String[] nombresM = { "Juan", "Pedro", "Miguel", "Carlos", "Luis", "José", "Francisco",
                "Antonio", "Manuel", "David", "Daniel", "Javier", "Rafael", "Sergio",
                "Andrés", "Diego", "Gabriel", "Ricardo", "Roberto", "Eduardo" };

        String[] nombresF = { "María", "Ana", "Carmen", "Laura", "Isabel", "Patricia", "Sofía",
                "Valentina", "Gabriela", "Camila", "Paula", "Daniela", "Andrea", "Natalia",
                "Carolina", "Alejandra", "Mónica", "Adriana", "Lucía", "Fernanda" };

        String[] apellidos = { "Martínez", "López", "Rodríguez", "González", "Pérez", "García",
                "Fernández", "Díaz", "Torres", "Ramírez", "Flores", "Castro",
                "Morales", "Jiménez", "Ruiz", "Herrera", "Mendoza", "Silva",
                "Vargas", "Rojas", "Ortiz", "Delgado", "Cruz", "Reyes", "Gutiérrez",
                "Sánchez", "Rivera", "Álvarez", "Romero", "Navarro" };

        String[] tiposSangre = { "O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-" };
        String[] alergias = { "Penicilina", "Polen", "Ácaros", "Mariscos", "Nueces", "Látex" };
        String[] enfermedadesCronicas = { "Hipertensión", "Diabetes tipo 2", "Asma", "Artritis", "Hipotiroidismo" };

        for (int i = 0; i < 128; i++) {
            Usuario paciente = new Usuario();
            boolean esMasculino = random.nextBoolean();

            paciente.setNumeroDocumento(String.format("200%07d", i + 1));

            if (esMasculino) {
                paciente.setNombre(nombresM[random.nextInt(nombresM.length)]);
                paciente.setGenero("Masculino");
            } else {
                paciente.setNombre(nombresF[random.nextInt(nombresF.length)]);
                paciente.setGenero("Femenino");
            }

            paciente.setApellido(apellidos[random.nextInt(apellidos.length)] + " " +
                    apellidos[random.nextInt(apellidos.length)]);
            paciente.setEmail("paciente" + (i + 1) + "@email.com");
            paciente.setTelefono(String.format("310%07d", 1000000 + i));
            paciente.setFechaNacimiento(generarFechaNacimiento());
            paciente.setUsername("paciente" + (i + 1));
            paciente.setPassword(passwordEncoder.encode("paciente123"));
            paciente.setRol(Rol.PACIENTE);
            paciente.setActivo(true);

            Direccion direccion = new Direccion();
            direccion.setCalle(
                    random.nextBoolean() ? "Carrera " + (1 + random.nextInt(30)) : "Calle " + (1 + random.nextInt(30)));
            direccion.setNumero((1 + random.nextInt(99)) + "-" + (1 + random.nextInt(99)));
            direccion.setCiudad("Santa Rosa");
            direccion.setDepartamento("Bolívar");
            direccion.setCodigoPostal("130001");
            paciente.setDireccion(direccion);

            InformacionMedica infoMed = new InformacionMedica();
            infoMed.setTipoSangre(tiposSangre[random.nextInt(tiposSangre.length)]);

            if (random.nextDouble() < 0.3) { // 30% tiene alergias
                List<String> alergiasLista = new ArrayList<>();
                alergiasLista.add(alergias[random.nextInt(alergias.length)]);
                if (random.nextBoolean()) {
                    alergiasLista.add(alergias[random.nextInt(alergias.length)]);
                }
                infoMed.setAlergias(alergiasLista);
            }

            if (random.nextDouble() < 0.2) { // 20% tiene enfermedades crónicas
                List<String> enfermedadesLista = new ArrayList<>();
                enfermedadesLista.add(enfermedadesCronicas[random.nextInt(enfermedadesCronicas.length)]);
                infoMed.setEnfermedadesCronicas(enfermedadesLista);
            }

            infoMed.setContactoEmergencia(nombresM[random.nextInt(nombresM.length)] + " " +
                    apellidos[random.nextInt(apellidos.length)]);
            infoMed.setTelefonoEmergencia(String.format("315%07d", random.nextInt(10000000)));

            paciente.setInformacionMedica(infoMed);

            pacientes.add(usuarioRepository.save(paciente));
        }

        return pacientes;
    }

    // ==================== HORARIOS ====================
    private List<HorarioDisponible> cargarHorariosDisponibles(List<Usuario> doctores) {
        List<HorarioDisponible> horarios = new ArrayList<>();

        DayOfWeek[] diasSemana = { DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY };

        for (Usuario doctor : doctores) {
            int cantidadHorarios = 2 + random.nextInt(2); // 2-3 horarios por doctor

            for (int i = 0; i < cantidadHorarios; i++) {
                HorarioDisponible horario = new HorarioDisponible();
                horario.setDoctorId(doctor.getId());
                horario.setDiaSemana(diasSemana[random.nextInt(diasSemana.length)]);

                if (random.nextBoolean()) {
                    horario.setHoraInicio(LocalTime.of(8 + random.nextInt(3), 0));
                    horario.setHoraFin(horario.getHoraInicio().plusHours(4));
                } else {
                    horario.setHoraInicio(LocalTime.of(14 + random.nextInt(2), 0));
                    horario.setHoraFin(horario.getHoraInicio().plusHours(4));
                }

                horario.setDuracionCita(random.nextBoolean() ? 30 : 20);
                horario.setActivo(true);
                horarios.add(horarioDisponibleRepository.save(horario));
            }
        }

        return horarios;
    }

    // ==================== CITAS ====================
    private List<Cita> cargarCitas(List<Usuario> doctores, List<Usuario> pacientes,
            List<HorarioDisponible> horarios) {
        List<Cita> citas = new ArrayList<>();
        LocalDate hoy = LocalDate.now();

        String[] motivos = {
                "Consulta general", "Control médico", "Dolor persistente", "Chequeo rutinario",
                "Síntomas gripales", "Seguimiento tratamiento", "Renovación de fórmula médica",
                "Malestar general", "Dolor abdominal", "Control de presión arterial",
                "Exámenes de laboratorio", "Certificado médico", "Dolor de cabeza recurrente",
                "Problemas respiratorios", "Control de peso"
        };

        // CITAS COMPLETADAS (400)
        for (int i = 0; i < 400; i++) {
            HorarioDisponible horario = horarios.get(random.nextInt(horarios.size()));
            Usuario paciente = pacientes.get(random.nextInt(pacientes.size()));

            LocalDate fechaCita = hoy.minusDays(1 + random.nextInt(60));
            LocalTime horaCita = generarHoraCita(horario);

            Cita cita = new Cita();
            cita.setPacienteId(paciente.getId());
            cita.setDoctorId(horario.getDoctorId());

            Horario horarioCita = new Horario();
            horarioCita.setFecha(fechaCita);
            horarioCita.setHoraInicio(horaCita);
            horarioCita.setHoraFin(horaCita.plusMinutes(horario.getDuracionCita()));
            cita.setHorario(horarioCita);

            cita.setEstado(EstadoCita.COMPLETADA);
            cita.setMotivoConsulta(motivos[random.nextInt(motivos.length)]);
            cita.setFechaCreacion(LocalDateTime.now().minusDays(60 + random.nextInt(10)));

            citas.add(citaRepository.save(cita));
        }

        // CITAS PRÓXIMAS (200)
        for (int i = 0; i < 200; i++) {
            HorarioDisponible horario = horarios.get(random.nextInt(horarios.size()));
            Usuario paciente = pacientes.get(random.nextInt(pacientes.size()));

            LocalDate fechaCita = hoy.plusDays(1 + random.nextInt(30));
            LocalTime horaCita = generarHoraCita(horario);

            Cita cita = new Cita();
            cita.setPacienteId(paciente.getId());
            cita.setDoctorId(horario.getDoctorId());

            Horario horarioCita = new Horario();
            horarioCita.setFecha(fechaCita);
            horarioCita.setHoraInicio(horaCita);
            horarioCita.setHoraFin(horaCita.plusMinutes(horario.getDuracionCita()));
            cita.setHorario(horarioCita);

            cita.setEstado(random.nextBoolean() ? EstadoCita.PROGRAMADA : EstadoCita.CONFIRMADA);
            cita.setMotivoConsulta(motivos[random.nextInt(motivos.length)]);
            cita.setFechaCreacion(LocalDateTime.now().minusDays(random.nextInt(30)));

            citas.add(citaRepository.save(cita));
        }

        // CITAS CANCELADAS (100)
        for (int i = 0; i < 100; i++) {
            HorarioDisponible horario = horarios.get(random.nextInt(horarios.size()));
            Usuario paciente = pacientes.get(random.nextInt(pacientes.size()));

            LocalDate fechaCita = hoy.plusDays(random.nextInt(15));
            LocalTime horaCita = generarHoraCita(horario);

            Cita cita = new Cita();
            cita.setPacienteId(paciente.getId());
            cita.setDoctorId(horario.getDoctorId());

            Horario horarioCita = new Horario();
            horarioCita.setFecha(fechaCita);
            horarioCita.setHoraInicio(horaCita);
            horarioCita.setHoraFin(horaCita.plusMinutes(horario.getDuracionCita()));
            cita.setHorario(horarioCita);

            cita.setEstado(EstadoCita.CANCELADA);
            cita.setMotivoConsulta(motivos[random.nextInt(motivos.length)]);
            cita.setFechaCreacion(LocalDateTime.now().minusDays(random.nextInt(20)));

            citas.add(citaRepository.save(cita));
        }

        return citas;
    }

    // ==================== DIAGNÓSTICOS ====================
    private List<Diagnostico> cargarDiagnosticos(List<Cita> citas, List<Usuario> doctores,
            List<Usuario> pacientes) {
        List<Diagnostico> diagnosticos = new ArrayList<>();

        String[] diagnosticosPrincipales = {
                "Hipertensión arterial", "Diabetes tipo 2", "Gastritis aguda", "Faringitis viral",
                "Migraña común", "Lumbalgia mecánica", "Rinitis alérgica", "Dermatitis atópica",
                "Ansiedad generalizada", "Infección respiratoria", "Conjuntivitis alérgica",
                "Otitis media aguda", "Amigdalitis bacteriana", "Bronquitis aguda"
        };

        String[] sintomas = {
                "Dolor de cabeza, mareos", "Fatiga, sed excesiva", "Dolor abdominal, náuseas",
                "Dolor de garganta, fiebre", "Dolor de cabeza intenso", "Dolor lumbar constante",
                "Congestión nasal, estornudos", "Picazón, enrojecimiento", "Nerviosismo, insomnio",
                "Tos, dolor de pecho", "Ojos rojos, lagrimeo", "Dolor de oído, fiebre",
                "Dolor al tragar, malestar", "Tos persistente, flema"
        };

        String[] tratamientos = {
                "Enalapril 10mg cada 12 horas", "Metformina 850mg cada 12 horas",
                "Omeprazol 20mg en ayunas", "Acetaminofén 500mg cada 8 horas",
                "Ibuprofeno 400mg cada 8 horas", "Diclofenaco 50mg cada 12 horas",
                "Loratadina 10mg cada 24 horas", "Betametasona crema tópica",
                "Alprazolam 0.25mg según necesidad", "Amoxicilina 500mg cada 8 horas",
                "Tobramicina colirio", "Amoxicilina + Ácido clavulánico",
                "Azitromicina 500mg día 1, luego 250mg", "Salbutamol inhalador"
        };

        String[] recomendaciones = {
                "Dieta baja en sodio, ejercicio moderado", "Control de glucosa, dieta balanceada",
                "Evitar alimentos irritantes, comidas pequeñas", "Reposo, abundantes líquidos",
                "Evitar luz intensa, descanso", "Terapia física, ejercicios de estiramiento",
                "Evitar alérgenos, mantener ventilación", "Hidratación de piel, evitar rascado",
                "Técnicas de relajación, terapia", "Reposo, hidratación abundante",
                "Evitar frotarse ojos, lágrimas artificiales", "Compresas tibias, analgesia",
                "Gárgaras con agua tibia y sal", "Evitar irritantes, nebulizaciones"
        };

        for (Cita cita : citas) {
            if (cita.getEstado() == EstadoCita.COMPLETADA) {
                int idx = random.nextInt(diagnosticosPrincipales.length);

                Diagnostico diagnostico = new Diagnostico();
                diagnostico.setCitaId(cita.getId());
                diagnostico.setPacienteId(cita.getPacienteId());
                diagnostico.setDoctorId(cita.getDoctorId());
                diagnostico.setDiagnostico(diagnosticosPrincipales[idx]);
                diagnostico.setSintomas(sintomas[idx]);
                diagnostico.setTratamiento(tratamientos[idx]);
                diagnostico.setRecomendaciones(recomendaciones[idx]);
                diagnostico.setPresionArterial(generarPresion());
                diagnostico.setTemperatura(36.0 + random.nextDouble() * 2);
                diagnostico.setFrecuenciaCardiaca(60 + random.nextInt(40));
                diagnostico.setPeso(50.0 + random.nextDouble() * 50);
                diagnostico.setAltura(1.50 + random.nextDouble() * 0.40);
                diagnostico.setFechaCreacion(LocalDateTime.now().minusDays(random.nextInt(60)));

                diagnosticos.add(diagnosticoRepository.save(diagnostico));
            }
        }

        return diagnosticos;
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private LocalDate generarFechaNacimiento() {
        int edad = 18 + random.nextInt(65);
        return LocalDate.now().minusYears(edad).minusDays(random.nextInt(365));
    }

    private LocalTime generarHoraCita(HorarioDisponible horario) {
        List<LocalTime> horasDisponibles = horario.generarHorariosDisponibles();
        if (horasDisponibles.isEmpty()) {
            return horario.getHoraInicio();
        }
        return horasDisponibles.get(random.nextInt(horasDisponibles.size()));
    }

    private String generarPresion() {
        int sistolica = 100 + random.nextInt(40);
        int diastolica = 60 + random.nextInt(30);
        return sistolica + "/" + diastolica;
    }
}