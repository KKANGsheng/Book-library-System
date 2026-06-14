package booklibrarysystem.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateBorrowerRequest
        (@NotBlank(message = "name must not be blank")
         String name,
         @NotBlank
         @Email(message = "email must be a valid address")
         String email){
}
