package online.ityura.springdigitallibrary.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import online.ityura.springdigitallibrary.dto.BaseDto;

@Data
@Schema(description = "Запрос на создание отзыва")
public class CreateReviewRequest extends BaseDto {
    
    @Schema(description = "Текст отзыва", example = "Отличная книга! Очень рекомендую к прочтению.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Review text is required")
    private String text;
}

