package online.ityura.springdigitallibrary.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Ответ на вход в систему")
public class LoginResponse {
    
    @Schema(description = "Access токен для аутентификации", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;
    
    @Schema(description = "Refresh токен для обновления access токена", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String refreshToken;
    
    @Schema(description = "Тип токена", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";
}

