package org.iesalixar.daw2.alvarolopez.axisgarage.utils;

/**
 * Constantes de mensajes de respuesta de la API.
 * Centraliza todos los textos que se envían al cliente en respuestas de error o éxito,
 * evitando strings dispersos por los controladores.
 * Así, si hay que cambiar un mensaje, se cambia en un solo sitio.
 */
public final class MessageConstants {

    /** Constructor privado para evitar instanciación. Esta clase es solo de constantes. */
    private MessageConstants() {}

    // ── Autenticación ──────────────────────────────────────────────────────────

    /** Error: email o contraseña no proporcionados en el login. */
    public static final String AUTH_CREDENTIALS_REQUIRED = "Email and password are required.";

    /** Éxito: autenticación completada correctamente. */
    public static final String AUTH_SUCCESS = "Authentication successful.";

    /** Error: credenciales incorrectas (email o contraseña erróneos). */
    public static final String AUTH_INVALID_CREDENTIALS = "Invalid credentials. Please check your email and password.";

    /** Error inesperado durante el proceso de autenticación. */
    public static final String AUTH_UNEXPECTED_ERROR = "Unexpected error. Please try again later.";

    /** Error: fallo al registrar el usuario. */
    public static final String AUTH_REGISTER_ERROR = "Error registering user. Please try again.";

    /** Éxito: enlace de recuperación de contraseña enviado (respuesta genérica para no revelar emails). */
    public static final String AUTH_FORGOT_PASSWORD_SENT = "If an account exists with that email, you will receive a reset link shortly.";

    /** Éxito: contraseña actualizada correctamente tras el reset. */
    public static final String AUTH_PASSWORD_UPDATED = "Password updated successfully.";

    // ── Vehículo ───────────────────────────────────────────────────────────────

    /** Error: el vehículo solicitado no existe en la base de datos. */
    public static final String VEHICLE_NOT_FOUND = "El vehículo no existe.";

    /** Error: fallo al buscar el vehículo. */
    public static final String VEHICLE_FETCH_ERROR = "Error al buscar el vehículo.";

    /** Error: fallo al crear el vehículo. */
    public static final String VEHICLE_CREATE_ERROR = "Error al crear el vehículo.";

    /** Error: fallo al actualizar el vehículo. */
    public static final String VEHICLE_UPDATE_ERROR = "Error al actualizar el vehículo.";

    /** Error: fallo al subir imagen del vehículo. */
    public static final String VEHICLE_IMAGE_UPLOAD_ERROR = "Error al subir la imagen.";

    /** Error: fallo al eliminar imagen del vehículo. */
    public static final String VEHICLE_IMAGE_DELETE_ERROR = "Error al eliminar la imagen.";

    /** Error: fallo al reordenar las imágenes del vehículo. */
    public static final String VEHICLE_IMAGE_REORDER_ERROR = "Error al reordenar imágenes.";

    /** Éxito: vehículo retirado de la flota correctamente. */
    public static final String VEHICLE_RETIRED = "Vehículo retirado con éxito.";

    /** Error crítico al eliminar un vehículo. */
    public static final String VEHICLE_DELETE_CRITICAL_ERROR = "Error crítico al eliminar.";

    // ── Reserva ────────────────────────────────────────────────────────────────

    /** Error: la reserva solicitada no existe en la base de datos. */
    public static final String RESERVATION_NOT_FOUND = "La reserva solicitada no existe.";

    /** Error: fallo al buscar la reserva. */
    public static final String RESERVATION_FETCH_ERROR = "Error interno al buscar la reserva.";

    /** Error: fallo al crear la reserva. */
    public static final String RESERVATION_CREATE_ERROR = "Error interno al crear la reserva.";

    /** Error: fallo al actualizar la reserva. */
    public static final String RESERVATION_UPDATE_ERROR = "Error interno al actualizar la reserva.";

    /** Error: fallo al borrar la reserva. */
    public static final String RESERVATION_DELETE_ERROR = "Error interno al borrar la reserva.";

    /** Error: el renterId del body no coincide con el usuario autenticado en el JWT. */
    public static final String RESERVATION_RENTER_MISMATCH = "El renterId no coincide con el usuario autenticado";

    // ── Cliente (Renter) ───────────────────────────────────────────────────────

    /** Error: el cliente solicitado no existe. */
    public static final String RENTER_NOT_FOUND = "El huésped no existe.";

    /** Error: no existe ningún cliente con ese email. */
    public static final String RENTER_EMAIL_NOT_FOUND = "No existe ningún huésped con ese email.";

    /** Error: fallo al buscar el cliente. */
    public static final String RENTER_FETCH_ERROR = "Error al buscar el huésped.";

    /** Error: fallo al asegurar/crear el perfil de cliente. */
    public static final String RENTER_ENSURE_ERROR = "Error al asegurar el perfil de cliente.";

    /** Error: fallo al crear el cliente. */
    public static final String RENTER_CREATE_ERROR = "Error interno al crear el huésped.";

    /** Error: fallo al actualizar el cliente. */
    public static final String RENTER_UPDATE_ERROR = "Error interno al actualizar el huésped.";

    /** Error: fallo al borrar el cliente. */
    public static final String RENTER_DELETE_ERROR = "Error interno al borrar el huésped.";

