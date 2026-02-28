package com.library.resource;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.library.dto.BookDTO;
import com.library.entity.Book;
import com.library.entity.Loan;
import com.library.mapper.EntityMapper;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Resource REST para gestión del catálogo de libros.
 * Expone endpoints CRUD bajo /api/books.
 */
@Path("/api/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Books", description = "Gestión del catálogo de libros")
public class BookResource {

    private static final Logger LOG = Logger.getLogger(BookResource.class.getName());

    @Inject
    EntityMapper mapper;

    @GET
    @Operation(summary = "Listar todos los libros")
    public List<BookDTO> listAll() {
        return Book.<Book>listAll()
            .stream()
            .map(mapper::toBookDTO)
            .collect(Collectors.toList());
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Obtener libro por ID")
    public Response getById(@PathParam("id") Long id) {
        Book book = Book.findById(id);
        if (book == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse("Libro no encontrado"))
                .build();
        }
        return Response.ok(mapper.toBookDTO(book)).build();
    }

    @POST
    @Transactional
    @Operation(summary = "Registrar un nuevo libro")
    public Response create(@Valid BookDTO dto) {
        // Validar ISBN único
        if (Book.find("isbn", dto.isbn).firstResult() != null) {
            return Response.status(Response.Status.CONFLICT)
                .entity(new ErrorResponse("Ya existe un libro con ese ISBN"))
                .build();
        }
        Book book = mapper.toBook(dto);
        Book.persist(book);
        return Response.status(Response.Status.CREATED)
            .entity(mapper.toBookDTO(book))
            .build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Actualizar datos de un libro")
    public Response update(@PathParam("id") Long id, @Valid BookDTO dto) {
        Book book = Book.findById(id);
        if (book == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse("Libro no encontrado"))
                .build();
        }
        mapper.updateBook(book, dto);
        return Response.ok(mapper.toBookDTO(book)).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Eliminar un libro del catálogo")
    public Response delete(@PathParam("id") Long id) {
        Book book = Book.findById(id);
        if (book == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse("Libro no encontrado"))
                .build();
        }
        // Liberar el contador de préstamos de los usuarios con préstamo activo
        List<Loan> activeLoans = Loan.list("book.id = ?1 and status = ?2",
                id, Loan.LoanStatus.ACTIVE);
        for (Loan loan : activeLoans) {
            loan.user.decrementLoans();
        }
        book.delete(); // CascadeType.ALL elimina los préstamos asociados
        return Response.noContent().build();
    }
}
