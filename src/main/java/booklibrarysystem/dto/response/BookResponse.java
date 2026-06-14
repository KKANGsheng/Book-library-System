package booklibrarysystem.dto.response;

import booklibrarysystem.model.Book;

public record BookResponse
        (String isbn,
         String title,
         String author) {
    public static BookResponse of(Book book) {
        return new BookResponse(book.getIsbn(), book.getTitle(), book.getAuthor());
    }

}