    /** Error: el token JWT no contiene el claim 'id'. */
    public static final String RENTER_TOKEN_MISSING_ID = "Token inválido: falta el claim 'id'.";

    /** Error: la sesión ha expirado. */
    public static final String RENTER_SESSION_EXPIRED = "Sesión expirada. Vuelve a iniciar sesión.";

    // ── Factura ────────────────────────────────────────────────────────────────

    /** Error: fallo al obtener la lista de facturas. */
    public static final String INVOICE_LIST_ERROR = "Error al obtener las facturas.";

    /** Error: fallo al obtener una factura concreta. */
    public static final String INVOICE_FETCH_ERROR = "Error al obtener la factura.";

    /** Error: fallo al crear la factura. */
    public static final String INVOICE_CREATE_ERROR = "Error al crear la factura.";

    /** Error: fallo al actualizar la factura. */
    public static final String INVOICE_UPDATE_ERROR = "Error al actualizar la factura.";

    /** Error: fallo al eliminar la factura. */
    public static final String INVOICE_DELETE_ERROR = "Error al eliminar la factura.";

    /** Error: fallo al generar el PDF de la factura. */
    public static final String INVOICE_PDF_ERROR = "Error al generar el PDF.";

    // ── Informe de daños ───────────────────────────────────────────────────────

    /** Error: el informe de daños solicitado no existe. */
    public static final String DAMAGE_REPORT_NOT_FOUND = "Informe de daños no encontrado.";

    // ── Reseña (Review) ────────────────────────────────────────────────────────

    /** Error: la reseña solicitada no existe. */
    public static final String REVIEW_NOT_FOUND = "La opinión solicitada no existe.";

    /** Error: fallo al buscar la reseña. */
    public static final String REVIEW_FETCH_ERROR = "Error interno al buscar la opinión.";

    /** Error: fallo al obtener las reseñas de un vehículo. */
    public static final String REVIEW_VEHICLE_FETCH_ERROR = "Error interno al obtener las opiniones del vehículo.";

    /** Error: fallo al obtener el listado general de reseñas. */
    public static final String REVIEW_LIST_ERROR = "Error interno al obtener las opiniones.";

    /** Error: fallo al crear la reseña. */
    public static final String REVIEW_CREATE_ERROR = "Error interno al crear la opinión.";

    /** Error: fallo al actualizar la reseña. */
    public static final String REVIEW_UPDATE_ERROR = "Error interno al actualizar la opinión.";

    /** Error: fallo al borrar la reseña. */
    public static final String REVIEW_DELETE_ERROR = "Error interno al borrar la opinión.";

    // ── Cobertura ──────────────────────────────────────────────────────────────

    /** Error: cobertura no encontrada para la reserva indicada. */
    public static final String COVERAGE_RESERVATION_NOT_FOUND = "Cobertura no encontrada para esa reserva.";

    /** Error: cobertura no encontrada por ID. */
    public static final String COVERAGE_NOT_FOUND = "Cobertura no encontrada.";

    // ── Propietario (Owner) ────────────────────────────────────────────────────

    /** Error: el propietario solicitado no existe. */
    public static final String OWNER_NOT_FOUND = "El propietario no existe.";

    /** Error: fallo al buscar el propietario (incluye ID en el mensaje del servicio). */
    public static final String OWNER_FETCH_ERROR_PREFIX = "Error al buscar el propietario con ID ";

    /** Error: fallo al crear el propietario. */
    public static final String OWNER_CREATE_ERROR = "Error interno al crear el propietario";

    /** Error: fallo al actualizar el propietario. */
    public static final String OWNER_UPDATE_ERROR = "Error interno al actualizar el propietario";

    /** Error: fallo al borrar el propietario. */
    public static final String OWNER_DELETE_ERROR = "Error interno al borrar el propietario";

    // ── Sede (Location) ────────────────────────────────────────────────────────

    /** Error: la sede solicitada no existe. */
    public static final String LOCATION_NOT_FOUND = "Sede no encontrada.";

    // ── Categoría ──────────────────────────────────────────────────────────────

    /** Error: la categoría solicitada no existe. */
    public static final String CATEGORY_NOT_FOUND = "Categoría no encontrada.";

    // ── Usuario ────────────────────────────────────────────────────────────────

    /** Error: fallo al obtener la información del usuario. */
    public static final String USER_FETCH_ERROR = "Error al obtener la información del usuario.";

    /** Error: fallo al subir la imagen de perfil. */
    public static final String USER_PHOTO_UPLOAD_ERROR = "Error al subir la imagen.";

    /** Éxito: contraseña del usuario actualizada correctamente. */
    public static final String USER_PASSWORD_UPDATED = "Contraseña actualizada correctamente.";

    /** Error: fallo al cambiar la contraseña. */
    public static final String USER_PASSWORD_CHANGE_ERROR = "Error al cambiar la contraseña.";

    // ── Administración ─────────────────────────────────────────────────────────

    /** Error: fallo al cambiar el rol del usuario. */
    public static final String ADMIN_ROLE_CHANGE_ERROR = "Error al cambiar el rol.";

    /** Error: fallo al eliminar el usuario. */
    public static final String ADMIN_USER_DELETE_ERROR = "Error al eliminar el usuario.";
}
