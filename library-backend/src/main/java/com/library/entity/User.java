package com.library.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.util.List;

/**
 * Entidad que representa un usuario inscrito en la biblioteca.
 * Aplica POO: cada instancia encapsula su estado de préstamos activos.
 */
@Entity
@Table(name = "users")
public class User extends PanacheEntityBase {

    /** Límite máximo de préstamos simultáneos permitidos por usuario. */
    public static final int MAX_LOANS = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public String name;

    @Column(name = "identification_document", unique = true, nullable = false)
    public String identificationDocument;

    @Column(unique = true, nullable = false)
    public String email;

    @Column(name = "active_loans", nullable = false)
    public int activeLoans = 0;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<Loan> loans;

    // ─── Business Methods ─────────────────────────────────────────────────────

    /**
     * Determina si el usuario puede solicitar un nuevo préstamo.
     * Un usuario puede tener como máximo {@value #MAX_LOANS} préstamos activos.
     *
     * @return true si activeLoans < MAX_LOANS
     */
    public boolean canRequestLoan() {
        return this.activeLoans < MAX_LOANS;
    }

    /**
     * Incrementa el contador de préstamos activos del usuario al registrar
     * un nuevo préstamo.
     */
    public void incrementLoans() {
        if (!canRequestLoan()) {
            throw new IllegalStateException(
                "El usuario " + this.name + " ya tiene el máximo de "
                + MAX_LOANS + " préstamos activos.");
        }
        this.activeLoans++;
    }

    /**
     * Decrementa el contador de préstamos activos al finalizar/devolver un
     * préstamo.
     */
    public void decrementLoans() {
        if (this.activeLoans > 0) {
            this.activeLoans--;
        }
    }
}
