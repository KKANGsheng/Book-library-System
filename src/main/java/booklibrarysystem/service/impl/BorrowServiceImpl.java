package booklibrarysystem.service.impl;

import booklibrarysystem.exception.ConflictException;
import booklibrarysystem.exception.ResourceNotFoundException;
import booklibrarysystem.model.Book;
import booklibrarysystem.model.BorrowRecord;
import booklibrarysystem.model.Borrower;
import booklibrarysystem.repository.BookRepository;
import booklibrarysystem.repository.BorrowRecordRepository;
import booklibrarysystem.repository.BorrowerRepository;
import booklibrarysystem.service.BorrowService;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class BorrowServiceImpl implements BorrowService {
    private final BorrowRecordRepository borrowRecordRepository;
    private final BorrowerRepository borrowerRepository;
    private final BookRepository bookRepository;

    @Override
    @Transactional
    public BorrowRecord borrow(Long bookId, Long borrowerId) {
        Borrower borrower = borrowerRepository.findById(borrowerId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrower does not exist"));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(()-> new ResourceNotFoundException("Book does not exist"));

        if (borrowRecordRepository.existsByBookIdAndReturnedAtIsNull(book.getId())) {
            throw new ConflictException("Book " +bookId + "is already borrowed");
        }
        BorrowRecord borrowRecord = new BorrowRecord(book,borrower, LocalDateTime.now(),null,book.getId());

        try {
            return borrowRecordRepository.save(borrowRecord);
        }catch (DataIntegrityViolationException exception) {
            throw new ConflictException("Book " + bookId + " is already borrowed.");
        }
    }

    @Override
    @Transactional
    public BorrowRecord returnBook(Long bookId) {
        if (!bookRepository.existsById(bookId)) {
            throw new ResourceNotFoundException("Book " + bookId + " not found.");
        }
        BorrowRecord bookRecord = borrowRecordRepository.findByBookIdAndReturnedAtIsNull(bookId)
                .orElseThrow(() -> new ConflictException("Book " + bookId + " is not currently borrowed."));

        bookRecord.setActiveBookId(null);
        bookRecord.setReturnedAt(LocalDateTime.now());
        return borrowRecordRepository.save(bookRecord);
    }
}
