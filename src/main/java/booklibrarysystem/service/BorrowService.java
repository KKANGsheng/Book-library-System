package booklibrarysystem.service;

import booklibrarysystem.model.BorrowRecord;

public interface BorrowService {
    BorrowRecord  borrow(Long bookId, Long borrowerId);
    BorrowRecord  returnBook(Long BookId);
}
