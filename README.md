# Hospital – Sistema de Gestión de Citas Hospitalarias

Proyecto final universitario desarrollado con **Java Spring Boot**, **Spring Data JPA**, **H2** (base de datos en memoria) y **Thymeleaf** para la capa de vistas. Implementa autenticación y autorización con **Spring Security**, con dos roles de usuario: `ADMIN` y `USUARIO`. Los médicos son gestionados como registros del dominio, **no** como usuarios del sistema.

## Estructura de carpetas principales

```
Hospital/
├── pom.xml                # Definición del proyecto Maven y dependencias
├── mvnw / mvnw.cmd        # Wrapper de Maven (build sin instalación local)
└── src/
    ├── main/
    │   ├── java/com/Proyecto/Hospital/
    │   │   ├── Config/         # Configuración de seguridad y carga inicial de datos
    │   │   ├── controller/     # Controladores web (rutas y navegación)
    │   │   ├── Model/          # Entidades JPA (Medico, Paciente, Usuario)
    │   │   ├── Repository/     # Interfaces JpaRepository de acceso a datos
    │   │   ├── Security/       # Lógica de autenticación (UserDetailsService, handlers)
    │   │   ├── Service/        # Lógica de negocio entre controladores y repositorios
    │   │   └── HospitalApplication.java  # Clase principal (arranque de Spring Boot)
    │   └── resources/
    │       ├── application.properties    # Configuración de la app y datasource H2
    │       ├── static/css/               # Hojas de estilo
    │       └── templates/                # Vistas Thymeleaf (HTML)
    └── test/
        └── java/com/Proyecto/Hospital/   # Pruebas automatizadas
```

## Detalle de las carpetas principales

### `Config/`
Configuración transversal de la aplicación:
- **`SecurityConfig.java`**: define las reglas de autorización por ruta y rol, el formulario de login (`/login`), el manejo de logout y accesos denegados.
- **`DataInitializer.java`**: crea, al arrancar la app, dos usuarios de prueba (un `ADMIN` y un `USUARIO`) si no existen aún en la base de datos.

### `controller/`
Controladores Spring MVC que exponen las rutas web:
- `LoginController` – pantalla de inicio de sesión.
- `InicioController` – página de inicio tras autenticarse.
- `MedicoController` – CRUD completo de médicos (listar, crear, editar, eliminar) + endpoint JSON (`/api/medicos`).
- `PacienteController` – CRUD completo de pacientes.
- `CitaController` – vista de citas (módulo en desarrollo).
- `PerfilController` – visualización y actualización del perfil del usuario autenticado.
- `AccesoDenegadoController` – página mostrada cuando un usuario intenta acceder a una ruta sin permisos.

### `Model/`
Entidades JPA mapeadas a tablas de la base de datos:
- `Medico` – datos del médico (nombre, cédula, especialidad, disponibilidad horaria, etc.). **No implementa autenticación.**
- `Paciente` – datos personales del paciente.
- `Usuario` – cuentas del sistema, con rol (`ADMIN` o `USUARIO`) y contraseña cifrada.

### `Repository/`
Interfaces `JpaRepository` que dan acceso a datos sin necesidad de implementación manual (CRUD automático vía Spring Data JPA):
- `MedicoRepository`, `PacienteRepository`, `UsuarioRepository` (esta última incluye `findByEmail`, usado para autenticación).

### `Security/`
- **`UsuarioDetailService.java`**: implementa `UserDetailsService`, cargando el usuario por email y asignándole su rol (`ROLE_ADMIN` / `ROLE_USUARIO`).
- **`LoginSecurityHeadler.java`**: maneja la redirección tras un login exitoso.

### `Service/`
Capa intermedia con la lógica de negocio, entre controladores y repositorios:
- `MedicoService` – listar, obtener, guardar y eliminar médicos.
- `PacienteService` – listar, obtener, guardar y eliminar pacientes.

### `resources/templates/`
Vistas Thymeleaf que conforman la interfaz de usuario:
- `login.html`, `inicio.html`, `perfil.html`, `acceso-denegado.html`
- `listaMedicos.html`, `formularioMedico.html`
- `ListaPacientes.html`, `FormularioPaciente.html`
- `citas.html`

### `resources/application.properties`
Configura el nombre de la aplicación y la conexión a la base de datos **H2 en memoria** (`jdbc:h2:mem:hospitaldb`).

## Roles y accesos (resumen de seguridad)

| Ruta | Rol requerido |
|---|---|
| `/login`, `/acceso-denegado`, `/css/**` | Público |
| `/inicio` | `ADMIN` o `USUARIO` |
| `/listaPacientes`, `/pacienteNuevo`, `/medicos` (gestión de médicos y pacientes) | `ADMIN` |
| `/citas`, `/perfil`, `/actualizarPerfil` | `USUARIO` |

## Credenciales de acceso (usuarios semilla)

Creadas automáticamente por `DataInitializer.java` al arrancar la aplicación:

| Rol | Email | Contraseña |
|---|---|---|
| `ADMIN` | Juan@hospital.com | 123456 |
| `USUARIO` | Pedro@hospital.com | 123456 |

## Cómo ejecutar el proyecto

```bash
./mvnw spring-boot:run
```

La aplicación quedará disponible en `http://localhost:8080`, con la base de datos H2 en memoria (los datos se reinician en cada arranque, excepto los usuarios semilla creados por `DataInitializer`).
