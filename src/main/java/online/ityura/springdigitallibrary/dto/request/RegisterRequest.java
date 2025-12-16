package online.ityura.springdigitallibrary.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Запрос на регистрацию нового пользователя")
public class RegisterRequest {
    
    @Schema(description = "Никнейм пользователя", example = "john_doe", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 3, maxLength = 50)
    @NotBlank(message = "Nickname is required")
    @Size(min = 3, max = 50, message = "Nickname must be between 3 and 50 characters")
    @Pattern(regexp = "^[\\w\\.\\-]+$", message = "Nickname must contain only letters, digits, dashes, underscores, and dots. Spaces and other special characters are not allowed")
    private String nickname;
    
    @Schema(description = "Email пользователя", example = "john@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @Schema(description = "Пароль пользователя", example = "Password123!", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 8)
    @NotBlank(message = "Password is required")
    @Pattern(
            regexp = "^(?=\\S+$)(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=]).{8,}$",
            message = "Password must contain at least one digit, one lower case, one upper case, one special character, no spaces, and be at least 8 characters long"
    )
    private String password;
}

