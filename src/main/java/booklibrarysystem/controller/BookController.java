package booklibrarysystem.controller;

import booklibrarysystem.dto.request.CreateBookRequest;
import booklibrarysystem.dto.response.BookResponse;
import booklibrarysystem.dto.response.PageResponse;
import booklibrarysystem.model.Book;
import booklibrarysystem.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/book")
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;

    @PostMapping("/register")
    public ResponseEntity<BookResponse> registerBook(@RequestBody @Valid CreateBookRequest request) {
        Book book = bookService.registerBook(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BookResponse.of(book));
    }

    @GetMapping("/books")
    public ResponseEntity<List<BookResponse>> getAllBook() {
        List<BookResponse> responseList = bookService.getAllBook().stream()
                .map(BookResponse::of)
                .toList();
        return ResponseEntity.ok(responseList);
    }

}
