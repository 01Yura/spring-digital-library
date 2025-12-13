package online.ityura.springdigitallibrary.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Запрос на обновление отзыва")
public class UpdateReviewRequest {
    
    @Schema(description = "Обновленный текст отзыва", example = "Обновленный отзыв: книга превзошла все ожидания!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Review text is required")
    private String text;
}

