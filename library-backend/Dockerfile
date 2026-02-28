# ──────────────────────────────────────────────────────────────────────────────
# Etapa 1 – Build
# ──────────────────────────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /build

# Cachear dependencias primero (layer caching)
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copiar fuentes y compilar el fast-jar
COPY src ./src
RUN mvn package -DskipTests -q

# ──────────────────────────────────────────────────────────────────────────────
# Etapa 2 – Runtime (imagen mínima y endurecida para GCP Cloud Run)
# ──────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-jammy AS runtime

# Metadatos OCI
LABEL org.opencontainers.image.title="library-backend" \
      org.opencontainers.image.description="Quarkus REST API – Sistema de Gestión de Biblioteca" \
      org.opencontainers.image.version="1.0.0"

# ── Hardening del sistema operativo ──────────────────────────────────────────
# Actualizar paquetes del sistema base para eliminar CVEs conocidos
RUN apt-get update -qq \
    && apt-get upgrade -y -qq --no-install-recommends \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Usuario no-root (UID/GID fijos para reproducibilidad)
RUN groupadd -r -g 1001 appgroup \
    && useradd -r -u 1001 -g appgroup -s /sbin/nologin -d /app appuser

WORKDIR /app

# Quarkus fast-jar layout
COPY --from=builder /build/target/quarkus-app/lib/             ./lib/
COPY --from=builder /build/target/quarkus-app/*.jar            ./
COPY --from=builder /build/target/quarkus-app/app/             ./app/
COPY --from=builder /build/target/quarkus-app/quarkus/         ./quarkus/

# Directorio persistente para SQLite (montar como volumen en Cloud Run / GCE)
# /app es de solo lectura para appuser; /data es el único directorio escribible
RUN mkdir -p /data \
    && chown -R root:appgroup /app \
    && chmod -R 550 /app \
    && chown appuser:appgroup /data \
    && chmod 700 /data

USER appuser

# Cloud Run inyecta $PORT; el fallback es 8080
ENV PORT=8080
EXPOSE 8080

# Health check: GCP Cloud Run lo usa para determinar si el contenedor está listo
HEALTHCHECK --interval=15s --timeout=5s --start-period=20s --retries=3 \
  CMD curl -f http://localhost:${PORT}/q/health/live || exit 1

# Quarkus fast-jar entry point con flags JVM de seguridad y rendimiento
ENTRYPOINT ["java", \
  # Enlazar a todas las interfaces (requerido por Cloud Run)
  "-Dquarkus.http.host=0.0.0.0", \
  # Gestor de logs de JBoss (requerido por Quarkus)
  "-Djava.util.logging.manager=org.jboss.logmanager.LogManager", \
  # Fuente de entropía no bloqueante (evita cuelgues en contenedores sin /dev/random)
  "-Djava.security.egd=file:/dev/./urandom", \
  # Encoding explícito para evitar problemas de codificación de datos
  "-Dfile.encoding=UTF-8", \
  # Terminar el proceso inmediatamente ante OutOfMemoryError (fail-fast seguro)
  "-XX:+ExitOnOutOfMemoryError", \
  # Bloquear deserialización Java nativa no autorizada
  "-Djdk.serialFilter=!*", \
  "-jar", "quarkus-run.jar"]
