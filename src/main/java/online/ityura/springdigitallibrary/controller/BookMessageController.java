package online.ityura.springdigitallibrary.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import online.ityura.springdigitallibrary.dto.request.MessageRequest;
import online.ityura.springdigitallibrary.dto.response.ErrorResponse;
import online.ityura.springdigitallibrary.dto.response.MessageResponse;
import online.ityura.springdigitallibrary.service.BookMessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/books")
@Tag(name = "Сообщения читателям", description = "API для отправки сообщений читателям о книгах")
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor
public class BookMessageController {
    
    private final BookMessageService bookMessageService;
    
    @Operation(
            summary = "Отправить вопрос о книге рандомному читателю",
            description = "Отправляет сообщение (вопрос) рандомному читателю, который уже прочитал книгу. " +
                    "Читатель отвечает. Однако сообщение приходит не сразу, так как ему надо время на написание ответа. Подождите 20-30 сек, будьте терпиливы, человек старается, пишет. " +
                    "Доступно без авторизации."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Сообщение успешно отправлено и получен ответ от читателя",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Книга не найдена",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации запроса",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Ошибка при обращении к OpenAI API",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @SecurityRequirements
    @PostMapping("/{bookId}/message")
    public ResponseEntity<MessageResponse> sendMessageToReader(
            @Parameter(description = "ID книги", example = "1", required = true)
            @PathVariable Long bookId,
            @Valid @RequestBody MessageRequest messageRequest) {
        
        String responseText = bookMessageService.sendMessageToReader(bookId, messageRequest.getMessage());
        
        MessageResponse response = MessageResponse.builder()
                .message(responseText)
                .build();
        
        return ResponseEntity.ok(response);
    }
}

