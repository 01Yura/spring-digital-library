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
    
    @Schema(description = "Ошибки валидации по полям", example = "{\"title\":\"Title is required\",\"authorName\":\"Author name is required\"}")
    private Map<String, String> errors;
}
