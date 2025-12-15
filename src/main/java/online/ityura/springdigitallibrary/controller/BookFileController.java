package online.ityura.springdigitallibrary.controller;

import online.ityura.springdigitallibrary.dto.response.MessageResponse;
import online.ityura.springdigitallibrary.service.BookFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/books/{bookId}")
@Tag(name = "Файлы книг", description = "API для скачивания PDF файлов книг (пока не реализован!)")
@SecurityRequirement(name = "Bearer Authentication")
public class BookFileController {
    
    @Autowired
    private BookFileService bookFileService;
    
    @Operation(
            summary = "Скачать PDF файл книги",
            description = "Скачивает PDF файл указанной книги. Файл возвращается с заголовком Content-Disposition для загрузки."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "PDF файл успешно загружен",
                    content = @Content(mediaType = "application/pdf")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Книга или файл не найдены",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Не авторизован",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))
            )
    })
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadBook(
            @Parameter(description = "ID книги", example = "1", required = true)
            @PathVariable Long bookId) {
        Resource resource = bookFileService.downloadBookFile(bookId);
        String filename = bookFileService.getOriginalFilename(bookId);
        
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replace("+", "%20");
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encodedFilename)
                .body(resource);
    }
}

