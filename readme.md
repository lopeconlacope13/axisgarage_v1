# 📌 Documentación de Endpoints y Seguridad - API REST LopeBnB

Bienvenido a la documentación de la API de LopeBnB.  
**URL Base del Servidor:** `http://localhost:8080`  
**Formato de Intercambio de Datos:** `application/json` (Todas las peticiones y respuestas utilizan JSON).

> **🔑 NOTA SOBRE SEGURIDAD:** > Todas las rutas que no sean PÚBLICAS requieren enviar un token JWT en la cabecera de la petición HTTP.  
> `Authorization: Bearer <tu_token_jwt>`

---

## 🛡️ Jerarquía de Roles (RBAC)

El sistema utiliza un control de acceso basado en roles (Role-Based Access Control) estructurado en 3 niveles de privilegios:

### 🥉 Nivel 1: `ROLE_USER` (El Cliente / Huésped)
Es el usuario de a pie que entra a la web/Angular para buscar alojamiento.
* **Casas:** Solo puede VER (`GET /api/casas`).
* **Reservas:** Puede CREAR una reserva para él mismo (`POST /api/reservas`) y VER su propio historial de reservas (`GET /api/reservas/mis-reservas`).
* **Opiniones:** Puede CREAR una opinión (`POST /api/opiniones`) solo si ha estado en la casa.

### 🥈 Nivel 2: `ROLE_MANAGER` (El Gestor / Atención al Cliente)
Es un empleado de LopeBnB. Su trabajo es gestionar el catálogo y ayudar con las reservas.
* **Propietarios:** Puede CREAR, VER, EDITAR y BORRAR dueños de casas (`/api/propietarios/**`).
* **Casas:** Puede dar de alta nuevas casas, modificar precios o borrarlas (`POST, PUT, DELETE /api/casas/**`).
* **Reservas:** Puede ver TODAS las reservas del sistema y cancelarlas si hay un problema (`GET, DELETE /api/reservas/**`).
* **Huéspedes:** Puede gestionar las fichas de los clientes (`/api/huespedes/**`).

### 🥇 Nivel 3: `ROLE_ADMIN` (El "Jefe" / Superusuario)
Tiene acceso total. Además de poder hacer todo lo del Manager, tiene poderes exclusivos del sistema:
* **Moderación de Opiniones:** Es el único que puede BORRAR una opinión si es ofensiva o falsa (`DELETE /api/opiniones/{id}`).
* **Gestión de Usuarios (Auth):** Puede ver todos los usuarios registrados en la plataforma (`GET /api/usuarios`) y cambiar sus roles (ej. ascender a un USER a MANAGER) o banearlos.
* *(Futuro) Estadísticas:* Acceso a métricas de negocio y facturación.

---

## 🗺️ Mapa de Rutas (Endpoints)

### 🔐 1. Autenticación y Registro (Público)
*Rutas para crear cuentas y obtener el Token JWT.*

* **POST** `/api/v1/register`
    * **Permiso:** PÚBLICO
    * **Uso:** Crea un nuevo usuario en la BD (Rol por defecto: USER).
    * **Body (JSON):** `{"username": "...", "password": "...", "firstName": "...", "lastName": "..."}`
* **POST** `/api/v1/authenticate`
    * **Permiso:** PÚBLICO
    * **Uso:** Inicia sesión con credenciales y devuelve el Token JWT en formato JSON.
    * **Body (JSON):** `{"username": "...", "password": "..."}`

### 📖 2. Documentación Swagger (Público)
*Interfaz gráfica interactiva (OpenAPI 3).*

* **GET** `/swagger-ui.html` (Interfaz Web)
* **GET** `/v3/api-docs` (Definición JSON)

### 🏡 3. Casas Rurales
*Gestión del catálogo de propiedades.*

* **GET** `/api/casas` | **Permiso:** PÚBLICO | Listado paginado (`?page=0&size=10`).
* **GET** `/api/casas/{id}` | **Permiso:** PÚBLICO | Detalles de una casa en JSON.
* **POST** `/api/casas` | **Permiso:** MANAGER, ADMIN | Alta de casa (Recibe JSON).
* **PUT** `/api/casas/{id}` | **Permiso:** MANAGER, ADMIN | Modificación (Recibe JSON).
* **DELETE** `/api/casas/{id}` | **Permiso:** MANAGER, ADMIN | Baja de casa.

### 👨‍💼 4. Propietarios
*Gestión de los dueños (Restringido a empleados).*

* **GET** `/api/propietarios` | **Permiso:** MANAGER, ADMIN | Listado paginado.
* **GET** `/api/propietarios/{id}` | **Permiso:** MANAGER, ADMIN | Ficha de propietario.
* **POST** `/api/propietarios` | **Permiso:** MANAGER, ADMIN | Registro de propietario (JSON).
* **PUT** `/api/propietarios/{id}` | **Permiso:** MANAGER, ADMIN | Actualización (JSON).
* **DELETE** `/api/propietarios/{id}` | **Permiso:** MANAGER, ADMIN | Eliminación.

### 👥 5. Huéspedes
*Gestión de clientes y fichas personales.*

* **GET** `/api/huespedes` | **Permiso:** MANAGER, ADMIN | Listado paginado.
* **GET** `/api/huespedes/{id}` | **Permiso:** MANAGER, ADMIN | Ficha de huésped.
* **POST** `/api/huespedes` | **Permiso:** MANAGER, ADMIN | Alta de huésped (JSON).
* **PUT** `/api/huespedes/{id}` | **Permiso:** MANAGER, ADMIN | Actualización (JSON).
* **DELETE** `/api/huespedes/{id}` | **Permiso:** MANAGER, ADMIN | Eliminación.

### 📅 6. Reservas
*Motor de transacciones comerciales.*

* **POST** `/api/reservas` | **Permiso:** USER, MANAGER, ADMIN | Crea una reserva (JSON). Valida solapamientos.
* **GET** `/api/reservas/mis-reservas` | **Permiso:** USER, MANAGER, ADMIN | Devuelve solo las reservas del JWT autenticado.
* **GET** `/api/reservas` | **Permiso:** MANAGER, ADMIN | Listado global paginado.
* **PUT** `/api/reservas/{id}` | **Permiso:** MANAGER, ADMIN | Modifica fechas/estado (JSON).
* **DELETE** `/api/reservas/{id}` | **Permiso:** MANAGER, ADMIN | Cancela una reserva.

### ⭐ 7. Opiniones
*Sistema de valoraciones públicas.*

* **GET** `/api/opiniones/casa/{idCasa}` | **Permiso:** PÚBLICO | Lista JSON de comentarios de una propiedad.
* **POST** `/api/opiniones` | **Permiso:** USER, MANAGER, ADMIN | Publicar valoración (JSON).
* **DELETE** `/api/opiniones/{id}` | **Permiso:** ADMIN | Moderación: Borrar comentario.

### 👑 8. Gestión de Usuarios (Auth)
*Administración del sistema interno.*

* **GET** `/api/usuarios` | **Permiso:** ADMIN | Listado de todas las cuentas y sus roles.