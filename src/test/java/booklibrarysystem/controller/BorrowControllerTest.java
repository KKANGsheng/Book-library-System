package booklibrarysystem.controller;

import booklibrarysystem.dto.request.BorrowRequest;
import booklibrarysystem.dto.request.CreateBorrowerRequest;
import booklibrarysystem.exception.ConflictException;
import booklibrarysystem.exception.ResourceNotFoundException;
import booklibrarysystem.model.Book;
import booklibrarysystem.model.BorrowRecord;
import booklibrarysystem.model.Borrower;
import booklibrarysystem.service.BorrowService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.cglib.core.Local;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;

@WebMvcTest(BorrowController.class)
public class BorrowControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private BorrowService borrowService;
    private static final Long BOOK_ID = 10L;
    private static final Long BORROWER_ID = 20L;
    private static final Long RECORD_ID = 30L;
    private BorrowRecord activeRecord;
    private BorrowRecord returnedRecord;

    @BeforeEach
    void setup() {
        Book book = new Book("1234567890","Clean Code","Robert Martin");
        book.setId(BOOK_ID);
        Borrower borrower = new Borrower("ALI","ali@test.com");
        borrower.setId(BORROWER_ID);

        activeRecord = new BorrowRecord(book,borrower, LocalDateTime.now(),null, BOOK_ID);
        activeRecord.setId(RECORD_ID);

        returnedRecord = new BorrowRecord(book, borrower, LocalDateTime.now().minusDays(7),LocalDateTime.now(),null);
        returnedRecord.setId(RECORD_ID);
    }

    @Test
    void borrow_returns200_whenSuccess() throws  Exception {
        when(borrowService.borrow((BOOK_ID),(BORROWER_ID))).thenReturn(activeRecord);
        String body = objectMapper.writeValueAsString(new BorrowRequest(BORROWER_ID));
        mockMvc.perform(post("/api/bookrecord/{bookId}/borrow",BOOK_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.borrowRecordId").value(RECORD_ID))
                .andExpect(jsonPath("$.bookId").value(BOOK_ID))
                .andExpect(jsonPath("$.borrowerId").value(BORROWER_ID))
                .andExpect(jsonPath("$.returnedAt").doesNotExist());
    }

    @Test
    void borrow_returns400_whenBorrowerIdMissing() throws Exception {
        String body = "{}";
        mockMvc.perform(post("/api/bookrecord/{bookId}/borrow",BOOK_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void borrow_returns404_whenBookNotFound() throws Exception {
        when(borrowService.borrow((BOOK_ID),(BORROWER_ID)))
                .thenThrow(new ResourceNotFoundException("Book does not exist"));
        String body = objectMapper.writeValueAsString(new BorrowRequest(BORROWER_ID));
        mockMvc.perform(post("/api/bookrecord/{bookId}/borrow",BOOK_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Book does not exist"));
    }

    @Test
    void borrow_returns409_whenBookAlreadyBorrowed() throws Exception {
        when(borrowService.borrow((BOOK_ID),(BORROWER_ID)))
                .thenThrow(new ConflictException("Book" + BOOK_ID + "is already borrowed"));
        String body = objectMapper.writeValueAsString(new BorrowRequest(BORROWER_ID));
        mockMvc.perform(post("/api/bookrecord/{bookId}/borrow",BOOK_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void returnBook_returns200_whenSuccess() throws Exception {
        when(borrowService.returnBook((BOOK_ID))).thenReturn(returnedRecord);
        mockMvc.perform(post("/api/bookrecord/{bookId}/return",BOOK_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.borrowRecordId").value(RECORD_ID))
                .andExpect(jsonPath("$.bookId").value(BOOK_ID))
                .andExpect(jsonPath("$.returnedAt").exists());
    }

    @Test
    void returnBook_returns404_whenBookNotFound() throws Exception {
        when(borrowService.returnBook((BOOK_ID)))
                .thenThrow(new ResourceNotFoundException("Book" +BOOK_ID + "not found."));
        mockMvc.perform(post("/api/bookrecord/{bookId}/return",BOOK_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnBook_returns409_whenBookNotCurrentlyBorrowed() throws Exception {
        when(borrowService.returnBook((BOOK_ID)))
                .thenThrow(new ConflictException("Book" +BOOK_ID +" is not currently borrowed"));
        mockMvc.perform(post("/api/bookrecord/{bookId}/return",BOOK_ID))
                .andExpect(status().isConflict());
    }
}
