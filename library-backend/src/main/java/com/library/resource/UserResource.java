package com.library.resource;

import com.library.dto.UserDTO;
import com.library.entity.Loan;
import com.library.entity.User;
import com.library.mapper.EntityMapper;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Resource REST para gestión de usuarios de la biblioteca.
 */
@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Users", description = "Gestión de usuarios de la biblioteca")
public class UserResource {

    @Inject
    EntityMapper mapper;

    @GET
    @Operation(summary = "Listar todos los usuarios")
    public List<UserDTO> listAll() {
        return User.<User>listAll()
            .stream()
            .map(mapper::toUserDTO)
            .collect(Collectors.toList());
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Obtener usuario por ID")
    public Response getById(@PathParam("id") Long id) {
        User user = User.findById(id);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\":\"Usuario no encontrado\"}")
                .build();
        }
        return Response.ok(mapper.toUserDTO(user)).build();
    }

    @POST
    @Transactional
    @Operation(summary = "Registrar un nuevo usuario")
    public Response create(@Valid UserDTO dto) {
        // Validar email único
        if (User.find("email", dto.email).firstResult() != null) {
            return Response.status(Response.Status.CONFLICT)
                .entity("{\"error\":\"Ya existe un usuario con ese email\"}")
                .build();
        }
        // Validar documento único
        if (User.find("identificationDocument", dto.identificationDocument).firstResult() != null) {
            return Response.status(Response.Status.CONFLICT)
                .entity("{\"error\":\"Ya existe un usuario con ese documento\"}")
                .build();
        }
        User user = mapper.toUser(dto);
        User.persist(user);
        return Response.status(Response.Status.CREATED)
            .entity(mapper.toUserDTO(user))
            .build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Actualizar datos de un usuario")
    public Response update(@PathParam("id") Long id, @Valid UserDTO dto) {
        User user = User.findById(id);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\":\"Usuario no encontrado\"}")
                .build();
        }
        // Validar que el nuevo email no esté en uso por otro usuario
        User emailOwner = User.find("email", dto.email).firstResult();
        if (emailOwner != null && !emailOwner.id.equals(id)) {
            return Response.status(Response.Status.CONFLICT)
                .entity("{\"error\":\"El email ya está registrado por otro usuario\"}")
                .build();
        }
        // Validar que el nuevo documento no esté en uso por otro usuario
        User docOwner = User.find("identificationDocument", dto.identificationDocument).firstResult();
        if (docOwner != null && !docOwner.id.equals(id)) {
            return Response.status(Response.Status.CONFLICT)
                .entity("{\"error\":\"El documento ya está registrado por otro usuario\"}")
                .build();
        }
        mapper.updateUser(user, dto);
        return Response.ok(mapper.toUserDTO(user)).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Eliminar un usuario")
    public Response delete(@PathParam("id") Long id) {
        User user = User.findById(id);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\":\"Usuario no encontrado\"}")
                .build();
        }
        // Restaurar el stock de los libros por cada préstamo activo del usuario
        List<Loan> activeLoans = Loan.list("user.id = ?1 and status = ?2",
                id, Loan.LoanStatus.ACTIVE);
        for (Loan loan : activeLoans) {
            loan.book.restoreStock();
        }
        user.delete(); // CascadeType.ALL elimina los préstamos asociados
        return Response.noContent().build();
    }
}
