package online.ityura.springdigitallibrary.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Ответ с ошибками валидации")
public class ValidationErrorResponse {
    
    @Schema(description = "Ошибки валидации по полям", example = "{\"errors\":{\"nickname\":\"Nickname must contain only letters, digits, dashes, underscores, and dots. Spaces and other special characters are not allowed\",\"password\":\"Password must contain at least one digit, one lower case, one upper case, one special character, no spaces, and be at least 8 characters long\"}}")
    private Map<String, String> errors;
}
