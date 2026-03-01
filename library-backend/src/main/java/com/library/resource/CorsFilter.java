package com.library.resource;

import java.util.Arrays;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

/**
 * Filtro CORS manual que reemplaza la configuración declarativa de Quarkus.
 *
 * Lee CORS_ORIGINS del entorno (separados por coma), normaliza los valores
 * eliminando barras finales y comparando sin importar mayúsculas, luego
 * inyecta las cabeceras Access-Control-* cuando el Origin de la petición
 * coincide con alguno de los orígenes permitidos.
 *
 * En dev (sin CORS_ORIGINS) acepta localhost en cualquier puerto.
 */
@Provider
public class CorsFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOG = Logger.getLogger(CorsFilter.class.getName());

    /** Orígenes permitidos, normalizados (sin barra final, en minúsculas). */
    private static final Set<String> ALLOWED_ORIGINS = resolveOrigins();

    private static Set<String> resolveOrigins() {
        String env = System.getenv("CORS_ORIGINS");
        if (env == null || env.isBlank()) {
            // Dev local: acepta cualquier localhost
            LOG.info("[CORS] CORS_ORIGINS no definida — modo desarrollo (localhost).");
            return Set.of(); // vacío → lógica especial en filter()
        }
        Set<String> origins = Arrays.stream(env.split(","))
                .map(String::trim)
                .map(o -> o.endsWith("/") ? o.substring(0, o.length() - 1) : o)
                .map(String::toLowerCase)
                .filter(o -> !o.isBlank())
                .collect(Collectors.toUnmodifiableSet());
        LOG.info("[CORS] Orígenes permitidos: " + origins);
        return origins;
    }

    // ── Preflight (OPTIONS) — responder antes de que llegue al recurso ─────────
    @Override
    public void filter(ContainerRequestContext req) {
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            String origin = req.getHeaderString("Origin");
            if (origin != null && isAllowed(origin)) {
                req.abortWith(Response.ok()
                        .header("Access-Control-Allow-Origin", origin)
                        .header("Access-Control-Allow-Credentials", "true")
                        .header("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS")
                        .header("Access-Control-Allow-Headers", "Content-Type,Authorization,Accept")
                        .header("Access-Control-Max-Age", "86400")
                        .build());
            }
        }
    }

    // ── Respuestas normales — añadir cabeceras CORS ───────────────────────────
    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext res) {
        String origin = req.getHeaderString("Origin");
        if (origin != null && isAllowed(origin)) {
            var h = res.getHeaders();
            h.putSingle("Access-Control-Allow-Origin", origin);
            h.putSingle("Access-Control-Allow-Credentials", "true");
            h.putSingle("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
            h.putSingle("Access-Control-Allow-Headers", "Content-Type,Authorization,Accept");
        }
    }

    private boolean isAllowed(String origin) {
        if (ALLOWED_ORIGINS.isEmpty()) {
            // Dev: permitir cualquier localhost
            String lower = origin.toLowerCase();
            return lower.startsWith("http://localhost")
                    || lower.startsWith("http://127.0.0.1");
        }
        // Normalizar el origen de la petición igual que los permitidos
        String normalized = origin.toLowerCase();
        if (normalized.endsWith("/")) normalized = normalized.substring(0, normalized.length() - 1);
        return ALLOWED_ORIGINS.contains(normalized);
    }
}
