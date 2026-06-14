package booklibrarysystem.service.impl;

import booklibrarysystem.dto.request.CreateBookRequest;
import booklibrarysystem.exception.BookStateException;
import booklibrarysystem.model.Book;
import booklibrarysystem.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceImplTest {
    @Mock
    private BookRepository bookRepository;
    @InjectMocks
    private BookServiceImpl bookService;

    private static final String ISBN = "1234567891";
    private static final String TITLE = "Atomic Habit";
    private static final String AUTHOR = "James Clear";

    @Test
    void registerBook_savesAndReturnsSuccessfully() {
        var request = new CreateBookRequest(ISBN,TITLE,AUTHOR);
        when(bookRepository.findByisbn(ISBN)).thenReturn(null);
        when(bookRepository.save(any(Book.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        Book result = bookService.registerBook(request);
        assertThat(result.getIsbn().equals(ISBN));
        assertThat(result.getTitle().equals(TITLE));
        assertThat(result.getAuthor().equals(AUTHOR));
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void registerBook_throwException_whenIsbnExistsWithDifferentTitle() {
        var request = new CreateBookRequest(ISBN,"Python OOP", AUTHOR);
        Book existing = new Book(ISBN,TITLE,AUTHOR);
        when(bookRepository.findByisbn(ISBN)).thenReturn(existing);
        assertThatThrownBy(()->bookService.registerBook(request))
                .isInstanceOf(BookStateException.class)
                .hasMessageContaining(ISBN);
        verify(bookRepository, never()).save((any()));
    }

    @Test
    void registerBook_throwException_whenIsbnExistWithDifferentAuthor() {
        var request = new CreateBookRequest(ISBN, TITLE,"ABU");
        Book existing = new Book(ISBN,TITLE,AUTHOR);
        when(bookRepository.findByisbn(ISBN)).thenReturn(existing);
        assertThatThrownBy(()->bookService.registerBook(request))
                .isInstanceOf(BookStateException.class)
                .hasMessageContaining(ISBN);
        verify(bookRepository, never()).save((any()));
    }

    @Test
    void getAllBook_returnsAllBook() {
        Book book1 = new Book(ISBN,TITLE,AUTHOR);
        Book book2 = new Book("9876543210","Practical System Design","AH MENG");
        when (bookRepository.findAll()).thenReturn(List.of(book1,book2));
        List<Book> resultList = bookService.getAllBook();
        assertThat(resultList).isNotEmpty().hasSize(2).containsExactly(book1,book2);
        verify(bookRepository).findAll();
    }

}
