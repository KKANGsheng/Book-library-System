package booklibrarysystem.dto.response;

import booklibrarysystem.model.Book;
import booklibrarysystem.model.Borrower;

public record BorrowerResponse (
        String name,
        String email) {

    public static BorrowerResponse of(Borrower borrower) {
        return new BorrowerResponse(borrower.getName(),borrower.getEmail());
    }
}
