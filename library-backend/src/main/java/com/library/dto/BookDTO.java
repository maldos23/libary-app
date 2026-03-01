package com.library.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para transferir datos de un libro entre el cliente y la API.
 * Desacopla la representación de red de la entidad de persistencia.
 */
public class BookDTO {

    public Long id;

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 255, message = "El título no puede superar 255 caracteres")
    public String title;

    @NotBlank(message = "El autor es obligatorio")
    @Size(max = 255, message = "El autor no puede superar 255 caracteres")
    public String author;

    @NotBlank(message = "El ISBN es obligatorio")
    // ISBN-13 sin guiones: 13 dígitos
    // ISBN-13 con guiones: NNN-N…-N…-N…-N  (ej. 978-84-206-0000-1)
    // ISBN-10 sin guiones: 9 dígitos + dígito/X
    // ISBN-10 con guiones: N…-N…-N…-N/X    (ej. 0-13-235088-4)
    @Pattern(regexp = "^[0-9]{13}$"
                    + "|^[0-9]{9}[0-9Xx]$"
                    + "|^[0-9]{3}-[0-9]{1,5}-[0-9]{1,7}-[0-9]{1,7}-[0-9Xx]$"
                    + "|^[0-9]{1,5}-[0-9]{1,7}-[0-9]{1,7}-[0-9Xx]$",
             message = "Formato de ISBN inválido (use ISBN-10 o ISBN-13, con o sin guiones)")
    @Size(max = 20, message = "El ISBN no puede superar 20 caracteres")
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
