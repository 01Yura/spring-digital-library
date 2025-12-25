package online.ityura.springdigitallibrary.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import online.ityura.springdigitallibrary.dto.BaseDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Ответ с сообщением")
public class MessageResponse extends BaseDto {
    
    @Schema(description = "Сообщение")
    private String message;
}
