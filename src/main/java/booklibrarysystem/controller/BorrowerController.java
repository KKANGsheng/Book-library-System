package booklibrarysystem.controller;

import booklibrarysystem.dto.request.CreateBorrowerRequest;
import booklibrarysystem.dto.response.BorrowerResponse;
import booklibrarysystem.model.Borrower;
import booklibrarysystem.service.BorrowerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/borrowers")
@RequiredArgsConstructor
public class BorrowerController {
    private final BorrowerService borrowerService;

    @PostMapping
    public ResponseEntity<BorrowerResponse> register (@Valid @RequestBody CreateBorrowerRequest request) {
        Borrower borrower = borrowerService.registerBorrower(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BorrowerResponse.of(borrower));
    }

}
