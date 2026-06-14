package booklibrarysystem.repository;

import booklibrarysystem.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findFirstByIsbn(String isbn);
}
