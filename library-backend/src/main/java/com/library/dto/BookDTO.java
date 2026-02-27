package com.library.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para transferir datos de un libro entre el cliente y la API.
 * Desacopla la representación de red de la entidad de persistencia.
 */
public class BookDTO {

    public Long id;

    @NotBlank(message = "El título es obligatorio")
    public String title;

    @NotBlank(message = "El autor es obligatorio")
    public String author;

    @NotBlank(message = "El ISBN es obligatorio")
    public String isbn;

    @NotNull
    @Min(value = 1, message = "La cantidad total debe ser al menos 1")
    public int totalQuantity;

    public int availableQuantity;

    // ─── Constructors ─────────────────────────────────────────────────────────

    public BookDTO() {}

    public BookDTO(Long id, String title, String author, String isbn,
                   int totalQuantity, int availableQuantity) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.totalQuantity = totalQuantity;
        this.availableQuantity = availableQuantity;
    }
}
