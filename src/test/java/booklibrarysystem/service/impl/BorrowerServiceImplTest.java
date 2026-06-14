package booklibrarysystem.service.impl;

import booklibrarysystem.dto.request.CreateBorrowerRequest;
import booklibrarysystem.exception.ConflictException;
import booklibrarysystem.model.Borrower;
import booklibrarysystem.repository.BorrowerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BorrowerServiceImplTest {
    @Mock
    private BorrowerRepository borrowerRepository;
    @InjectMocks
    private BorrowerServiceImpl borrowerService;
    private static final String NAME = "ALI";
    private static final String EMAIL = "ali@test.com";

    @Test
    void registerBorrower_savesAndReturnsSuccessFully() {
        var request = new CreateBorrowerRequest(NAME, EMAIL);
        when(borrowerRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(borrowerRepository.save(any(Borrower.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        Borrower result = borrowerService.registerBorrower(request);
        assertThat(result.getName()).isEqualTo(NAME);
        assertThat(result.getEmail()).isEqualTo(EMAIL);
        verify(borrowerRepository).save(any(Borrower.class));
    }

    @Test
    void registerBorrower_throwsConflict_whenEmailAlreadyExists() {
        var request = new CreateBorrowerRequest(NAME, EMAIL);
        when(borrowerRepository.existsByEmail(EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> borrowerService.registerBorrower(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining(EMAIL);

        verify(borrowerRepository, never()).save(any());
    }
}
