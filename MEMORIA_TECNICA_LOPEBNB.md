# MEMORIA TÉCNICA DEL PROYECTO

## LopeBnB - API REST de Gestión de Alojamientos Rurales

**Alumno:** Álvaro López Pérez  
**Asignatura:** Desarrollo de Interfaces / Programación de Servicios y Procesos (según plan docente)  
**Centro:** IES Alixar  
**Fecha:** 27/02/2026  
**Versión:** 1.0

---

## Índice

1. Introducción
2. Objetivos del proyecto
3. Alcance funcional
4. Stack tecnológico y dependencias
5. Arquitectura y organización del código
6. Modelo de datos
7. Seguridad y control de acceso
8. API REST: recursos y operaciones
9. Reglas de negocio implementadas
10. Gestión de archivos e imágenes
11. Configuración, despliegue y ejecución
12. Calidad, pruebas y validación
13. Incidencias técnicas observadas
14. Conclusiones y propuesta de mejora
15. Anexos

---

## 1. Introducción

LopeBnB es una API REST desarrollada para gestionar el ciclo completo de un sistema de alojamientos rurales: catálogo de casas, propietarios, huéspedes, reservas y opiniones. El proyecto sigue una arquitectura en capas y emplea autenticación basada en JWT con autorización por roles.

La finalidad de esta memoria es describir la solución técnica implementada, justificar las decisiones principales y documentar el estado real del proyecto para su evaluación académica.

---

## 2. Objetivos del proyecto

Los objetivos técnicos definidos y alcanzados son:

- Implementar una API REST con operaciones CRUD sobre entidades de negocio.
- Aplicar paginación, filtrado y ordenación en listados.
- Incorporar autenticación JWT y autorización por roles.
- Proteger datos mediante DTOs, evitando exposición de información sensible.
- Gestionar subida y publicación de imágenes en recursos de casas rurales.
- Documentar la API con OpenAPI/Swagger.

---

## 3. Alcance funcional

El proyecto cubre los siguientes dominios:

- **Autenticación de usuarios** (`/api/v1/authenticate`).
- **Gestión de casas rurales** (`/api/casas`).
- **Gestión de propietarios** (`/api/propietarios`).
- **Gestión de huéspedes** (`/api/huespedes`).
- **Gestión de reservas** (`/api/reservas`).
- **Gestión de opiniones** (`/api/opiniones`).
- **Recursos de imagen** (`/uploads/**`).
- **Consulta de perfil autenticado** (`/api/user`).

---

## 4. Stack tecnológico y dependencias

### 4.1 Plataforma

- Java 21
- Spring Boot 3.5.6
- Maven Wrapper (`mvnw`, `mvnw.cmd`)

### 4.2 Frameworks y librerías principales

