package com.library.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Entidad que representa un préstamo de un libro a un usuario.
 * Implementa polimorfismo de comportamiento mediante métodos de negocio
 * que orquestan cambios en Book y User.
 */
@Entity
@Table(name = "loans")
public class Loan extends PanacheEntityBase {

    /** Estados posibles de un préstamo. */
    public enum LoanStatus {
        ACTIVE,
        RETURNED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "loan_date", nullable = false)
    public LocalDate loanDate;

    @Column(name = "return_date")
    public LocalDate returnDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public LoanStatus status = LoanStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    public User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    public Book book;

    // ─── Business Methods ─────────────────────────────────────────────────────

    /**
     * Registra el préstamo: valida disponibilidad del libro y capacidad del
     * usuario, luego actualiza el stock y el contador de préstamos.
     *
     * @param book El libro a prestar
     * @param user El usuario que solicita el préstamo
     */
    public void registerLoan(Book book, User user) {
        // Validar precondiciones mediante métodos de negocio de cada entidad
        if (!book.checkAvailability()) {
            throw new IllegalStateException(
                "El libro '" + book.title + "' no tiene ejemplares disponibles.");
        }
        if (!user.canRequestLoan()) {
            throw new IllegalStateException(
                "El usuario '" + user.name + "' ha alcanzado el límite de "
                + User.MAX_LOANS + " préstamos simultáneos.");
        }

        this.book = book;
        this.user = user;
        this.loanDate = LocalDate.now();
        this.status = LoanStatus.ACTIVE;

        // Actualizar estado de entidades relacionadas
        book.updateStock();
        user.incrementLoans();
    }

    /**
     * Finaliza el préstamo: marca el estado como RETURNED, registra la fecha
     * de devolución y restaura el stock del libro.
     */
    public void finalizeLoan() {
        if (this.status == LoanStatus.RETURNED) {
            throw new IllegalStateException(
                "Este préstamo ya fue finalizado previamente.");
        }
        this.status = LoanStatus.RETURNED;
        this.returnDate = LocalDate.now();

        // Restaurar recursos en entidades relacionadas
        this.book.restoreStock();
        this.user.decrementLoans();
    }
}
