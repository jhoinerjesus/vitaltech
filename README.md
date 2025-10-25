# 🏥 VitalTech - Sistema de Gestión de Citas Médicas

Sistema web desarrollado con Spring Boot para la gestión integral de citas médicas en clínicas y hospitales.

## 📋 Descripción

VitalTech es un sistema completo de gestión de citas médicas que permite administrar pacientes, doctores, citas y diagnósticos de manera eficiente y segura.

## ✨ Características Principales

- ✅ Sistema de autenticación y autorización con 4 roles
- ✅ Gestión de citas médicas con duración de 30 minutos
- ✅ Registro y administración de pacientes
- ✅ Gestión de horarios disponibles para médicos
- ✅ Sistema de diagnósticos médicos
- ✅ Panel de control diferenciado por rol
- ✅ Base de datos MongoDB con documentos embebidos

## 👥 Roles del Sistema

### 1. **Administrador**
- Registrar, editar y eliminar usuarios
- Gestión completa del sistema
- Asignación de roles

### 2. **Doctor**
- Ver citas disponibles por día
- Establecer diagnósticos
- Marcar citas como completadas
- Gestionar horarios disponibles

### 3. **Recepcionista**
- Registrar pacientes
- Buscar y crear citas
- Cancelar citas
- Validar usuarios

### 4. **Paciente**
- Auto-registro en el sistema
- Agendar citas médicas
- Ver diagnósticos anteriores
- Gestionar sus propias citas

### Usuario No Registrado
- Ver último diagnóstico (solo consulta)

## 🛠️ Tecnologías Utilizadas

- **Backend:** Spring Boot 3.2.0
- **Base de Datos:** MongoDB
- **Frontend:** Thymeleaf + Bootstrap 5
- **Seguridad:** Spring Security
- **Build Tool:** Maven
- **Java:** 21

## 📦 Dependencias Principales

- Spring Boot Starter Web
- Spring Boot Starter Data MongoDB
- Spring Boot Starter Security
- Spring Boot Starter Thymeleaf
- Spring Boot Starter Validation
- Lombok
- Bootstrap 5.3.2
- jQuery 3.7.1

## 🚀 Instalación y Configuración

### Prerrequisitos

- Java JDK 21 o superior
- Maven 3.6+
- MongoDB 4.4+ (local o MongoDB Atlas)
- IDE (VS Code, IntelliJ IDEA, Eclipse)

### Pasos de Instalación

1. **Clonar el repositorio**
   ```bash
   git clone https://github.com/TU_USUARIO/vitaltech.git
   cd vitaltech
   ```

2. **Configurar MongoDB**
   
   Edita `src/main/resources/application.properties`:
   ```properties
   spring.data.mongodb.uri=mongodb://localhost:27017/vitaltech
   spring.data.mongodb.database=vitaltech
   ```

3. **Instalar dependencias**
   ```bash
   mvn clean install
   ```

4. **Ejecutar la aplicación**
   ```bash
   mvn spring-boot:run
   ```

5. **Acceder a la aplicación**
   
   Abre tu navegador en: `http://localhost:8080`

## 📁 Estructura del Proyecto

```
vitaltech/
├── src/
│   ├── main/
│   │   ├── java/com/universidad/vitaltech/
│   │   │   ├── config/          # Configuraciones
│   │   │   ├── controller/      # Controladores MVC
│   │   │   ├── model/           # Entidades MongoDB
│   │   │   ├── repository/      # Repositorios
│   │   │   ├── service/         # Lógica de negocio
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   └── util/            # Utilidades
│   │   └── resources/
│   │       ├── templates/       # Vistas Thymeleaf
│   │       ├── static/          # CSS, JS, imágenes
│   │       └── application.properties
│   └── test/                    # Tests unitarios
├── pom.xml
└── README.md
```

## 🔐 Seguridad

El sistema implementa Spring Security con:
- Autenticación basada en sesiones
- Encriptación de contraseñas con BCrypt
- Autorización basada en roles
- Protección CSRF
- Validación de datos de entrada

## 🗓️ Reglas de Negocio

- Duración de citas: **30 minutos máximo**
- Solo el administrador puede registrar usuarios con roles específicos
- Los pacientes pueden auto-registrarse
- Los médicos definen sus propios horarios disponibles
- Usuarios no registrados solo pueden ver su último diagnóstico

## 👨‍💻 Autor

Proyecto desarrollado para la Universidad

## 📝 Licencia

Este proyecto es de uso académico.

## 📧 Contacto

Para consultas o sugerencias sobre el proyecto, contactar a través del repositorio.

---

**Estado del Proyecto:** 🚧 En Desarrollo

**Última actualización:** Octubre 2025