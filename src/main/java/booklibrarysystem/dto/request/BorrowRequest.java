package booklibrarysystem.dto.request;

import jakarta.validation.constraints.NotNull;

public record BorrowRequest(
        @NotNull(message = "BorrowerId must not be null")Long borrowerId) {
}
