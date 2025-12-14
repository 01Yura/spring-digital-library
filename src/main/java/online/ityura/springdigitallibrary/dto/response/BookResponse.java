package online.ityura.springdigitallibrary.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import online.ityura.springdigitallibrary.model.Genre;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookResponse {
    private Long id;
    private String title;
    private AuthorResponse author;
    private String description;
    private Integer publishedYear;
    private Genre genre;
    private BigDecimal ratingAvg;
    private Integer ratingCount;
    private Boolean hasFile;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

