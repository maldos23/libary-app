package com.library.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para transferir datos de un usuario de la biblioteca.
 */
public class UserDTO {

    public Long id;

    @NotBlank(message = "El nombre es obligatorio")
    public String name;

    @NotBlank(message = "El documento de identificación es obligatorio")
    public String identificationDocument;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
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
