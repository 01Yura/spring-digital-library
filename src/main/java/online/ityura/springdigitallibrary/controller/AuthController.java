package online.ityura.springdigitallibrary.controller;

import online.ityura.springdigitallibrary.dto.request.LoginRequest;
import online.ityura.springdigitallibrary.dto.request.RefreshTokenRequest;
import online.ityura.springdigitallibrary.dto.request.RegisterRequest;
import online.ityura.springdigitallibrary.dto.response.AuthResponse;
import online.ityura.springdigitallibrary.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Аутентификация", description = "API для регистрации и входа в систему")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @Operation(
            summary = "Регистрация нового пользователя",
            description = "Создает нового пользователя с ролью USER. Возвращает access токен (5 минут) и refresh токен (24 часа) для дальнейшей аутентификации."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Пользователь успешно зарегистрирован",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(value = "{\"token\":\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\",\"type\":\"Bearer\",\"refreshToken\":\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\",\"userId\":1,\"email\":\"user@example.com\",\"role\":\"USER\"}")
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Неверный формат данных"),
            @ApiResponse(responseCode = "409", description = "Email уже существует")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @Operation(
            summary = "Вход в систему",
            description = "Аутентифицирует пользователя по email и паролю. Возвращает access токен (5 минут) и refresh токен (24 часа)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешный вход",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Неверные учетные данные"),
            @ApiResponse(responseCode = "400", description = "Неверный формат данных")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
            summary = "Обновить access токен",
            description = "Обновляет access токен используя refresh токен. Возвращает новую пару access и refresh токенов. " +
                    "Refresh токен действителен 24 часа."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Токены успешно обновлены",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Неверный формат данных"),
            @ApiResponse(responseCode = "401", description = "Refresh токен недействителен или истек")
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }
}

