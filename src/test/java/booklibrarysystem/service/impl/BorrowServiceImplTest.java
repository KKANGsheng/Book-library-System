package booklibrarysystem.service.impl;

import booklibrarysystem.exception.ConflictException;
import booklibrarysystem.exception.ResourceNotFoundException;
import booklibrarysystem.model.Book;
import booklibrarysystem.model.BorrowRecord;
import booklibrarysystem.model.Borrower;
import booklibrarysystem.repository.BookRepository;
import booklibrarysystem.repository.BorrowRecordRepository;
import booklibrarysystem.repository.BorrowerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BorrowServiceImplTest {
    @Mock
    private BorrowRecordRepository borrowRecordRepository;
    @Mock
    private BorrowerRepository borrowerRepository;
    @Mock
    private BookRepository bookRepository;
    @InjectMocks
    private BorrowServiceImpl borrowService;

    private static final Long bookId = 1L;
    private static final Long borrowerId = 2L;

    private Book book;
    private Borrower borrower;

    @BeforeEach
    void setUp() {
        book = new Book("1234567891", "Atomic Habit", "James Clear");
        book.setId(bookId);
        borrower = new Borrower("ALI", "ali@test.com");
        borrower.setId(borrowerId);
    }

    // ---------- borrow() ----------

    @Test
    void borrow_savesAndReturnsSuccessFully() {
        when(borrowerRepository.findById(borrowerId)).thenReturn(Optional.of(borrower));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(borrowRecordRepository.existsByBookIdAndReturnedAtIsNull(bookId)).thenReturn(false);
        when(borrowRecordRepository.save(any(BorrowRecord.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        BorrowRecord result = borrowService.borrow(bookId, borrowerId);

        assertThat(result.getBook()).isEqualTo(book);
        assertThat(result.getBorrower()).isEqualTo(borrower);
        assertThat(result.getBorrowedAt()).isNotNull();
        assertThat(result.getReturnedAt()).isNull();
        assertThat(result.getActiveBookId()).isEqualTo(bookId);
        verify(borrowRecordRepository).save(any(BorrowRecord.class));
    }

    @Test
    void borrow_throwsResourceNotFound_whenBorrowerDoesNotExist() {
        when(borrowerRepository.findById(borrowerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> borrowService.borrow(bookId, borrowerId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Borrower");

        verify(bookRepository, never()).findById(any());
        verify(borrowRecordRepository, never()).save(any());
    }

    @Test
    void borrow_throwsResourceNotFound_whenBookDoesNotExist() {
        when(borrowerRepository.findById(borrowerId)).thenReturn(Optional.of(borrower));
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> borrowService.borrow(bookId, borrowerId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Book");

        verify(borrowRecordRepository, never()).save(any());
    }

    @Test
    void borrow_throwsConflict_whenBookIsAlreadyBorrowed() {
        when(borrowerRepository.findById(borrowerId)).thenReturn(Optional.of(borrower));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(borrowRecordRepository.existsByBookIdAndReturnedAtIsNull(bookId)).thenReturn(true);

        assertThatThrownBy(() -> borrowService.borrow(bookId, borrowerId))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining(String.valueOf(bookId));

        verify(borrowRecordRepository, never()).save(any());
    }

    @Test
    void borrow_throwsConflict_whenSaveFailsWithDataIntegrityViolation() {
        when(borrowerRepository.findById(borrowerId)).thenReturn(Optional.of(borrower));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(borrowRecordRepository.existsByBookIdAndReturnedAtIsNull(bookId)).thenReturn(false);
        when(borrowRecordRepository.save(any(BorrowRecord.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate active_book_id"));

        assertThatThrownBy(() -> borrowService.borrow(bookId, borrowerId))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining(String.valueOf(bookId));
    }

    // ---------- returnBook() ----------

    @Test
    void returnBook_clearsActiveBookIdAndSaves_whenBookIsCurrentlyBorrowed() {
        BorrowRecord existing = new BorrowRecord(book, borrower, LocalDateTime.now(), null, bookId);

        when(bookRepository.existsById(bookId)).thenReturn(true);
        when(borrowRecordRepository.findByBookIdAndReturnedAtIsNull(bookId))
                .thenReturn(Optional.of(existing));
        when(borrowRecordRepository.save(any(BorrowRecord.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        BorrowRecord result = borrowService.returnBook(bookId);

        assertThat(result.getActiveBookId()).isNull();
        verify(borrowRecordRepository).save(existing);
    }

    @Test
    void returnBook_throwsResourceNotFound_whenBookDoesNotExist() {
        when(bookRepository.existsById(bookId)).thenReturn(false);

        assertThatThrownBy(() -> borrowService.returnBook(bookId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(String.valueOf(bookId));

        verify(borrowRecordRepository, never()).save(any());
    }

    @Test
    void returnBook_throwsConflict_whenBookIsNotCurrentlyBorrowed() {
        when(bookRepository.existsById(bookId)).thenReturn(true);
        when(borrowRecordRepository.findByBookIdAndReturnedAtIsNull(bookId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> borrowService.returnBook(bookId))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining(String.valueOf(bookId));

        verify(borrowRecordRepository, never()).save(any());
    }
}
