package online.ityura.springdigitallibrary.controller;

import online.ityura.springdigitallibrary.dto.request.CreateReviewRequest;
import online.ityura.springdigitallibrary.dto.request.UpdateReviewRequest;
import online.ityura.springdigitallibrary.dto.response.ReviewResponse;
import online.ityura.springdigitallibrary.service.ReviewService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/books/{bookId}/reviews")
@Tag(name = "Отзывы", description = "API для управления отзывами на книги")
@SecurityRequirement(name = "Bearer Authentication")
public class ReviewController {
    
    @Autowired
    private ReviewService reviewService;
    
    @Autowired
    private online.ityura.springdigitallibrary.repository.UserRepository userRepository;
    
    @Operation(
            summary = "Создать отзыв на книгу",
            description = "Создает отзыв текущего пользователя на указанную книгу. " +
                    "Один пользователь может оставить только один отзыв на книгу."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Отзыв успешно создан",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReviewResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Книга не найдена"),
            @ApiResponse(responseCode = "409", description = "Отзыв уже существует для этой книги")
    })
    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @Parameter(description = "ID книги", example = "1", required = true)
            @PathVariable Long bookId,
            @Valid @RequestBody CreateReviewRequest request,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        ReviewResponse response = reviewService.createReview(bookId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @Operation(
            summary = "Обновить свой отзыв",
            description = "Обновляет текст отзыва текущего пользователя на указанную книгу."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Отзыв успешно обновлен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReviewResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Отзыв не найден")
    })
    @PutMapping("/my")
    public ResponseEntity<ReviewResponse> updateMyReview(
            @Parameter(description = "ID книги", example = "1", required = true)
            @PathVariable Long bookId,
            @Valid @RequestBody UpdateReviewRequest request,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        ReviewResponse response = reviewService.updateReview(bookId, userId, request);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
            summary = "Получить список отзывов на книгу",
            description = "Возвращает пагинированный список всех отзывов на указанную книгу, отсортированных по дате создания (новые первыми)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Список отзывов получен",
                    content = @Content(mediaType = "application/json")
            )
    })
    @GetMapping
    public ResponseEntity<Page<ReviewResponse>> getReviews(
            @Parameter(description = "ID книги", example = "1", required = true)
            @PathVariable Long bookId,
            @Parameter(description = "Параметры пагинации")
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(reviewService.getReviewsByBookId(bookId, pageable));
    }
    
    @Operation(
            summary = "Получить свой отзыв на книгу",
            description = "Возвращает отзыв текущего пользователя на указанную книгу."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Отзыв найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReviewResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Отзыв не найден")
    })
    @GetMapping("/my")
    public ResponseEntity<ReviewResponse> getMyReview(
            @Parameter(description = "ID книги", example = "1", required = true)
            @PathVariable Long bookId,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(reviewService.getMyReview(bookId, userId));
    }
    
    private Long getCurrentUserId(Authentication authentication) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }
}

