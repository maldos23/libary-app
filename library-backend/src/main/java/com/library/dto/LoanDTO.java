package com.library.dto;

import com.library.entity.Loan.LoanStatus;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * DTO para transferir datos de un préstamo.
 * Para crear un préstamo se requieren userId y bookId.
 */
public class LoanDTO {

    public Long id;

    public LocalDate loanDate;

    public LocalDate returnDate;

    public LoanStatus status;

    // IDs de relaciones (usados en creación/respuesta)
    @NotNull(message = "El ID del usuario es obligatorio")
    public Long userId;

    @NotNull(message = "El ID del libro es obligatorio")
    public Long bookId;

    // Campos de lectura (nombre del usuario y título del libro)
    public String userName;
    public String bookTitle;

    // ─── Constructors ─────────────────────────────────────────────────────────

    public LoanDTO() {}

    public LoanDTO(Long id, LocalDate loanDate, LocalDate returnDate,
                   LoanStatus status, Long userId, Long bookId,
                   String userName, String bookTitle) {
        this.id = id;
        this.loanDate = loanDate;
        this.returnDate = returnDate;
        this.status = status;
        this.userId = userId;
        this.bookId = bookId;
        this.userName = userName;
        this.bookTitle = bookTitle;
    }
}
