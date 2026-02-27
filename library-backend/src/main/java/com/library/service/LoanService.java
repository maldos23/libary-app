package com.library.service;

import com.library.dto.LoanDTO;
import com.library.entity.Book;
import com.library.entity.Loan;
import com.library.entity.User;
import com.library.mapper.EntityMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio que encapsula la lógica de negocio para préstamos.
 * Orquesta las operaciones sobre Loan, Book y User asegurando
 * consistencia transaccional.
 */
@ApplicationScoped
public class LoanService {

    @Inject
    EntityMapper mapper;

    /**
     * Registra un nuevo préstamo.
     *
     * @param dto DTO con userId y bookId
     * @return LoanDTO con los datos del préstamo creado
     * @throws IllegalArgumentException si el usuario o libro no existen
     * @throws IllegalStateException    si hay alguna restricción de negocio
     */
    @Transactional
    public LoanDTO createLoan(LoanDTO dto) {
        // Buscar entidades
        User user = User.findById(dto.userId);
        if (user == null) {
            throw new IllegalArgumentException("Usuario con ID " + dto.userId + " no encontrado.");
        }

        Book book = Book.findById(dto.bookId);
        if (book == null) {
            throw new IllegalArgumentException("Libro con ID " + dto.bookId + " no encontrado.");
        }

        // Validar que el usuario no tenga ya un préstamo activo del mismo libro
        boolean alreadyLoaned = Loan.count(
                "user.id = ?1 and book.id = ?2 and status = ?3",
                dto.userId, dto.bookId, Loan.LoanStatus.ACTIVE) > 0;
        if (alreadyLoaned) {
            throw new IllegalStateException(
                "El usuario ya tiene un préstamo activo del libro '" + book.title + "'.");
        }

        // Crear el préstamo y ejecutar lógica de negocio
        Loan loan = new Loan();
        loan.registerLoan(book, user); // valida y actualiza stock + activeLoans

        // Persistir
        Loan.persist(loan);
        return mapper.toLoanDTO(loan);
    }

    /**
     * Finaliza (devuelve) un préstamo existente.
     *
     * @param loanId ID del préstamo a finalizar
     * @return LoanDTO actualizado
     */
    @Transactional
    public LoanDTO returnLoan(Long loanId) {
        Loan loan = Loan.findById(loanId);
        if (loan == null) {
            throw new IllegalArgumentException("Préstamo con ID " + loanId + " no encontrado.");
        }
        loan.finalizeLoan(); // actualiza estado, fecha y restaura stock
        return mapper.toLoanDTO(loan);
    }

    /**
     * Retorna la lista completa de préstamos.
     */
    public List<LoanDTO> listAll() {
        return Loan.<Loan>listAll()
            .stream()
            .map(mapper::toLoanDTO)
            .collect(Collectors.toList());
    }

    /**
     * Retorna los préstamos activos de un usuario específico.
     */
    public List<LoanDTO> listActiveByUser(Long userId) {
        return Loan.<Loan>list("user.id = ?1 and status = ?2", userId, Loan.LoanStatus.ACTIVE)
            .stream()
            .map(mapper::toLoanDTO)
            .collect(Collectors.toList());
    }
}
