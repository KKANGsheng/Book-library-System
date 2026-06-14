package booklibrarysystem.controller;

import booklibrarysystem.dto.request.CreateBorrowerRequest;
import booklibrarysystem.exception.ConflictException;
import booklibrarysystem.model.Borrower;
import booklibrarysystem.service.BorrowerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BorrowerController.class)
public class BorrowerControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private BorrowerService borrowerService;
    private static final Long BORROWER_ID =10L;
    private static final String NAME = "ALI";
    private static final String EMAIL = "ali@test.com";
    private Borrower savedBorrower;

    @BeforeEach
    void setup() {
        savedBorrower = new Borrower(NAME,EMAIL);
        savedBorrower.setId(BORROWER_ID);
    }

    @Test
    void register_returns201_whenValid() throws Exception {
        when(borrowerService.registerBorrower(new CreateBorrowerRequest(NAME,EMAIL)))
                .thenReturn(savedBorrower);
        String body = objectMapper.writeValueAsString(new CreateBorrowerRequest(NAME,EMAIL));
        mockMvc.perform(post("/api/borrowers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(NAME))
                .andExpect(jsonPath("$.email").value(EMAIL));
    }

    @Test
    void register_returns400_whenNameBlank() throws Exception {
        String body = objectMapper.writeValueAsString(new CreateBorrowerRequest("", EMAIL));

        mockMvc.perform(post("/api/borrowers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_returns400_whenEmailMalformed() throws Exception {
        String body = objectMapper.writeValueAsString(new CreateBorrowerRequest(NAME, "invalidEmail"));

        mockMvc.perform(post("/api/borrowers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_returns409_whenEmailAlreadyExists() throws Exception {
        when(borrowerService.registerBorrower(new CreateBorrowerRequest(NAME, EMAIL)))
                .thenThrow(new ConflictException("A borrower with email " + EMAIL + " already exists"));

        String body = objectMapper.writeValueAsString(new CreateBorrowerRequest(NAME, EMAIL));

        mockMvc.perform(post("/api/borrowers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

}
