package booklibrarysystem.dto.response;

import lombok.AllArgsConstructor;

public record ErrorResponse(
        String error,
        String title) {

}
