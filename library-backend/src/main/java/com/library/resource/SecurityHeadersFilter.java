package com.library.resource;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

/**
 * Filtro JAX-RS que inyecta cabeceras de seguridad HTTP en todas las respuestas.
 *
 * Protege contra:
 *  - XSS reflejado              → Content-Security-Policy, X-XSS-Protection
 *  - Clickjacking               → X-Frame-Options
 *  - MIME sniffing              → X-Content-Type-Options
 *  - Caching de datos sensibles → Cache-Control, Pragma
 *  - Exposición de servidor     → elimina la cabecera Server
 */
@Provider
public class SecurityHeadersFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext res) {
        var h = res.getHeaders();

        // Evita que el navegador adivine el tipo MIME (MIME-sniffing)
        h.putSingle("X-Content-Type-Options", "nosniff");

        // Bloquea la carga de la página en frames (anti-clickjacking)
        h.putSingle("X-Frame-Options", "DENY");

        // Protección XSS para navegadores legacy
        h.putSingle("X-XSS-Protection", "1; mode=block");

        // No cachear respuestas API que pueden contener datos sensibles
        h.putSingle("Cache-Control", "no-store, no-cache, must-revalidate");
        h.putSingle("Pragma", "no-cache");

        // Política de contenido estricta: solo permite datos JSON de este mismo origen
        h.putSingle("Content-Security-Policy", "default-src 'none'");

        // Fuerza HTTPS en produción (browsers recordarán por 1 año)
        h.putSingle("Strict-Transport-Security", "max-age=31536000; includeSubDomains");

        // Ocultar información del servidor
        h.remove("Server");
    }
}
