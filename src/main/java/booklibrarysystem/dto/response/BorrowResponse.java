package booklibrarysystem.dto.response;

import booklibrarysystem.model.BorrowRecord;

import java.time.LocalDateTime;

public record BorrowResponse(
        Long borrowRecordId,
        Long bookId,
        Long borrowerId,
        LocalDateTime borrowedAt,
        LocalDateTime returnedAt
) {
    public static BorrowResponse of(BorrowRecord borrowRecord) {
        return new BorrowResponse(borrowRecord.getId(),
                borrowRecord.getBook().getId(),
                borrowRecord.getBorrower().getId(),
                borrowRecord.getBorrowedAt(),
                borrowRecord.getReturnedAt());
    }

}
