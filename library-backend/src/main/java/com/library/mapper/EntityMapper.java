package com.library.mapper;

import com.library.dto.BookDTO;
import com.library.dto.LoanDTO;
import com.library.dto.UserDTO;
import com.library.entity.Book;
import com.library.entity.Loan;
import com.library.entity.User;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Mapper responsable de la conversión entre Entidades y DTOs.
 * Aplica el principio de responsabilidad única (SRP): toda transformación
 * de datos pasa por esta clase, manteniendo las entidades y los DTOs limpios.
 */
@ApplicationScoped
public class EntityMapper {

    // ─── Book ─────────────────────────────────────────────────────────────────

    public BookDTO toBookDTO(Book book) {
        if (book == null) return null;
        return new BookDTO(
            book.id,
            book.title,
            book.author,
            book.isbn,
            book.totalQuantity,
            book.availableQuantity
        );
    }

    public Book toBook(BookDTO dto) {
        Book book = new Book();
        book.title = dto.title;
        book.author = dto.author;
        book.isbn = dto.isbn;
        book.totalQuantity = dto.totalQuantity;
        book.availableQuantity = dto.totalQuantity; // al crear, disponibles = total
        return book;
    }

    public void updateBook(Book book, BookDTO dto) {
        book.title = dto.title;
        book.author = dto.author;
        book.isbn = dto.isbn;
        book.totalQuantity = dto.totalQuantity;
        // No sobreescribimos availableQuantity para no perder préstamos en curso
    }

    // ─── User ─────────────────────────────────────────────────────────────────

    public UserDTO toUserDTO(User user) {
        if (user == null) return null;
        return new UserDTO(
            user.id,
            user.name,
            user.identificationDocument,
            user.email,
            user.activeLoans
        );
    }

    public User toUser(UserDTO dto) {
        User user = new User();
        user.name = dto.name;
        user.identificationDocument = dto.identificationDocument;
        user.email = dto.email;
        return user;
    }

    public void updateUser(User user, UserDTO dto) {
        user.name = dto.name;
        user.identificationDocument = dto.identificationDocument;
        user.email = dto.email;
    }

    // ─── Loan ─────────────────────────────────────────────────────────────────

    public LoanDTO toLoanDTO(Loan loan) {
        if (loan == null) return null;
        return new LoanDTO(
            loan.id,
            loan.loanDate,
            loan.returnDate,
            loan.status,
            loan.user != null ? loan.user.id : null,
            loan.book != null ? loan.book.id : null,
            loan.user != null ? loan.user.name : null,
            loan.book != null ? loan.book.title : null
        );
    }
}
