package booklibrarysystem.service;

import booklibrarysystem.dto.request.CreateBookRequest;
import booklibrarysystem.model.Book;

import java.util.List;

public interface BookService {
    public Book registerBook(CreateBookRequest book);
    public List<Book> getAllBook();
}
