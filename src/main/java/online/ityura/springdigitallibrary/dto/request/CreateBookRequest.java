package online.ityura.springdigitallibrary.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;
import online.ityura.springdigitallibrary.model.Genre;

@Data
@Schema(description = "Запрос на создание новой книги")
public class CreateBookRequest {
    
    @Schema(description = "Название книги", example = "Spring Boot Guide", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Title is required")
    private String title;
    
    @Schema(description = "Полное имя автора", example = "John Smith", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Author name is required")
    private String authorName;
    
    @Schema(description = "Описание книги", example = "Comprehensive guide to Spring Boot framework")
    private String description;
    
    @Schema(description = "Год публикации", example = "2023", minimum = "1000", maximum = "9999")
    @Min(value = 1000, message = "Published year must be at least 1000")
    @Max(value = 9999, message = "Published year must be at most 9999")
    private Integer publishedYear;
    
    @Schema(description = "Жанр книги", example = "FICTION")
    private Genre genre;
}

