package online.ityura.springdigitallibrary.controller;

import online.ityura.springdigitallibrary.dto.response.BookResponse;
import online.ityura.springdigitallibrary.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/books")
@Tag(name = "Книги", description = "API для работы с книгами (просмотр каталога)")
@SecurityRequirement(name = "Bearer Authentication")
public class BookController {
    
    @Autowired
    private BookService bookService;
    
    @Operation(
            summary = "Получить список книг",
            description = "Возвращает пагинированный список всех книг с возможностью сортировки. " +
                    "Требуется аутентификация (роли USER или ADMIN)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Список книг успешно получен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @GetMapping
    public ResponseEntity<Page<BookResponse>> getAllBooks(
            @Parameter(description = "Параметры пагинации (page, size, sort)", example = "page=0&size=20&sort=title,asc")
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(bookService.getAllBooks(pageable));
    }
    
    @Operation(
            summary = "Получить детальную информацию о книге",
            description = "Возвращает полную информацию о книге по её ID, включая автора, рейтинг и наличие файла."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Информация о книге получена",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Книга не найдена"),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @GetMapping("/{bookId}")
    public ResponseEntity<BookResponse> getBookById(
            @Parameter(description = "ID книги", example = "1", required = true)
            @PathVariable Long bookId) {
        return ResponseEntity.ok(bookService.getBookById(bookId));
    }
}

