package booklibrarysystem.repository;

import booklibrarysystem.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book,Long> {
    <Optional> Book findByisbn(String isbn);
    boolean existsById(Long Id);
}
