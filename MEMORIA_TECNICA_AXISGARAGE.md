# MEMORIA TÉCNICA DEL PROYECTO: AXIS GARAGE

**Alumno:** Álvaro López Pérez  
**Proyecto:** Axis Garage - Plataforma Exclusiva de Alquiler y Compra-Venta de Vehículos de Lujo  
**Fecha:** Marzo 2026  
**Versión:** 2.0 (Refactorización Integral a Ecosistema Luxury)

---

## 1. Introducción

En la era digital, el sector de la automoción de alta gama se presenta a menudo con el ruido y la estridencia del mercado masivo. Las plataformas existentes, aunque funcionales, carecen de la sobriedad y el respeto que la ingeniería y el diseño de estos vehículos merecen, generando una experiencia de usuario desconectada de la propia esencia del producto.

Axis Garage nace para resolver esa disonancia. Este proyecto propone el desarrollo de una aplicación web para la compra-venta y reserva de vehículos de alta gama, concebida no como un simple portal, sino como un espacio digital que refleja la elegancia, la precisión técnica y el carácter de las máquinas que alberga.

El objetivo es crear una herramienta digital donde la funcionalidad y la estética no compiten, sino que se fusionan para ofrecer una experiencia de usuario contenida, intuitiva y refinada, honrando tanto el tiempo del cliente como el arte de la ingeniería automotriz. Axis Garage no es una web. Es una declaración de principios: elegancia, precisión y propósito.

---

## 2. Descripción del Proyecto

**Objetivo General:**  
Diseñar y desarrollar una aplicación web robusta y elegante con Java, arquitectura MVC y MariaDB/MySQL, que funcione como plataforma de alquiler y compra-venta para vehículos de alta gama, priorizando una experiencia de usuario sobria y profesional.

**Objetivos Específicos:**

- Implementar un sistema de autenticación seguro para clientes y administradores.
- Desarrollar un catálogo de vehículos dinámico con un sistema de filtrado preciso e intuitivo.
- Crear un panel de administración (CRUD) para la gestión integral de vehículos, usuarios y reservas.
- Integrar un asistente de chat simulado (Chatbot Axis) para guiar al usuario y ofrecer atención personalizada.
- Construir una interfaz de usuario fluida, responsive y visualmente coherente con la identidad de la marca (Glassmorphism, Dark Theme).
- Asegurar una arquitectura de software limpia, modular y documentada bajo el patrón MVC y API REST.

**Alcance del Proyecto:**

- **Incluye:** Funcionalidades completas de registro, login, visualización y filtrado de catálogo, reserva de vehículos y gestión administrativa. El proyecto será un prototipo funcional completo.
- **No Incluye:** Integración con pasarelas de pago reales, sistemas de logística de concesionarios externos o monetización. El enfoque fundamental radica en la experiencia y la funcionalidad central.

**Público Objetivo:**  
Entusiastas del motor, coleccionistas y compradores de vehículos de alta gama que valoran la discreción, la eficiencia y una estética cuidada. Profesionales que buscan una experiencia digital tan refinada como los propios vehículos.

---

## 3. Motivación del Proyecto

Este análisis formaliza la motivación, alineando la visión del proyecto con los criterios de desarrollo académico y personal.

- **Personales:**  
  _¿Me gusta la idea?_ Sí. El proyecto es la pasión de la ingeniería automotriz de alta gama. Es una ejecución con propósito y visión de futuro.  
  _¿Tengo formación e información?_ Sí. Se cuenta con la formación completa del ciclo DAW para abordar el stack tecnológico y con un conocimiento profundo del sector para guiar la experiencia de usuario.  
  _¿Puntos fuertes?_ La disciplina, la atención al detalle y una visión estética clara.

- **Mercado:**  
  _¿Satisface una necesidad?_ Satisface una necesidad latente: la de una experiencia digital que esté a la altura del producto que presenta. El cliente de alta gama busca coherencia, confianza y discreción.  
  _¿Hay competencia?_ La competencia funcional es amplia, pero compite en volumen, no en experiencia. Ninguno ofrece el enfoque de "galería digital" curada y sobria que proponemos.

