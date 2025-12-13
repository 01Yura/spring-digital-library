package online.ityura.springdigitallibrary.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Запрос на регистрацию нового пользователя")
public class RegisterRequest {
    
    @Schema(description = "Никнейм пользователя", example = "John Doe", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 3, maxLength = 50)
    @NotBlank(message = "Nickname is required")
    @Size(min = 3, max = 50, message = "Nickname must be between 3 and 50 characters")
    private String nickname;
    
    @Schema(description = "Email пользователя", example = "john@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @Schema(description = "Пароль пользователя", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 6)
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}

