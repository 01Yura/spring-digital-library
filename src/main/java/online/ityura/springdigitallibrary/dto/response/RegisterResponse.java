package online.ityura.springdigitallibrary.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import online.ityura.springdigitallibrary.model.Role;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Ответ на регистрацию нового пользователя")
public class RegisterResponse {
    
    @Schema(description = "ID пользователя", example = "1")
    private Long userId;
    
    @Schema(description = "Email пользователя", example = "user@example.com")
    private String email;
    
    @Schema(description = "Роль пользователя", example = "USER")
    private Role role;
}

