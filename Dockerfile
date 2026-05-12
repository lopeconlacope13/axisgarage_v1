# ── Etapa 1: compilación ──────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build
COPY . .
RUN mvn clean package -DskipTests

# ── Etapa 2: runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copiamos el JAR generado en la etapa anterior
COPY --from=builder /build/target/*.jar app.jar

# Directorio donde el backend guarda las imágenes subidas por los usuarios
RUN mkdir -p /app/uploads

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
