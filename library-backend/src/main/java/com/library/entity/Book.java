package com.library.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.util.List;

/**
 * Entidad que representa un libro en el catálogo de la biblioteca.
 * Aplica POO: encapsulamiento de atributos y métodos de negocio cohesivos.
 */
@Entity
@Table(name = "books")
public class Book extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public String title;

    @Column(nullable = false)
    public String author;

    @Column(unique = true, nullable = false)
    public String isbn;

    @Column(name = "total_quantity", nullable = false)
    public int totalQuantity;

    @Column(name = "available_quantity", nullable = false)
    public int availableQuantity;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<Loan> loans;

    // ─── Business Methods ─────────────────────────────────────────────────────

    /**
     * Verifica si existe al menos un ejemplar disponible para préstamo.
     *
     * @return true si availableQuantity > 0
     */
    public boolean checkAvailability() {
        return this.availableQuantity > 0;
    }

    /**
     * Reduce en 1 el stock disponible cuando se registra un préstamo.
     * Lanza excepción si no hay ejemplares disponibles.
     */
    public void updateStock() {
        if (!checkAvailability()) {
            throw new IllegalStateException(
                "No hay ejemplares disponibles del libro: " + this.title);
        }
        this.availableQuantity--;
    }

    /**
     * Incrementa el stock cuando se registra la devolución de un préstamo.
     */
    public void restoreStock() {
        if (this.availableQuantity < this.totalQuantity) {
            this.availableQuantity++;
        }
    }
}