- `spring-boot-starter-web`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-security`
- `spring-boot-starter-validation`
- `mariadb-java-client`
- `jjwt-api`, `jjwt-impl`, `jjwt-jackson`
- `springdoc-openapi-starter-webmvc-ui`
- `dotenv-java`
- `lombok`

### 4.3 Persistencia

- Motor: MariaDB
- ORM: Hibernate/JPA
- Inicialización SQL: `schema.sql` y `data.sql`

---

## 5. Arquitectura y organización del código

El proyecto sigue una arquitectura por capas:

1. **Controller**: expone endpoints HTTP y controla respuestas.
2. **Service**: concentra reglas de negocio y validaciones.
3. **Repository**: acceso a datos mediante Spring Data JPA.
4. **Entity**: modelo persistente.
5. **DTO + Mapper**: contrato de entrada/salida y transformación.

Estructura principal del código:

- `config`: seguridad, JWT, OpenAPI, CORS, locale, recursos web.
- `controllers`: controladores REST por recurso.
- `services`: lógica de negocio.
- `repositories`: repositorios JPA.
- `entities`: entidades de dominio y seguridad.
- `dtos`: objetos de transferencia.
- `mappers`: transformación entidad/DTO.
- `utils`: utilidades de token y soporte.

---

## 6. Modelo de datos

### 6.1 Entidades de dominio

- `Propietario`
- `Huesped`
- `CasaRural`
- `Reserva`
- `Opinion`

### 6.2 Entidades de seguridad

- `User`
- `Role`

### 6.3 Relaciones principales

- `Propietario` 1:N `CasaRural`
- `CasaRural` 1:N `Reserva`
- `CasaRural` 1:N `Opinion`
- `Huesped` 1:N `Reserva`
- `Huesped` 1:N `Opinion`
- `User` N:M `Role` a través de `user_roles`

### 6.4 Restricciones relevantes

- Unicidad de email y teléfono en propietarios.
- Unicidad de email, DNI y teléfono en huéspedes.
- Integridad referencial en tablas relacionadas.

---

## 7. Seguridad y control de acceso

La seguridad se define en `SecurityConfig` con enfoque stateless:

- `SessionCreationPolicy.STATELESS`
- CSRF deshabilitado
- Filtro `JwtAuthenticationFilter` previo al filtro de usuario/contraseña

### 7.1 Autenticación

- Endpoint: `POST /api/v1/authenticate`
- Motor: `AuthenticationManager` + `CustomUserDetailsService`
- Hash de contraseñas: `BCryptPasswordEncoder`

### 7.2 JWT

- Generación y validación: `JwtUtil`
- Algoritmo de firma: RS256
- Claves desde keystore configurado en `JwtConfig`
- Tiempo de validez del token: 1 hora

### 7.3 Autorización por roles

Resumen operativo de permisos:

- **Público**: autenticación, Swagger, lectura de casas, recursos de `/uploads/**`.
- **USER/ADMIN**: gestión de huéspedes, opiniones y reservas (con matices).
- **MANAGER/ADMIN**: gestión de casas y propietarios.
- **ADMIN**: rutas de administración de usuarios.

---

## 8. API REST: recursos y operaciones

### 8.1 Autenticación

- `POST /api/v1/authenticate`

### 8.2 Casas (`/api/casas`)

- `GET /api/casas`
- `GET /api/casas/{id}`
- `POST /api/casas` (multipart)
- `PUT /api/casas/{id}` (multipart)
- `DELETE /api/casas/{id}`
- `GET /api/casas/{id}/opiniones`

### 8.3 Reservas (`/api/reservas`)

- `GET /api/reservas`
- `GET /api/reservas/{id}`
- `POST /api/reservas`
- `PUT /api/reservas/{id}`
- `DELETE /api/reservas/{id}`

### 8.4 Opiniones (`/api/opiniones`)

- `GET /api/opiniones`
- `GET /api/opiniones/{id}`
- `GET /api/opiniones/casa/{casaRuralId}`
- `POST /api/opiniones`
- `PUT /api/opiniones/{id}`
- `DELETE /api/opiniones/{id}`

### 8.5 Propietarios (`/api/propietarios`)

- `GET /api/propietarios`
- `GET /api/propietarios/{id}`
- `POST /api/propietarios`
- `PUT /api/propietarios/{id}`
- `DELETE /api/propietarios/{id}`

### 8.6 Huéspedes (`/api/huespedes`)

- `GET /api/huespedes`
- `GET /api/huespedes/{id}`
- `POST /api/huespedes`
- `PUT /api/huespedes/{id}`
- `DELETE /api/huespedes/{id}`

### 8.7 Usuario autenticado

- `GET /api/user`

---

## 9. Reglas de negocio implementadas

### 9.1 Casas rurales

- Se evita duplicar nombre de casa para el mismo propietario en alta y edición.
- Se permite filtrado por nombre y capacidad.

### 9.2 Huéspedes

- En alta: DNI, email y teléfono únicos.
- En edición: se evita colisión con otros huéspedes.

### 9.3 Propietarios

- Email y teléfono únicos en alta y edición.

### 9.4 Reservas

- `fechaSalida` debe ser posterior a `fechaEntrada`.
- Se bloquea solapamiento de reservas para una misma casa.
- Se recalcula `importeTotal` a partir de duración y precio por noche.

### 9.5 Opiniones

- Se restringe a una opinión por pareja huésped-casa.
- Se soporta filtrado por puntuación mínima y casa rural.

---

## 10. Gestión de archivos e imágenes

La API soporta subida de imágenes en casas rurales con `multipart/form-data`.

- Servicio implicado: `FileStorageService`.
- Publicación de archivos: `/uploads/**`.
- Mapeo de recursos estáticos mediante `WebConfig` usando `UPLOAD_PATH`.

Esto permite consumo directo por frontend para galerías y vistas de detalle.

---

## 11. Configuración, despliegue y ejecución

### 11.1 Variables de entorno

Principales variables requeridas:

- `DB_URL`, `DB_USER`, `DB_PASSWORD`, `DB_DRIVER`
- `UPLOAD_PATH`
- Configuración JWT de keystore

### 11.2 Base de datos con Docker

`docker-compose.yaml` levanta MariaDB con:

- imagen `mariadb:latest`
- puerto host `3307`
- volumen persistente `db_data`

### 11.3 Arranque local

1. Definir entorno (`.env` o variables de sistema).
2. Levantar base de datos (`docker compose up -d`).
3. Ejecutar backend con Maven Wrapper.
4. Validar API desde Swagger y/o Postman.

---

## 12. Calidad, pruebas y validación

El proyecto presenta estructura de test, pero la clase de prueba principal está comentada actualmente (`LopeBnBApplicationTests`).

Validación funcional disponible:

- Colecciones Postman incluidas en carpeta de documentación.
- Swagger para verificación manual de contrato de API.

Se recomienda como mejora inmediata:

- Activar pruebas automáticas mínimas de arranque y servicios críticos.

---

## 13. Incidencias técnicas observadas

Durante la revisión técnica se detectan los siguientes puntos:

1. En seguridad se declara pública la ruta `/api/v1/register`, pero no existe controlador asociado en estado actual.
2. `CorsConfig` y `LocaleConfig` no muestran anotación `@Configuration`, lo que puede impedir carga automática de beans.
3. `HomeController` combina patrón REST con uso de `Model` y retorno de vista, lo que sugiere ajuste pendiente de enfoque (REST vs MVC).
4. En entorno de producción conviene sustituir `ddl-auto=update` + inicialización SQL por estrategia de migraciones controladas.

Estas incidencias no invalidan el funcionamiento base para entorno académico, pero sí son relevantes para endurecer el sistema.

---

## 14. Conclusiones y propuesta de mejora

El proyecto LopeBnB cumple los objetivos de una API REST completa en entorno académico, con seguridad JWT, separación por capas y reglas de negocio relevantes para el dominio de reservas.

Fortalezas principales:

- Diseño modular y mantenible.
- Cobertura funcional de los recursos principales.
- Buen uso de DTOs, servicios y repositorios.
- Integración de Swagger y soporte para pruebas manuales.

Líneas de mejora recomendadas:

- Activar y ampliar pruebas automatizadas.
- Unificar configuración de clases transversales.
- Corregir endpoints declarados/no implementados.
- Formalizar migraciones de base de datos para producción.

---

## 15. Anexos

### Anexo A - Documentación complementaria

En la carpeta `123/` se incluyen materiales adicionales:

- Documentación técnica ampliada.
- Guía de vídeo.
- Colecciones Postman.
- Checklists de entrega y resúmenes ejecutivos.

### Anexo B - Evidencias sugeridas para entrega

Para versión final en Word/PDF se recomienda incluir capturas de:

1. Estructura del proyecto en el IDE.
2. `SecurityConfig` con matriz de rutas y roles.
3. Swagger UI con endpoints desplegados.
4. Ejecución en Postman de casos OK y casos de error controlado.
5. Tablas principales de la base de datos.
