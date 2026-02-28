package com.library.resource;

import java.util.List;
import java.util.logging.Logger;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.library.dto.LoanDTO;
import com.library.service.LoanService;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Resource REST para gestión de préstamos.
 * Delega la lógica de negocio al {@link LoanService}.
 */
@Path("/api/loans")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Loans", description = "Gestión de préstamos de libros")
public class LoanResource {

    private static final Logger LOG = Logger.getLogger(LoanResource.class.getName());

    @Inject
    LoanService loanService;

    @GET
    @Operation(summary = "Listar todos los préstamos")
    public List<LoanDTO> listAll() {
        return loanService.listAll();
    }

    @GET
    @Path("/user/{userId}/active")
    @Operation(summary = "Listar préstamos activos de un usuario")
    public List<LoanDTO> listActiveByUser(@PathParam("userId") Long userId) {
        return loanService.listActiveByUser(userId);
    }

    @POST
    @Operation(summary = "Registrar un nuevo préstamo")
    public Response create(@Valid LoanDTO dto) {
        try {
            LoanDTO created = loanService.createLoan(dto);
            return Response.status(Response.Status.CREATED).entity(created).build();
        } catch (IllegalArgumentException e) {
            // Serialización segura mediante ErrorResponse (previene JSON-injection)
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse(e.getMessage()))
                .build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT)
                .entity(new ErrorResponse(e.getMessage()))
                .build();
        } catch (Exception e) {
            // Nunca exponer el stack trace al cliente
            LOG.severe("Error inesperado al crear préstamo: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Error interno del servidor"))
                .build();
        }
    }

    @PUT
    @Path("/{id}/return")
    @Operation(summary = "Registrar devolución de un préstamo")
    public Response returnLoan(@PathParam("id") Long id) {
        try {
            LoanDTO updated = loanService.returnLoan(id);
            return Response.ok(updated).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse(e.getMessage()))
                .build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT)
                .entity(new ErrorResponse(e.getMessage()))
                .build();
        } catch (Exception e) {
            LOG.severe("Error inesperado al procesar devolución id=" + id + ": " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Error interno del servidor"))
                .build();
        }
    }
}
