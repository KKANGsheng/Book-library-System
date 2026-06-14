package booklibrarysystem.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateBookRequest
        (@NotBlank (message = "ISBN is required")
         @Pattern(regexp ="\\d{10}|\\d{13}",message = "ISBN must be 10 or 13 digits")
         String isbn,
         @NotBlank (message = "Book title is required")
         String title,
         @NotBlank (message = "Book Author is required")
         String author) {
}
