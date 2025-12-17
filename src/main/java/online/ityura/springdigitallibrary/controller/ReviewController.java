package online.ityura.springdigitallibrary.controller;

import online.ityura.springdigitallibrary.dto.request.CreateReviewRequest;
import online.ityura.springdigitallibrary.dto.request.UpdateReviewRequest;
import online.ityura.springdigitallibrary.dto.response.ErrorResponse;
import online.ityura.springdigitallibrary.dto.response.ReviewResponse;
import online.ityura.springdigitallibrary.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/books/{bookId}/reviews")
@Tag(name = "Отзывы", description = "API для управления отзывами на книги")
@RequiredArgsConstructor
public class ReviewController {
    
    private final ReviewService reviewService;
    
    private final online.ityura.springdigitallibrary.repository.UserRepository userRepository;
    
    @Operation(
            summary = "Создать отзыв на книгу",
            description = "Создает отзыв текущего пользователя на указанную книгу. " +
                    "Один пользователь может оставить только один отзыв на книгу."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Отзыв успешно создан",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReviewResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Неверный формат данных",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":400,\"error\":\"VALIDATION_ERROR\",\"message\":\"Validation failed\",\"fieldErrors\":{\"text\":\"Review text is required\"},\"timestamp\":\"2025-12-17T13:20:00Z\",\"path\":\"/api/v1/books/1/reviews\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Книга не найдена",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":404,\"error\":\"BOOK_NOT_FOUND\",\"message\":\"Book not found with id: 1\",\"timestamp\":\"2025-12-17T13:20:00Z\",\"path\":\"/api/v1/books/1/reviews\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Отзыв уже существует для этой книги",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":409,\"error\":\"REVIEW_ALREADY_EXISTS\",\"message\":\"Review already exists for this book\",\"timestamp\":\"2025-12-17T13:20:00Z\",\"path\":\"/api/v1/books/1/reviews\"}")
                    )
            )
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
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Отзыв успешно обновлен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReviewResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Неверный формат данных",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":400,\"error\":\"VALIDATION_ERROR\",\"message\":\"Validation failed\",\"fieldErrors\":{\"text\":\"Review text is required\"},\"timestamp\":\"2025-12-17T13:20:00Z\",\"path\":\"/api/v1/books/1/reviews/my\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Отзыв не найден",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":404,\"error\":\"REVIEW_NOT_FOUND\",\"message\":\"Review not found\",\"timestamp\":\"2025-12-17T13:20:00Z\",\"path\":\"/api/v1/books/1/reviews/my\"}")
                    )
            )
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
            summary = "Получить список отзывов на книгу (доступно без авторизации)",
            description = "Возвращает пагинированный список всех отзывов на указанную книгу с возможностью сортировки. " +
                    "Параметры пагинации: `page` (номер страницы, по умолчанию 0), `size` (размер страницы, по умолчанию 20), " +
                    "`sort` (сортировка, по умолчанию `createdAt,desc` - новые первыми). " +
                    "Доступные поля для сортировки: `createdAt` (дата создания), `updatedAt` (дата обновления). " +
                    "Примеры: `createdAt,desc`, `createdAt,asc`, `updatedAt,desc`."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Список отзывов получен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Книга не найдена или отзывы отсутствуют",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Book not found",
                                            value = "{\"status\":404,\"error\":\"BOOK_NOT_FOUND\",\"message\":\"Book not found with id: 1\",\"timestamp\":\"2025-12-17T13:20:00Z\",\"path\":\"/api/v1/books/1/reviews\"}",
                                            summary = "Книга не найдена"
                                    ),
                                    @ExampleObject(
                                            name = "No reviews found",
                                            value = "{\"status\":404,\"error\":\"NOT_FOUND\",\"message\":\"No reviews found for book with id: 1\",\"timestamp\":\"2025-12-17T13:20:00Z\",\"path\":\"/api/v1/books/1/reviews\"}",
                                            summary = "Отзывы отсутствуют"
                                    )
                            }
                    )
            )
    })
    @GetMapping
    public ResponseEntity<Page<ReviewResponse>> getReviews(
            @Parameter(description = "ID книги", example = "1", required = true)
            @PathVariable Long bookId,
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(reviewService.getReviewsByBookId(bookId, pageable));
    }
    
    
    private Long getCurrentUserId(Authentication authentication) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"))
                .getId();
    }
}

