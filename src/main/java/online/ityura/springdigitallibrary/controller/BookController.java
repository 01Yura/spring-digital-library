package online.ityura.springdigitallibrary.controller;

import online.ityura.springdigitallibrary.dto.response.BookResponse;
import online.ityura.springdigitallibrary.dto.response.MessageResponse;
import online.ityura.springdigitallibrary.service.BookImageService;
import online.ityura.springdigitallibrary.service.BookService;
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
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/books")
@Tag(name = "Книги", description = "API для работы с книгами (доступно без авторизации)")
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor
public class BookController {
    
    private final BookService bookService;
    
    private final BookImageService bookImageService;
    
    @Operation(
            summary = "Получить список книг",
            description = "Возвращает пагинированный список всех книг с возможностью сортировки. " +
                    "Параметры пагинации: `page` (номер страницы, по умолчанию 0), `size` (размер страницы, по умолчанию 10), " +
                    "`sort` (сортировка, по умолчанию `title,asc`). " +
                    "Доступные поля для сортировки: `title` (название), `author.fullName` (автор), `ratingAvg` (рейтинг), " +
                    "`genre` (жанр), `createdAt` (дата добавления), `publishedYear` (год публикации), `updatedAt` (дата обновления). " +
                    "Примеры: `title,asc`, `ratingAvg,desc`, `author.fullName,asc`, `genre,asc`, `createdAt,desc`."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Список книг успешно получен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))
            )
    })
    @SecurityRequirements
    @GetMapping
    public ResponseEntity<Page<BookResponse>> getAllBooks(
            @ParameterObject
            @PageableDefault(size = 10, sort = "title", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(bookService.getAllBooks(pageable));
    }
    
    @Operation(
            summary = "Получить детальную информацию о книге",
            description = "Возвращает полную информацию о книге по её ID, включая автора, рейтинг и наличие файла. " +
                    "Доступно без авторизации."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Информация о книге получена",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Книга не найдена",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class),
                            examples = @ExampleObject(value = "{\"message\":\"Book not found with id: 1\"}")
                    )
            )
    })
    @SecurityRequirements
    @GetMapping("/{bookId}")
    public ResponseEntity<BookResponse> getBookById(
            @Parameter(description = "ID книги", example = "1", required = true)
            @PathVariable Long bookId) {
        return ResponseEntity.ok(bookService.getBookById(bookId));
    }
    
    @Operation(
            summary = "Получить изображение книги",
            description = "Возвращает изображение книги по её ID. " +
                    "Доступно без авторизации."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Изображение успешно получено",
                    content = @Content(mediaType = "image/png, image/jpeg, image/jpg")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Книга или изображение не найдены",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class),
                            examples = @ExampleObject(value = "{\"message\":\"Image not found for book id: 1\"}")
                    )
            )
    })
    @SecurityRequirements
    @GetMapping("/{bookId}/image")
    public ResponseEntity<Resource> getBookImage(
            @Parameter(description = "ID книги", example = "1", required = true)
            @PathVariable Long bookId) {
        Resource resource = bookImageService.getBookImage(bookId);
        
        // Определяем MediaType на основе расширения файла
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        try {
            String imagePath = resource.getURI().getPath();
            String extension = "";
            if (imagePath != null && imagePath.contains(".")) {
                extension = imagePath.substring(imagePath.lastIndexOf(".") + 1).toLowerCase();
            }
            
            switch (extension) {
                case "png":
                    mediaType = MediaType.IMAGE_PNG;
                    break;
                case "jpg":
                case "jpeg":
                    mediaType = MediaType.IMAGE_JPEG;
                    break;
                case "gif":
                    mediaType = MediaType.IMAGE_GIF;
                    break;
                case "webp":
                    mediaType = MediaType.parseMediaType("image/webp");
                    break;
                default:
                    mediaType = MediaType.APPLICATION_OCTET_STREAM;
            }
        } catch (Exception e) {
            // Если не удалось определить тип, используем по умолчанию
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }
        
        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(resource);
    }
}

