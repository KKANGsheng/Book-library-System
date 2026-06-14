package booklibrarysystem.service.impl;

import booklibrarysystem.dto.request.CreateBookRequest;
import booklibrarysystem.exception.BookStateException;
import booklibrarysystem.model.Book;
import booklibrarysystem.repository.BookRepository;
import booklibrarysystem.service.BookService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@AllArgsConstructor
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;

    @Override
    @Transactional
    public Book registerBook(CreateBookRequest request) {
        bookRepository.findFirstByIsbn(request.isbn())
                .ifPresent(existingBook -> duplicateBookValidate(existingBook, request));
        Book book = new Book(request.isbn(), request.title(), request.author());
        return bookRepository.save(book);
    }

    @Override
    public List<Book> getAllBook() {
        return bookRepository.findAll();
    }

    public void duplicateBookValidate(Book existingBook, CreateBookRequest request) {
        if (!existingBook.getTitle().equals(request.title())
                || !existingBook.getAuthor().equals(request.author())) {
            throw new BookStateException(String.format(
                    "ISBN %s is already registered as '%s' by %s, "
                            + "but the request provided '%s' by %s. "
                            + "A given ISBN must always have the same title and author.",
                    existingBook.getIsbn(),
                    existingBook.getTitle(), existingBook.getAuthor(),
                    request.title(), request.author()));
        }
    }
}
