package online.ityura.springdigitallibrary.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import online.ityura.springdigitallibrary.dto.request.CreateBookRequest;
import online.ityura.springdigitallibrary.dto.request.PutBookRequest;
import online.ityura.springdigitallibrary.dto.request.UpdateBookRequest;
import online.ityura.springdigitallibrary.dto.response.BookResponse;
import online.ityura.springdigitallibrary.dto.response.ErrorResponse;
import online.ityura.springdigitallibrary.dto.response.MessageResponse;
import online.ityura.springdigitallibrary.service.AdminBookService;
import online.ityura.springdigitallibrary.service.BookImageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/books")
@Tag(name = "Администрирование книг", description = "API для управления книгами (требуется роль ADMIN)")
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor
public class AdminBookController {

    private final AdminBookService adminBookService;

    private final BookImageService bookImageService;

    @Operation(
            summary = "Создать новую книгу",
            description = "Создает новую книгу в каталоге. Автор будет создан автоматически, если его еще нет. " +
                    "Книга должна быть уникальна по паре (title, author). Требуется роль ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Книга успешно создана",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Неверный формат данных или несуществующий жанр",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Validation error",
                                            value = "{\"status\":400,\"error\":\"VALIDATION_ERROR\",\"message\":\"Validation failed\",\"fieldErrors\":{\"title\":\"Title is required\",\"authorName\":\"Author name is required\",\"publishedYear\":\"Published year must be at least 1000\"},\"timestamp\":\"2025-12-17T13:20:00Z\",\"path\":\"/api/v1/admin/books\"}"
                                    ),
                                    @ExampleObject(
                                            name = "Invalid genre",
                                            value = "{\"status\":400,\"error\":\"INVALID_GENRE\",\"message\":\"Invalid genre: INVALID_GENRE_VALUE. Please use one of the valid genre values.\",\"timestamp\":\"2025-12-17T13:20:00Z\",\"path\":\"/api/v1/admin/books\"}"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Недостаточно прав (требуется роль ADMIN)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":403,\"error\":\"ACCESS_DENIED\",\"message\":\"Insufficient permissions (ADMIN role required)\",\"timestamp\":\"2025-12-17T13:20:00Z\",\"path\":\"/api/v1/admin/books\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Книга с таким названием и автором уже существует",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":409,\"error\":\"BOOK_ALREADY_EXISTS\",\"message\":\"Book with this title and author already exists\",\"timestamp\":\"2025-12-17T13:20:00Z\",\"path\":\"/api/v1/admin/books\"}")
                    )
            )
    })
    @PostMapping
    public ResponseEntity<BookResponse> createBook(@Valid @RequestBody CreateBookRequest request) {
        BookResponse response = adminBookService.createBook(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Hidden // TODO: ВРЕМЕННО СКРЫТО ИЗ SWAGGER
    @Operation(
            summary = "Создать несколько книг одновременно",
            description = "Создает несколько книг в каталоге за один запрос. Передайте массив объектов CreateBookRequest. " +
                    "Авторы будут созданы автоматически, если их еще нет. " +
                    "Каждая книга должна быть уникальна по паре (title, author). " +
                    "Если хотя бы одна книга не может быть создана (конфликт уникальности), вся операция откатывается. " +
                    "Требуется роль ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Все книги успешно созданы",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Неверный формат данных или несуществующий жанр",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Validation error",
                                            value = "{\"status\":400,\"error\":\"VALIDATION_ERROR\",\"message\":\"Validation failed\",\"fieldErrors\":{\"title\":\"Title is required\",\"authorName\":\"Author name is required\",\"publishedYear\":\"Published year must be between 1000-9999\"},\"timestamp\":\"2025-12-17T13:20:00Z\",\"path\":\"/api/v1/admin/books/batch\"}"
                                    ),
                                    @ExampleObject(
                                            name = "Invalid genre",
                                            value = "{\"status\":400,\"error\":\"INVALID_GENRE\",\"message\":\"Invalid genre: INVALID_GENRE_VALUE. Please use one of the valid genre values.\",\"timestamp\":\"2025-12-17T13:20:00Z\",\"path\":\"/api/v1/admin/books/batch\"}"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Недостаточно прав (требуется роль ADMIN)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":403,\"error\":\"ACCESS_DENIED\",\"message\":\"Insufficient permissions (ADMIN role required)\",\"timestamp\":\"2025-12-17T13:20:00Z\",\"path\":\"/api/v1/admin/books/batch\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Одна из книг с таким названием и автором уже существует",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":409,\"error\":\"BOOK_ALREADY_EXISTS\",\"message\":\"Book with this title and author already exists\",\"timestamp\":\"2025-12-17T13:20:00Z\",\"path\":\"/api/v1/admin/books/batch\"}")
                    )
            )
    })
    @PostMapping("/batch")
    public ResponseEntity<List<BookResponse>> createBooks(@Valid @RequestBody List<CreateBookRequest> requests) {
        List<BookResponse> responses = adminBookService.createBooks(requests);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @Operation(
            summary = "Полностью заменить всю инфу о книге",
            description = "Полностью заменяет информацию о существующей книге согласно REST стандартам. " +
                    "Все поля обязательны. Если переданы не все поля, возвращается ошибка 400. " +
                    "PUT заменяет весь ресурс целиком, в отличие от PATCH который делает частичное обновление. " +
                    "При изменении title или author проверяется уникальность комбинации title + author (у разных авторов могут быть книги с одинаковым названием). " +
                    "Требуется роль ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Книга успешно обновлена",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Неверный формат данных",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Validation error",
                                    value = "{\"status\":400,\"error\":\"VALIDATION_ERROR\",\"message\":\"Validation failed\",\"fieldErrors\":{\"title\":\"Title is required\",\"publishedYear\":\"Published year must be between 1000-9999\"},\"timestamp\":\"2025-12-17T13:20:00Z\",\"path\":\"/api/v1/admin/books/1\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Книга не найдена",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":404,\"error\":\"BOOK_NOT_FOUND\",\"message\":\"Book not found with id: 1\",\"timestamp\":\"2025-12-17T13:20:00Z\",\"path\":\"/api/v1/admin/books/1\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Недостаточно прав",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":403,\"error\":\"ACCESS_DENIED\",\"message\":\"Insufficient permissions (ADMIN role required)\",\"timestamp\":\"2025-12-17T13:20:00Z\",\"path\":\"/api/v1/admin/books/1\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Конфликт уникальности",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":409,\"error\":\"BOOK_ALREADY_EXISTS\",\"message\":\"Book with this title and author already exists\",\"timestamp\":\"2025-12-17T13:20:00Z\",\"path\":\"/api/v1/admin/books/1\"}")
                    )
            )
    })
    @PutMapping("/{bookId}")
    public ResponseEntity<BookResponse> updateBook(
            @Parameter(description = "ID книги", example = "1", required = true)
            @PathVariable Long bookId,
            @Valid @RequestBody PutBookRequest request) {
        BookResponse response = adminBookService.updateBook(bookId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Частично обновить информацию о книге",
            description = "Частично обновляет информацию о существующей книге. Все поля опциональны. " +
                    "При изменении title или author проверяется уникальность комбинации title + author (у разных авторов могут быть книги с одинаковым названием). " +
                    "Для обновления изображения используйте PATCH с multipart/form-data. " +
                    "Требуется роль ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Книга успешно обновлена",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Неверный формат данных",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Validation error",
                                    value = "{\"status\":400,\"error\":\"VALIDATION_ERROR\",\"message\":\"Validation failed\",\"fieldErrors\":{\"publishedYear\":\"Published year must be between 1000-9999\"},\"timestamp\":\"2025-12-17T13:20:00Z\",\"path\":\"/api/v1/admin/books/1\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Книга не найдена",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":404,\"error\":\"BOOK_NOT_FOUND\",\"message\":\"Book not found with id: 1\",\"timestamp\":\"2025-12-17T13:20:00Z\",\"path\":\"/api/v1/admin/books/1\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Недостаточно прав",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":403,\"error\":\"ACCESS_DENIED\",\"message\":\"Insufficient permissions (ADMIN role required)\",\"timestamp\":\"2025-12-17T13:20:00Z\",\"path\":\"/api/v1/admin/books/1\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Конфликт уникальности",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":409,\"error\":\"BOOK_ALREADY_EXISTS\",\"message\":\"Book with this title and author already exists\",\"timestamp\":\"2025-12-17T13:20:00Z\",\"path\":\"/api/v1/admin/books/1\"}")
                    )
            )
    })
    @PatchMapping(value = "/{bookId}", consumes = "application/json")
    public ResponseEntity<BookResponse> patchBook(
            @Parameter(description = "ID книги", example = "1", required = true)
            @PathVariable Long bookId,
            @Valid @RequestBody UpdateBookRequest request) {
        BookResponse response = adminBookService.patchBook(bookId, request, null);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Частично обновить информацию о книге с изображением (multipart/form-data)",
            description = "Частично обновляет информацию о существующей книге и/или изображение. " +
                    "Все поля опциональны. Можно обновить только поля, только изображение, или и то и другое. " +
                    "Можно обновить любое поле, включая автора (authorName). " +
                    "При изменении title или author проверяется уникальность комбинации title + author (у разных авторов могут быть книги с одинаковым названием). " +
                    "Изображение должно быть в формате multipart/form-data, размером не более 5MB. " +
                    "Требуется роль ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Книга успешно обновлена",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Неверный формат данных, файл слишком большой или попытка изменить автора",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Validation error",
                                            value = "{\"status\":400,\"error\":\"VALIDATION_ERROR\",\"message\":\"Validation failed\",\"fieldErrors\":{\"publishedYear\":\"Published year must be between 1000-9999\"},\"timestamp\":\"2025-12-17T13:20:00Z\",\"path\":\"/api/v1/admin/books/1\"}"
                                    ),
                                    @ExampleObject(
                                            name = "Author change not allowed",
                                            value = "{\"status\":400,\"error\":\"AUTHOR_CHANGE_NOT_ALLOWED\",\"message\":\"Cannot change author: author modification is not allowed\",\"timestamp\":\"2025-12-17T13:20:00Z\",\"path\":\"/api/v1/admin/books/1\"}"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Книга не найдена",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":404,\"error\":\"BOOK_NOT_FOUND\",\"message\":\"Book not found with id: 1\",\"timestamp\":\"2025-12-17T13:20:00Z\",\"path\":\"/api/v1/admin/books/1\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Недостаточно прав",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":403,\"error\":\"ACCESS_DENIED\",\"message\":\"Insufficient permissions (ADMIN role required)\",\"timestamp\":\"2025-12-17T13:20:00Z\",\"path\":\"/api/v1/admin/books/1\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Конфликт уникальности",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":409,\"error\":\"BOOK_ALREADY_EXISTS\",\"message\":\"Book with this title and author already exists\",\"timestamp\":\"2025-12-17T13:20:00Z\",\"path\":\"/api/v1/admin/books/1\"}")
                    )
            )
    })
    @PatchMapping(value = "/{bookId}", consumes = "multipart/form-data")
    public ResponseEntity<BookResponse> patchBookWithImage(
            @Parameter(description = "ID книги", example = "1", required = true)
            @PathVariable Long bookId,
            @Parameter(description = "Название книги", required = false)
            @RequestParam(value = "title", required = false) String title,
            @Parameter(description = "Полное имя автора (изменение запрещено, будет возвращена ошибка 400)", required = false)
            @RequestParam(value = "authorName", required = false) String authorName,
            @Parameter(description = "Описание книги", required = false)
            @RequestParam(value = "description", required = false) String description,
            @Parameter(description = "Год публикации", required = false)
            @RequestParam(value = "publishedYear", required = false) Integer publishedYear,
            @Parameter(description = "Жанр книги", required = false)
            @RequestParam(value = "genre", required = false) String genre,
            @Parameter(description = "Файл изображения", required = false)
            @RequestParam(value = "image", required = false) MultipartFile image) {

        // Создаем UpdateBookRequest из параметров
        UpdateBookRequest request = new UpdateBookRequest();
        request.setTitle(title);
        request.setAuthorName(authorName);
        request.setDescription(description);
        request.setPublishedYear(publishedYear);
        if (genre != null && !genre.isEmpty()) {
            try {
                request.setGenre(online.ityura.springdigitallibrary.model.Genre.valueOf(genre.toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid genre: " + genre);
            }
        }

        BookResponse response = adminBookService.patchBook(bookId, request, image);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Удалить книгу",
            description = "Удаляет книгу из каталога. Удаление запрещено, если: " +
                    "1) deletion_locked = true (административная блокировка, установленная в БД) - возвращает 403, " +
                    "2) есть связанные отзывы - возвращает 409. Требуется роль ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Книга успешно удалена"),
            @ApiResponse(
                    responseCode = "403",
                    description = "Удаление запрещено администратором (deletion_locked = true в БД)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":403,\"error\":\"ACCESS_DENIED\",\"message\":\"Cannot delete book: deletion is locked\",\"timestamp\":\"2025-12-17T13:20:00Z\",\"path\":\"/api/v1/admin/books/1\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Удаление запрещено (есть связанные отзывы)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":409,\"error\":\"BOOK_HAS_REVIEWS\",\"message\":\"Cannot delete book: it has reviews\",\"timestamp\":\"2025-12-17T13:20:00Z\",\"path\":\"/api/v1/admin/books/1\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Книга не найдена",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":404,\"error\":\"BOOK_NOT_FOUND\",\"message\":\"Book not found with id: 1\",\"timestamp\":\"2025-12-17T13:20:00Z\",\"path\":\"/api/v1/admin/books/1\"}")
                    )
            )
    })
    @DeleteMapping("/{bookId}")
    public ResponseEntity<Void> deleteBook(
            @Parameter(description = "ID книги", example = "1", required = true)
            @PathVariable Long bookId) {
        adminBookService.deleteBook(bookId);
        return ResponseEntity.noContent().build();
    }

    @Hidden // TODO: ВРЕМЕННО СКРЫТО ИЗ SWAGGER
    @Operation(
            summary = "Удалить автора и все его книги",
            description = "Удаляет автора и все его книги из каталога. " +
                    "При удалении также удаляются все связанные данные: файлы книг, отзывы и рейтинги. " +
                    "Удаление запрещено, если у какой-либо книги: " +
                    "1) deletion_locked = true (административная блокировка, установленная в БД) - возвращает 403, " +
                    "2) есть связанные отзывы - возвращает 409. " +
                    "Требуется роль ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Автор и все его книги успешно удалены"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Автор не найден",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":404,\"error\":\"AUTHOR_NOT_FOUND\",\"message\":\"Author not found with id: 1\",\"timestamp\":\"2025-12-17T13:20:00Z\",\"path\":\"/api/v1/admin/books/authors/1\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Удаление запрещено администратором (deletion_locked = true в БД)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":403,\"error\":\"ACCESS_DENIED\",\"message\":\"Cannot delete book with id 1: deletion is locked\",\"timestamp\":\"2025-12-17T13:20:00Z\",\"path\":\"/api/v1/admin/books/authors/1\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Удаление запрещено (книга имеет отзывы)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":409,\"error\":\"BOOK_HAS_REVIEWS\",\"message\":\"Cannot delete book with id 1: it has reviews\",\"timestamp\":\"2025-12-17T13:20:00Z\",\"path\":\"/api/v1/admin/books/authors/1\"}")
                    )
            )
    })
    @DeleteMapping("/authors/{authorId}")
    public ResponseEntity<Void> deleteAuthorAndAllBooks(
            @Parameter(description = "ID автора", example = "1", required = true)
            @PathVariable Long authorId) {
        adminBookService.deleteAuthorAndAllBooks(authorId);
        return ResponseEntity.noContent().build();
    }

    // TODO: ВРЕМЕННО ОТКРЫТО БЕЗ АВТОРИЗАЦИИ - убрать @SecurityRequirements и вернуть требование авторизации
    @Operation(
            summary = "Загрузить изображение для книги (ВРЕМЕННО ДОСТУПНО БЕЗ АВТОРИЗАЦИИ)",
            description = "Загружает изображение для указанной книги. " +
                    "Изображение должно быть в формате multipart/form-data, размером не более 5MB. " +
                    "Имя файла в хранилище будет сгенерировано на основе названия книги (пробелы заменяются на _). "
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Изображение успешно загружено",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class),
                            examples = @ExampleObject(value = "{\"message\":\"Image uploaded successfully\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Неверный формат данных или файл слишком большой",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":400,\"error\":\"VALIDATION_ERROR\",\"message\":\"Invalid file format or file too large\",\"timestamp\":\"2025-12-17T13:20:00Z\",\"path\":\"/api/v1/admin/books/1/image\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Книга не найдена",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":404,\"error\":\"BOOK_NOT_FOUND\",\"message\":\"Book not found with id: 1\",\"timestamp\":\"2025-12-17T13:20:00Z\",\"path\":\"/api/v1/admin/books/1/image\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Недостаточно прав (требуется роль ADMIN)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":403,\"error\":\"ACCESS_DENIED\",\"message\":\"Insufficient permissions (ADMIN role required)\",\"timestamp\":\"2025-12-17T13:20:00Z\",\"path\":\"/api/v1/admin/books/1/image\"}")
                    )
            )
    })
    @SecurityRequirements // TODO: ВРЕМЕННО - убрать эту аннотацию для возврата требования авторизации
    @PostMapping(value = "/{bookId}/image", consumes = "multipart/form-data")
    public ResponseEntity<MessageResponse> uploadBookImage(
            @Parameter(description = "ID книги", example = "1", required = true)
            @PathVariable Long bookId,
            @Parameter(description = "Файл изображения", required = true)
            @RequestParam("file") MultipartFile file) {
        bookImageService.uploadBookImage(bookId, file);
        MessageResponse response = MessageResponse.builder()
                .message("Image uploaded successfully")
                .build();
        return ResponseEntity.ok(response);
    }
}

