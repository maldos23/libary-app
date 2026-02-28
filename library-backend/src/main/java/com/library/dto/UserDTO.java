package com.library.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para transferir datos de un usuario de la biblioteca.
 */
public class UserDTO {

    public Long id;

    @NotBlank(message = "El nombre es obligatorio")
    // Solo letras, espacios y caracteres diacríticos comunes
    @Pattern(regexp = "^[\\p{L} .'-]{2,150}$",
             message = "El nombre solo puede contener letras, espacios y guiones (2-150 caracteres)")
    public String name;

    @NotBlank(message = "El documento de identificación es obligatorio")
    // Alfanumérico con guiones, entre 4 y 20 caracteres
    @Pattern(regexp = "^[A-Za-z0-9\\-]{4,20}$",
             message = "El documento debe tener entre 4 y 20 caracteres alfanuméricos")
    public String identificationDocument;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    @Size(max = 255, message = "El email no puede superar 255 caracteres")
    public String email;

    public int activeLoans;

    // ─── Constructors ─────────────────────────────────────────────────────────

    public UserDTO() {}

    public UserDTO(Long id, String name, String identificationDocument,
                   String email, int activeLoans) {
        this.id = id;
        this.name = name;
        this.identificationDocument = identificationDocument;
        this.email = email;
        this.activeLoans = activeLoans;
    }
}
