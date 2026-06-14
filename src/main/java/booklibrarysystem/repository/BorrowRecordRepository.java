package booklibrarysystem.repository;

import booklibrarysystem.model.BorrowRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BorrowRecordRepository extends JpaRepository<BorrowRecord,Long> {
    boolean existsByBookIdAndReturnedAtIsNull(Long bookId);
    Optional<BorrowRecord> findByBookIdAndReturnedAtIsNull(Long bookId);
}
