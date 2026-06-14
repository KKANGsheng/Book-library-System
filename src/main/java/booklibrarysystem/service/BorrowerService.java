package booklibrarysystem.service;

import booklibrarysystem.dto.request.CreateBorrowerRequest;
import booklibrarysystem.model.Borrower;

public interface BorrowerService {
    public Borrower registerBorrower(CreateBorrowerRequest request);
}
