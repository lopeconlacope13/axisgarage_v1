# ── Fase 1: compilación ──────────────────────────────────────────────────────
# Descargamos dependencias Maven primero (capa cacheada si no cambia pom.xml)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -DskipTests -q

# ── Fase 2: imagen de ejecución (mucho más ligera) ────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/AxisGarage-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
