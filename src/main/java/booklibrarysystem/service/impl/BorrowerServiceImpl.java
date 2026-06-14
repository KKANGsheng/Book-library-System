package booklibrarysystem.service.impl;

import booklibrarysystem.dto.request.CreateBorrowerRequest;
import booklibrarysystem.exception.ConflictException;
import booklibrarysystem.model.Borrower;
import booklibrarysystem.repository.BorrowerRepository;
import booklibrarysystem.service.BorrowerService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class BorrowerServiceImpl implements BorrowerService {
    private final BorrowerRepository borrowerRepository;

    @Override
    public Borrower registerBorrower(CreateBorrowerRequest request) {
        if (borrowerRepository.existsByEmail(request.email())) {
            throw new ConflictException("A borrower with email " + request.email() + " already exists");
        }
        Borrower borrower = new Borrower(request.name(),request.email());
        return borrowerRepository.save(borrower);
    }


}
