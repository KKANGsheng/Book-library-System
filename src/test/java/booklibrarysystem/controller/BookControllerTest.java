package booklibrarysystem.controller;

import booklibrarysystem.dto.request.CreateBookRequest;
import booklibrarysystem.model.Book;
import booklibrarysystem.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.print.attribute.standard.Media;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
public class BookControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
//  inject  the real service bean in the spring context with Mockito mock
    @MockitoBean
    private BookService bookService;
    private CreateBookRequest createBookRequest;
    private Book savedBook;
    private static final String ISBN  ="1234567890";
    private static final String TITLE ="Clean Code";
    private static final String AUTHOR ="Robert Martin";
    private Book book1;
    private Book book2;

    @BeforeEach
    void setup() {
        createBookRequest = new CreateBookRequest("1234567890","Clean Code","Robert Martin");
        savedBook  = new Book("1234567890","Clean Code","Robert Martin");
        book1 = new Book("111111111","Java Design Pattern","Misrolav");
        book2  = new Book("222222222","Modern Java Design Pattern","Markus");
    }

/*
    This test to test positive flow when successfully the register book
 */
    @Test
    void registerBook_returns201() throws Exception {
        when(bookService.registerBook(any(CreateBookRequest.class))).thenReturn(savedBook);
        mockMvc.perform(post("/api/book/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createBookRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isbn").value(ISBN))
                .andExpect(jsonPath("$.title").value(TITLE))
                .andExpect(jsonPath("$.author").value(AUTHOR));
    }

    /*
    This test to test negative flow when isbn is non numeric
    */
    @Test
    void registerBook_returns400_whenIsbnIsNonNumeric() throws Exception {
        String badJson = "{\"isbn\":\"ABCDEFGHIJ\",\"title\":\"X\",\"author\":\"Y\"}";

        mockMvc.perform(post("/api/book/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerBook_returns400_whenIsbnBlank() throws Exception {
        String invalidJson ="{\"isbn\":\"\",\"title\":\"System Design Interview\",\"author\":\"Alex Xu\"}";
        mockMvc.perform(post("/api/book/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerBook_returns400_whenTitleBlank() throws Exception {
        String invalidJson ="{\"isbn\":\"1234567890\",\"title\":\"\",\"author\":\"Alex Xu\"}";
        mockMvc.perform(post("/api/book/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerBook_returns400_whenAuthorBlank() throws Exception {
        String invalidJson ="{\"isbn\":\"1234567890\",\"title\":\"System Design\",\"author\":\"\"}";
        mockMvc.perform(post("/api/book/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
    /*
    This test to test positive flow when successfully the get all book
    */
    @Test
    void getAllBook_returns200() throws Exception {
        when(bookService.getAllBook()).thenReturn(List.of(book1,book2));
        mockMvc.perform(get("/api/book/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].isbn").value("111111111"))
                .andExpect(jsonPath("$[0].title").value("Java Design Pattern"))
                .andExpect(jsonPath("$[0].author").value("Misrolav"))
                .andExpect(jsonPath("$[1].isbn").value("222222222"))
                .andExpect(jsonPath("$[1].title").value("Modern Java Design Pattern"))
                .andExpect(jsonPath("$[1].author").value("Markus"));
    }

    @Test
    void getAllBook_returns200_whenNoBooks() throws Exception {
        when(bookService.getAllBook()).thenReturn(List.of());
        mockMvc.perform(get("/api/book/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
