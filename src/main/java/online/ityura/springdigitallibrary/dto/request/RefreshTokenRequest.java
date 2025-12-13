package online.ityura.springdigitallibrary.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Запрос на обновление access токена")
public class RefreshTokenRequest {
    
    @Schema(description = "Refresh токен", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}

