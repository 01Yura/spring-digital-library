package online.ityura.springdigitallibrary.controller;

import online.ityura.springdigitallibrary.dto.request.CreateRatingRequest;
import online.ityura.springdigitallibrary.dto.request.UpdateRatingRequest;
import online.ityura.springdigitallibrary.dto.response.RatingResponse;
import online.ityura.springdigitallibrary.service.RatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/books/{bookId}/ratings")
@Tag(name = "Рейтинги", description = "API для управления рейтингами книг (1-10)")
@SecurityRequirement(name = "Bearer Authentication")
public class RatingController {
    
    @Autowired
    private RatingService ratingService;
    
    @Autowired
    private online.ityura.springdigitallibrary.repository.UserRepository userRepository;
    
    @Operation(
            summary = "Поставить рейтинг книге",
            description = "Ставит рейтинг текущего пользователя на указанную книгу (от 1 до 10). " +
                    "Один пользователь может поставить только один рейтинг на книгу. " +
                    "Средний рейтинг книги автоматически пересчитывается."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Рейтинг успешно создан",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RatingResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Неверное значение рейтинга (должно быть 1-10)"),
            @ApiResponse(responseCode = "404", description = "Книга не найдена"),
            @ApiResponse(responseCode = "409", description = "Рейтинг уже существует для этой книги")
    })
    @PostMapping
    public ResponseEntity<RatingResponse> createRating(
            @Parameter(description = "ID книги", example = "1", required = true)
            @PathVariable Long bookId,
            @Valid @RequestBody CreateRatingRequest request,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        RatingResponse response = ratingService.createRating(bookId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @Operation(
            summary = "Изменить свой рейтинг книге",
            description = "Обновляет рейтинг текущего пользователя на указанную книгу. " +
                    "Средний рейтинг книги автоматически пересчитывается."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Рейтинг успешно обновлен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RatingResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Неверное значение рейтинга"),
            @ApiResponse(responseCode = "404", description = "Рейтинг не найден")
    })
    @PutMapping("/my")
    public ResponseEntity<RatingResponse> updateMyRating(
            @Parameter(description = "ID книги", example = "1", required = true)
            @PathVariable Long bookId,
            @Valid @RequestBody UpdateRatingRequest request,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        RatingResponse response = ratingService.updateRating(bookId, userId, request);
        return ResponseEntity.ok(response);
    }
    
    private Long getCurrentUserId(Authentication authentication) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }
}