- **Recursos:**  
  _¿Puede ser rentable?_ Como prototipo académico, su rentabilidad no es financiera, sino de portafolio para mi desarrollo profesional el día de mañana. Ojalá también poder implementar esta "plantilla de aplicación web" y comercializarla a concesionarios de lujo directos.  
  _¿Disponibilidad?_ Se cuenta con todos los recursos técnicos y de software necesarios proporcionados por el entorno académico.

- **Innovación:**  
  _¿Es innovadora?_ La innovación reside en la aplicación radical de los principios de diseño. El aspecto diferenciador es la experiencia de usuario contenida, la robustez asíncrona (SPA) y el silencio estético mediante el uso de _Glassmorphism_ dinámico.

---

## 4. Stack Tecnológico

A continuación, se detalla y justifica la arquitectura tecnológica sobre la que se asienta de manera robusta Axis Garage, conformando sus tres núcleos principales:

- **Backend:** Spring Boot (Java) + Spring Data JPA + Spring Security, MariaDB.
- **Frontend:** Angular (SPA) y/o plantillas HTML estructuradas, Bootstrap/Custom CSS/Angular Material y JavaScript.
- **DevOps:** Docker / Docker Compose, Proxmox (despliegue en VM/LXC), Nginx reverse proxy, subdominio por alumno, GitHub (repos, issues, PRs, Actions).

### Justificación del Stack:

**Backend (Spring Boot + MariaDB):**

- **Madurez y ecosistema:** Spring Boot permite levantar APIs REST de forma rápida y escalable. Integra de forma nativa la seguridad mediante Spring Security, la persistencia estructural a la base de datos mediante JPA/Hibernate, y la documentación técnica pública a través de Springdoc/OpenAPI.
- **Productivo y educativo:** Enseña patrones estándar y robustos de la industria (arquitectura en capas Controller–Service–Repository, DTOs de frontera, validaciones de integridad).
- **SQL Realista:** Empleamos MariaDB, una solución open-source libre, compatible con MySQL y sumamente usada en entornos de producción, facilitando migraciones versionadas.
- **Escalabilidad:** El diseño REST aislado y empaquetado permite escalar servicios independientes del front.

**Frontend (Angular y Componentes Híbridos):**

- **Angular:** Framework inmensamente robusto para Single-Page Applications (SPA), propulsado por un tipado estricto gracias a TypeScript, reactividad asíncrona mediante RxJS y modularidad.
- **Bootstrap y Angular Material:** Bootstrap acelera la maquetación responsive garantizando accesibilidad nativa. Combinado con Angular Material (y Custom CSS para lograr el efecto _Glassmorphism_ oscuro), logramos una coherencia visual extrema sin reinventar componentes atómicos que ralentizarían el delivery del proyecto.

**DevOps (Docker, Proxmox, GitHub, Nginx):**

