#!/bin/bash
# =============================================================================
# Script de backup de la base de datos MariaDB de Axis Garage
# Hace un dump completo desde el contenedor Docker y lo guarda con timestamp
# Uso: ./scripts/backup_db.sh
# =============================================================================

# Nombre del contenedor Docker que ejecuta MariaDB
CONTAINER_NAME="axisgarage_api-db-1"

# Nombre de la base de datos a respaldar
DB_NAME="axisgarage"

# Usuario y contraseña de MariaDB (root)
DB_USER="root"
DB_PASS="1234"

# Carpeta donde se guardarán los backups (relativa al proyecto)
BACKUP_DIR="$(dirname "$0")/../backups"

# Fecha actual en formato YYYYMMDD para el nombre del archivo
DATE=$(date +%Y%m%d_%H%M%S)

# Nombre completo del archivo de backup
BACKUP_FILE="$BACKUP_DIR/axisgarage_backup_$DATE.sql"

# Creamos la carpeta de backups si no existe
mkdir -p "$BACKUP_DIR"

echo "Iniciando backup de la base de datos '$DB_NAME'..."

# Ejecutamos el dump desde dentro del contenedor Docker y redirigimos la salida al archivo
docker exec "$CONTAINER_NAME" mariadb-dump -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" > "$BACKUP_FILE"

# Comprobamos si el comando anterior terminó bien (código de salida 0 = éxito)
if [ $? -eq 0 ]; then
    echo "Backup completado: $BACKUP_FILE"
    echo "Tamaño: $(du -sh "$BACKUP_FILE" | cut -f1)"
else
    echo "ERROR: El backup ha fallado. Revisa que el contenedor '$CONTAINER_NAME' esté corriendo."
    # Borramos el archivo vacío o corrupto si algo falló
    rm -f "$BACKUP_FILE"
    exit 1
fi
