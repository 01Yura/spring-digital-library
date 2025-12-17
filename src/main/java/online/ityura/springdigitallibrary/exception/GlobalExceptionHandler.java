package online.ityura.springdigitallibrary.exception;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import online.ityura.springdigitallibrary.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Hidden
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(
            ResponseStatusException ex, 
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String message = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
        String errorCode = determineErrorCode(status, message);
        
        ErrorResponse response = ErrorResponse.builder()
                .status(status.value())
                .error(errorCode)
                .message(message)
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex, 
            HttpServletRequest request) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Internal server error";
        
        ErrorResponse response = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("INTERNAL_SERVER_ERROR")
                .message(message)
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, 
            HttpServletRequest request) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });
        
        ErrorResponse response = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("VALIDATION_ERROR")
                .message("Validation failed")
                .fieldErrors(fieldErrors)
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }
    
    private String determineErrorCode(HttpStatus status, String message) {
        if (message == null) {
            message = "";
        }
        String lowerMessage = message.toLowerCase();
        
        return switch (status) {
            case BAD_REQUEST -> "VALIDATION_ERROR";
            case UNAUTHORIZED -> "UNAUTHORIZED";
            case FORBIDDEN -> "ACCESS_DENIED";
            case NOT_FOUND -> {
                if (lowerMessage.contains("book")) {
                    yield "BOOK_NOT_FOUND";
                } else if (lowerMessage.contains("author")) {
                    yield "AUTHOR_NOT_FOUND";
                } else if (lowerMessage.contains("user")) {
                    yield "USER_NOT_FOUND";
                } else if (lowerMessage.contains("rating")) {
                    yield "RATING_NOT_FOUND";
                } else if (lowerMessage.contains("review")) {
                    yield "REVIEW_NOT_FOUND";
                }
                yield "NOT_FOUND";
            }
            case CONFLICT -> {
                if (lowerMessage.contains("book") && lowerMessage.contains("already exists")) {
                    yield "BOOK_ALREADY_EXISTS";
                } else if (lowerMessage.contains("email") && lowerMessage.contains("already exists")) {
                    yield "EMAIL_ALREADY_EXISTS";
                } else if (lowerMessage.contains("rating") && lowerMessage.contains("already exists")) {
                    yield "RATING_ALREADY_EXISTS";
                } else if (lowerMessage.contains("review") && lowerMessage.contains("already exists")) {
                    yield "REVIEW_ALREADY_EXISTS";
                } else if (lowerMessage.contains("book") && (lowerMessage.contains("has reviews") || lowerMessage.contains("it has reviews"))) {
                    yield "BOOK_HAS_REVIEWS";
                }
                yield "CONFLICT";
            }
            case INTERNAL_SERVER_ERROR -> "INTERNAL_SERVER_ERROR";
            default -> status.getReasonPhrase().replace(" ", "_").toUpperCase();
        };
    }
}

