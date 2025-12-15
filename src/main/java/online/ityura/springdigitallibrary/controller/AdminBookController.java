package online.ityura.springdigitallibrary.controller;

import online.ityura.springdigitallibrary.dto.request.CreateBookRequest;
import online.ityura.springdigitallibrary.dto.request.UpdateBookRequest;
import online.ityura.springdigitallibrary.dto.response.BookResponse;
import online.ityura.springdigitallibrary.dto.response.MessageResponse;
import online.ityura.springdigitallibrary.dto.response.ValidationErrorResponse;
import online.ityura.springdigitallibrary.service.AdminBookService;
import online.ityura.springdigitallibrary.service.BookImageService;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/books")
@Tag(name = "Администрирование книг", description = "API для управления книгами (требуется роль ADMIN)")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminBookController {
    
    @Autowired
    private AdminBookService adminBookService;
    
    @Autowired
    private BookImageService bookImageService;
    
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
                    description = "Неверный формат данных",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Недостаточно прав (требуется роль ADMIN)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Книга с таким названием и автором уже существует",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))
            )
    })
    @PostMapping
    public ResponseEntity<BookResponse> createBook(@Valid @RequestBody CreateBookRequest request) {
        BookResponse response = adminBookService.createBook(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
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
                    description = "Неверный формат данных",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Недостаточно прав (требуется роль ADMIN)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Одна из книг с таким названием и автором уже существует",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))
            )
    })
    @PostMapping("/batch")
    public ResponseEntity<List<BookResponse>> createBooks(@Valid @RequestBody List<CreateBookRequest> requests) {
        List<BookResponse> responses = adminBookService.createBooks(requests);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }
    
    @Operation(
            summary = "Обновить информацию о книге",
            description = "Обновляет информацию о существующей книге. Все поля опциональны. " +
                    "При изменении title или author проверяется уникальность. Требуется роль ADMIN."
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
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Книга не найдена",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Недостаточно прав",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Конфликт уникальности",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))
            )
    })
    @PutMapping("/{bookId}")
    public ResponseEntity<BookResponse> updateBook(
            @Parameter(description = "ID книги", example = "1", required = true)
            @PathVariable Long bookId,
            @Valid @RequestBody UpdateBookRequest request) {
        BookResponse response = adminBookService.updateBook(bookId, request);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
            summary = "Удалить книгу",
            description = "Удаляет книгу из каталога. Удаление запрещено, если: " +
                    "1) deletion_locked = true (на уровне БД), 2) есть связанные отзывы. Требуется роль ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Книга успешно удалена"),
            @ApiResponse(
                    responseCode = "403",
                    description = "Удаление запрещено (deletion_locked или есть отзывы)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Книга не найдена",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))
            )
    })
    @DeleteMapping("/{bookId}")
    public ResponseEntity<Void> deleteBook(
            @Parameter(description = "ID книги", example = "1", required = true)
            @PathVariable Long bookId) {
        adminBookService.deleteBook(bookId);
        return ResponseEntity.noContent().build();
    }
    
    @Operation(
            summary = "Удалить автора и все его книги",
            description = "Удаляет автора и все его книги из каталога. " +
                    "При удалении также удаляются все связанные данные: файлы книг, отзывы и рейтинги. " +
                    "Удаление запрещено, если у какой-либо книги: " +
                    "1) deletion_locked = true (на уровне БД), 2) есть связанные отзывы. " +
                    "Требуется роль ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Автор и все его книги успешно удалены"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Автор не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Удаление запрещено (deletion_locked или есть отзывы)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Конфликт (книга имеет отзывы)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))
            )
    })
    @DeleteMapping("/authors/{authorId}")
    public ResponseEntity<Void> deleteAuthorAndAllBooks(
            @Parameter(description = "ID автора", example = "1", required = true)
            @PathVariable Long authorId) {
        adminBookService.deleteAuthorAndAllBooks(authorId);
        return ResponseEntity.noContent().build();
    }
    
    @Operation(
            summary = "Загрузить изображение для книги",
            description = "Загружает изображение для указанной книги. " +
                    "Изображение должно быть в формате multipart/form-data, размером не более 5MB. " +
                    "Имя файла в хранилище будет сгенерировано на основе названия книги (пробелы заменяются на _). " +
                    "Требуется роль ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Изображение успешно загружено",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Неверный формат данных или файл слишком большой",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Книга не найдена",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Недостаточно прав (требуется роль ADMIN)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))
            )
    })
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

