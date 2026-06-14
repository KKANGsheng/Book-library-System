package booklibrarysystem.controller;

import booklibrarysystem.dto.request.BorrowRequest;
import booklibrarysystem.dto.response.BorrowResponse;
import booklibrarysystem.service.BorrowService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookrecord")
@AllArgsConstructor
public class BorrowController {
    private final BorrowService borrowService;

    @PostMapping("/{bookId}/borrow")
    public ResponseEntity<BorrowResponse> borrow(@PathVariable Long bookId, @Valid @RequestBody BorrowRequest borrowRequest) {
        return ResponseEntity.ok(BorrowResponse.of(borrowService.borrow(bookId,borrowRequest.borrowerId())));
    }

    @PostMapping("/{bookId}/return")
    public ResponseEntity<BorrowResponse> returnBook(@PathVariable Long bookId){
        return ResponseEntity.ok(BorrowResponse.of(borrowService.returnBook(bookId)));
    }

}