- **Docker:** Empaqueta tanto API como BD para garantizar reproducibilidad absoluta al 100% en todos los equipos.
- **Proxmox:** Entorno realista de virtualización/contenedores para escenarios de despliegue finales en instancias LXC.
- **Nginx reverse proxy:** Publica los contenedores internamente bajo subdominios asegurados con TLS (Let's Encrypt).
- **GitHub:** Repositorio base, integrando prácticas como Issues, PR reviews, y automatizaciones vía Actions para CI/CD continuo.

---

## 5. Análisis de Requisitos

El modelo conductual del sistema Axis Garage se compone de los siguientes requisitos, estructurados según los actores del sistema y exigencias técnicas.

### Actores y Roles

- **Usuario Anónimo:** Es un actor pasivo. Visitante que puede explorar el catálogo de vehículos y su información técnica fundamental.
- **Cliente (Usuario Autenticado):** Perfil registrado (Renter). Usa las funcionalidades principales: gestión de perfil interno, solicitud formal de reservas sobre el inventario, revisión de vehículos.
- **Administrador (Gerencia/Owner):** Autenticación de alto privilegio. Gestiona usuarios activos, roles, y ostenta un control CRUD absoluto sobre entidades maestras del negocio: los **Vehículos** y las **Reservas**.

### Requisitos Funcionales (RF)

**Autenticación y Cuentas**

- **RF-01:** Gestión de Cuentas - Registro: Un usuario podrá crear una cuenta nueva empleando un email único transaccional y fijando una contraseña segura.
- **RF-02:** Gestión de Cuentas - Autenticación: Los usuarios podrán iniciar sesión autorizada en el sistema (Login por Token JWT/Sesión).
- **RF-03:** Gestión de Cuentas - Perfil: El Cliente podrá visualizar y editar sin restricciones los datos básicos de su perfil en tiempo real.

**Autorización y Roles**

- **RF-04:** Control de Acceso por Rol: El modelo inyectará una comprobación interceptada de los permisos del usuario (Cliente vs. Admin) para determinar qué recursos REST y qué pantallas de Frontend Angular pueden observarse y/o modificarse.

**Módulo de Catálogo y Detalle**

- **RF-05:** Módulo de Catálogo - Visualización: Cualquier usuario (público o privado) podrá visualizar el listado de todos los modelos exclusivos incorporados en flota.
- **RF-06:** Módulo de Catálogo - Filtrado: Los usuarios podrán filtrar el catálogo visualmente aplicando cotas de marca, modelo y rango de precios o potencia (CV).
- **RF-07:** Módulo de Vehículo - Ficha de Detalle: Se mostrará un "Showroom" dedicando la mitad de pantalla a una galería inmersiva de media-alta resolución, y otra mitad a fichas técnicas numéricas, de equipamiento y de contacto directo.

**Módulo de Reservas y Gestión Administrativa**

- **RF-08:** Módulo de Reservas - Creación: Un Cliente autenticado podrá registrar firmemente una solicitud de reserva sobre un vehículo tasado, previendo fecha de interés.
- **RF-09:** Panel de Administración - Gestión de Vehículos: El administrador mantiene una vista `Dashboard` nativa donde tiene control CRUD (Crear, Leer, Actualizar mediante formularios fotográficos, y Eliminar lógicamente) sobre la tabla de Vehículos.
- **RF-10:** Panel de Administración - Gestión de Usuarios: El Administrador podrá listar su base de datos de usuarios (Leads), activarles/desactivarles cuentas limitadas, y rotar sus jerarquías de roles de control.
- **RF-11:** Asistente Chatbot Axis: Integración final donde el cliente tendrá a un asesor cognitivo básico simulado para encauzar a la pasarela de contacto real respondiendo a FAQs sobre política de reservas y características.

### Requisitos No Funcionales (RNF)

- **RNF-01 [Rendimiento]:** Menor a 1.5 - 2s de _Time To First Byte_ (TTFB) en cargas de recursos estáticos e invocaciones DTO sencillas para la visualización de catálogos concurrentes.
- **RNF-02 [Seguridad]:** Se impedirá que las contraseñas viajen en formato plano por base de datos, hasheándose vía asimétrica (BCrypt). Políticas activas de CORS para prevenir secuestros de dominio entre aplicaciones front y back aisladas y validación pre-commit de Inputs (no Script tags, etc).
- **RNF-03 [Usabilidad e Interfaz Cuidada]:** UI/UX hiperenfocada al Responsive Design empleando Bootstrap, implementando niveles sólidos WCAG para lectura de contrastes mínimos sobre fondos oscuros (Dark Mode master).
- **RNF-04 [Mantenibilidad y Clean-Code]:** Las dependencias (Controller–Service–Repository) quedan claramente inyectadas como componentes singleton mediante Inversión de Control de Spring. Los datos en las vistas Angular nunca llaman al objeto relacional, sino mapeos estancos `DTO` preconfigurados.
- **RNF-05 [Portabilidad]:** Arquitectura montada y validada integralmente sobre base de orquestación `docker-compose.yml`, haciéndola estanca frente a SO host.
- **RNF-06 [Auditoría]:** El sistema estampará Logs transaccionales al detectar acciones sobre la capa Administrativa que cambien entidades CRUD (creaciones y reservas).

---

## 6. Diseño Preliminar

La estructura de datos ha sido orquestada para ser un reflejo exacto y riguroso de las necesidades matemáticas y comerciales de Axis Garage.

### Diccionario de Entidades y Modelos (Schema SQL Nativo)

El motor de Axis Garage descansa sobre una arquitectura relacional altamente normalizada, separando nítidamente clientes, flotistas y las transacciones de alto nivel.

**1. Entidades de Identidad de Dominio: `owners` y `renters`**  
Se ha extirpado el concepto obsoleto de _Usuario Mixto_ del negocio core para garantizar independencia entre quienes aportan la máquina y quienes la disfrutan.

- **`owners` (Propietarios)**: Custodios de la flota.
  - _PK:_ `id` (BIGINT).
  - _Atributos clave:_ `name`, `last_name`, `email` (UNIQUE), `phone` (UNIQUE).

- **`renters` (Huéspedes / Clientes VIP)**: Usuarios finales con acceso a catálogo.
  - _PK:_ `id` (BIGINT).
  - _Atributos clave:_ `name`, `last_name`, `email` (UNIQUE), `dni` (UNIQUE), `phone` (UNIQUE).

**2. Entidad Central: `vehicles` (Vehículos de Lujo)**  
El corazón técnico de Axis Garage. Despliega todas las especificaciones de la ingeniería del producto.
| Atributo (Columna) | Tipo SQL | Restricciones / Índices | Descripción Funcional |
| :--- | :--- | :--- | :--- |
| `id` | BIGINT | PK, AUTO_INCREMENT | Identificador de chasis interno en BBDD. |
| `owner_id` | BIGINT | FK (owners) | Propietario fiduciario del vehículo. |
| `brand` & `model` | VARCHAR | NOT NULL | Marca (ej. Porsche) y Denominación (ej. 911 GT3 RS). |
| `production_year` | INT | NOT NULL | Año de ensamblaje (ej. 2024). |
| `price_per_day` | DECIMAL(10,2)| NOT NULL | Tarifa de arrendamiento por bloque de uso (24h). |
| `engine_type` | VARCHAR | NOT NULL | Tipología del bloque motor (ej. V8 Biturbo, Flat-6). |
| `horse_power` | INT | NOT NULL | Rendimiento absoluto en CV. |
| `torque_nm` | INT | NOT NULL | Par máximo generado medido en Nm. |
| `transmission` | VARCHAR | NOT NULL | Automática, Manual, PDK de doble embrague, etc. |
| `drivetrain` | VARCHAR | NOT NULL | Tracción (RWD, AWD, FWD). |
| `fuel_type` | VARCHAR | NOT NULL | Combustible o energía requerida (Gasolina 98, Eléctrico). |
| `zero_to_hundred`| DECIMAL(3,1)| | Aceleración de 0-100 km/h en segundos. |
| `available` | BOOLEAN | DEFAULT TRUE | Disponibilidad técnica física del vehículo para alquileres. |

**3. Entidad Visual: `vehicle_images` (Galería Multimedia)**  
Modelado Many-to-One para las Galerías Showroom.
| Atributo | Tipo SQL | Restricciones / Índices | Descripción |
| :--- | :--- | :--- | :--- |
| `id` | BIGINT | PK, AUTO_INCREMENT | Secuencial de archivo visual. |
| `vehicle_id` | BIGINT | FK (vehicles), ON DELETE CASCADE | Apunta estrictamente al coche al que representa. |
| `file_name` | VARCHAR | NOT NULL | String que concatena el File System Name físico servido por /uploads/. |
| `is_main` | BOOLEAN | DEFAULT FALSE | Etiqueta para seleccionar la imagen Hero (Showroom de Axis). |

**4. Pipeline Operativo: `reservations` (Reservas)**  
Modela el Pipeline de leads y el contrato temporal. Fuente de verdad paramétrica de estados.
| Atributo | Tipo SQL | Restricciones / Índices | Descripción |
| :--- | :--- | :--- | :--- |
| `id` | BIGINT | PK, AUTO_INCREMENT | Tracking number para la cita física real. |
| `vehicle_id` | BIGINT | FK (vehicles) | Vehículo interconectado en esta fecha. |
| `renter_id` | BIGINT | FK (renters) | Cliente enajenador temporal del bien. |
| `start_date` | DATE | NOT NULL | Fecha de estricto inicio (Pickup). |
| `end_date` | DATE | NOT NULL | Fecha de estricta conclusión (Drop-off). |
| `total_price` | DECIMAL(10,2)| NOT NULL | Montante económico blindado en el instante del pacto. |
| `status` | VARCHAR | DEFAULT 'PENDING' | Trazabilidad ('PENDING', 'CONFIRMED', 'CANCELLED'). |

**5. Sistema de Audición Pública: `reviews` (Reseñas VIP)**  
Asegurado por BD: Sólo perfiles Renter con reservas adyacentes pueden opinar.
| Atributo | Tipo SQL | Restricciones / Índices | Descripción |
| :--- | :--- | :--- | :--- |
| `id` | BIGINT | PK, AUTO_INCREMENT | Tracking de auditoría de la opinión. |
| `reservation_id` | BIGINT | FK (reservations) | Fuerza que la crítica nazca biunívocamente de un uso real. |
| `renter_id` | BIGINT | FK (renters) | Autor de la reseña. |
| `rating` | INT | NOT NULL | Calificación estelar de la experiencia. |
| `comment` | TEXT | | Elaboración escrita opcional de su experiencia en ruta. |

**6. Motor de Seguridad: `users`, `roles`, `user_roles`**  
Axis Garage segrega la seguridad estricta del negocio. Las tablas `users` (`username`, `password`, `enabled`) y `roles` orquestan los JWT bajo Spring Security de forma transversal a la plataforma, totalmente desacoplados de los clientes del garaje `renters/owners`.

### Relaciones entre Entidades Core

- **owners (1) ←→ (N) vehicles**: Un custodio puede poseer múltiples coches de alta gama dentro del garaje virtual, pero todo coche rinde cuentas únicamente a un propietario (`owner_id` FK).
- **vehicles (1) ←→ (N) vehicle_images**: Relacionado fuertemente `ON DELETE CASCADE`. Un vehículo aglutina _N_ fotos, si el vehículo es exterminado de stock, la BBDD pulveriza las imágenes satélites para no almacenar basura.
- **renters (1) ←→ (N) reservations**: Un perfil de alto poder adquisitivo generará recurrentes reservas a lo largo de las temporadas con Axis Garage (`renter_id` FK).
- **vehicles (1) ←→ (N) reservations**: Un chasis soportará múltiples contratos en su vida, acotados y validados todos por su lógica de solapamiento `start_date` e `end_date` correspondientes (`vehicle_id` FK).
- **reservations (1) ←→ (1/N) reviews**: Una reserva concluida permite arrojar una declaración (reseña) del cliente que ejecutó la misma, vinculando la `reservation_id` y `renter_id` obligatoriamente.

### Lógica de Integridad de Datos SQL

La integridad está forzada tanto en la capa de datos (Motor MariaDB) como en la capa de transacciones (Spring Data).

**Fuente Única de Verdad (Single Source of Truth) para Disponibilidad**

- El estado general de ocupación del modelo del vehículo en Axis Garage es una propiedad indirecta. Su disponibilidad deriva de sub-ejecutar una consulta que evalúa las fechas asignadas con `status = 'CONFIRMED'` en la tabla `reservations`. Las máquinas y su bandera de `available` nativo se complementan combinando el estatus de alquiler con bloqueos administrativos absolutos.

**Integridad Referencial (ON DELETE vs RESTRICT vs CASCADE)**

- En el submódulo multimedia (`vehicle_images`), aplica `ON DELETE CASCADE` en SQL garantizando limpieza asincrónica de la tabla adjunta al coche.
- En el submódulo financiero y relacional (`renters` hacia `reservations`, u `owners` hacia `vehicles`), operan restricciones clásicas. Ningún Administrador ni Sistema podrá fulminar (eliminar) la cuenta de un Renter si este ostenta reservas pasadas o activas; preservando inquebrantablemente el historial de contratos vinculantes.

**Restricciones de Unicidad Extendidas**

- Interceptación constante a nivel de Base de Datos Nativa con columnas `UNIQUE` aplicadas a restricciones para `users.username`, `owners.email`, `owners.phone`, `renters.dni` y `renters.email`, impidiendo que colisiones erróneas corrompan el motor del garaje virtual de Axis o den pie al robo orgánico de cuentas por tokens engañosos.

---

## 7. API, Interfaz y Evolución del Prototipo (Entrega 2 vs Entrega 1)

**A. Explicación y Rutas de la API REST**
La API de Axis Garage ha sido refactorizada íntegramente de extremo a extremo. Opera bajo la base `/api/` devolviendo cargas JSON estructuradas a través de sus DTOs blindados.

**Mapa de Endpoints Analizados en Producción:**

- **Módulo de Autenticación (`AuthenticationController`)**
  - `POST /api/v1/authenticate` (Login que devuelve el Token JWT firmado)
- **Módulo de Identidad (`UserController`)**
  - `GET /api/user` (Recupera la identidad del propietario del token activo)
- **Módulo de Vehículos (`VehicleController`)**
  - `GET /api/vehicles` (Listado paramétrico y público del catálogo)
  - `GET /api/vehicles/{id}` (Ficha técnica individual por bastidor)
  - `POST /api/vehicles` (`consumes = "multipart/form-data"` - Alta con imágenes adjuntas)
  - `PUT /api/vehicles/{id}` (`consumes = "multipart/form-data"` - Edición)
  - `DELETE /api/vehicles/{id}` (Borrado y extinción en cascada de sus galerías)
- **Módulo de Acuerdos / Reservas (`ReservationController`)**
  - `GET /api/reservations` (Lista de expedientes de alquiler)
  - `GET /api/reservations/{id}` (Detalle del expediente)
  - `POST /api/reservations` (Firma digital de una nueva reserva)
  - `PUT /api/reservations/{id}` (Edición de condiciones o estados)
  - `DELETE /api/reservations/{id}` (Anulación comercial extrema)
- **Módulo de Flotistas (`OwnerController`)**
  - `GET /api/owners`
  - `GET /api/owners/{id}`
  - `POST /api/owners`
  - `PUT /api/owners/{id}`
  - `DELETE /api/owners/{id}`
- **Módulo de Invitados VIP (`RenterController`)**
  - `GET /api/renters`
  - `GET /api/renters/{id}`
  - `POST /api/renters`
  - `PUT /api/renters/{id}`
  - `DELETE /api/renters/{id}`
- **Módulo de Interacción y Feedback (`ReviewController`)**
  - `GET /api/reviews`
  - `GET /api/reviews/{id}`
  - `GET /api/reviews/reservation/{reservationId}` (Filtrado de reseñas relativas estrictamente a un uso validado)
  - `POST /api/reviews`
  - `PUT /api/reviews/{id}`
  - `DELETE /api/reviews/{id}`

- Todas las rutas ajenas a lecturas públicas requieren un Token `JWT` válido, interceptado por filtros de Seguridad garantizando su uso autorizado.

**B. La Nueva Página de Inicio (Showroom) y UI**
La página de presentación SPA (`home.html`) deja atrás el enfoque generalista de un portal inmobiliario para abrazar una identidad corporativa exclusiva.

- **Hero Section:** Imagen de impacto visual a pantalla completa. El logotipo de _Axis Garage_ está fundido en un entorno oscuro y gradientes que transmiten el lujo inquebrantable de la marca.
- **Tarjetas Glassmorphism:** El catálogo inicial exhibe los vehículos a través de tarjetas con esquinas redondeadas, fondos translúcidos estilo _cristal ahumado oscuro_ y sutiles animaciones de elevación aerodinámica al pasar el ratón (_hover_), informando de atributos primordiales de la máquina (Marca, Modelo, CV y Precio/Día) usando tipografías corporativas combinadas (_Playfair Display_ para lujo, e _Inter_ para especificaciones).

**C. Cambios Sufridos en Prototipo y Base de Datos (Entrega 2 - Axis Garage vs Entrega 1 - LopeBnB)**

1. **Evolución Visual del Prototipo UI/UX:**
   - De **Sistema de Alojamientos Rurales** a **Concesionario de Alta Gama**.
   - Se ha diseñado una paleta Master Variables CSS completamente nueva y global: del tono rural/rústico luminoso a negros absolutos, grises grafito y detalles oro viejo corporativo (`--axis-gold`).
   - Las antiguas e impersonales tablas orgánicas de _Angular Material_ (`mat-table`) han sido sobrescritas sin perder funcionalidad, sustituyéndose por Dashboards inmersivos.
   - El formulario de detalle genérico ha sido partido. Ahora es un imponente "Layout Showroom a Dos Columnas", separando de forma agresiva una foto vertical gigantesca a izquierda, y a la derecha las especificaciones técnicas separadas del panel de contacto de Reserva Activa.

2. **Evolución del Esquema H2/MariaDB y Endpoints Spring:**
   - La refactorización ha sido total bajo el paraguas Spring Data JPA, realizando un mapeo estricto 1-a-1 de las entidades nativas:
     - `casarural` → `vehicles`
     - `propietario` → `owners`
     - `huesped` → `renters`
     - `reserva` → `reservations`
     - `opinion` → `reviews`
     - `casarural_imagenes` → `vehicle_images`
   - **Atributos Reimaginados:** Los campos anticuados que medían _habitaciones_ o _ubicación_ han mutado a la máxima precisión mecánica: `engine_type`, `horse_power`, `torque_nm`, `transmission` y tiempos de `zero_to_hundred`.
   - **Lógica Temporal Deductiva:** Se ha extirpado el valor _estado o disponibilidad física_ de la casa rural. Ahora, la matemática del garaje cruza las fechas validadas (`start_date` a `end_date`) de la tabla de reservas con estatus confirmado. Si las fechas deseadas por un _Renter_ chocan digitalmente con las que posee otro ticket de la base de datos... Axis Garage negará la transacción, asegurando integridad sin ensuciar la entidad abstracta del `Vehicle`.

---

## 8. Propósito Final

Construir un prototipo funcional de aplicación web que:

1. Cumpla y exceda métricamente con los estándares del currículo académico correspondiente a 2º de DAW.
2. Demuestre comprensión esférica absoluta del ciclo tecnológico completo (Bases de datos SQL vinculadas, Lógica REST y Seguridad Java de Middleware, Renderizaciones y asincronía TypeScript de Frontend DOM).
3. **Refleje una identidad personal sólida y coherente.**

Axis Garage no se limita a engordar métricas y aprobar un proyecto de titulación técnica. Es la carta de presentación y el reflejo de una mente que respeta su tiempo de ingeniería, rechaza firmemente plantillas pre-fabricadas sin propósito y no negocia el buen gusto y la profundidad en sus realizaciones de arquitectura de software.
