package pl.srozga.gluqalc_api.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import pl.srozga.gluqalc_api.dto.internal.ApiError;
import pl.srozga.gluqalc_api.exception.ApplicationAuthenticationException;
import pl.srozga.gluqalc_api.exception.ConflictException;
import pl.srozga.gluqalc_api.exception.NotFoundException;
import pl.srozga.gluqalc_api.exception.TokenAuthenticationException;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@NullMarked
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnknownException(Exception e) {
        log.error("Unhandled exception occurred: ", e);
        return handleException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgumentException(IllegalArgumentException e) {
        return handleException(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> errors = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Invalid value",
                        (existing, replacement) -> existing + "; " + replacement
                ));
        return handleException(HttpStatus.BAD_REQUEST, errors);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String message = String.format("Parameter '%s' should be of type '%s', received: '%s'",
                e.getName(), Objects.requireNonNull(e.getRequiredType()).getSimpleName(), e.getValue());
        return handleException(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotAllowedException(HttpRequestMethodNotSupportedException e) {
        String supportedMethods = Objects.requireNonNull(e.getSupportedHttpMethods())
                .stream()
                .map(Object::toString)
                .collect(Collectors.joining(", "));

        String message = String.format("Method '%s' not allowed for this endpoint. Supported methods: %s",
                e.getMethod(), supportedMethods);

        return handleException(HttpStatus.METHOD_NOT_ALLOWED, message);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNotFoundException() {
        return handleException(HttpStatus.NOT_FOUND, "Resource not found");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleMessageNotReadableException() {
        return handleException(HttpStatus.BAD_REQUEST, "Malformed JSON request");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDeniedException() {
        return handleException(HttpStatus.FORBIDDEN, "Access denied");
    }

    @ExceptionHandler(ApplicationAuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthenticationException(ApplicationAuthenticationException e) {
        return handleException(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflictException(ConflictException e) {
        return handleException(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(TokenAuthenticationException.class)
    public ResponseEntity<ApiError> handleTokenAuthenticationException(TokenAuthenticationException e) {
        return handleException(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleCustomNotFoundException(NotFoundException e) {
        return handleException(HttpStatus.NOT_FOUND, e.getMessage());
    }

    private ResponseEntity<ApiError> handleException(HttpStatus status, Object message) {
        ApiError error = new ApiError(status.value(), message);
        return ResponseEntity.status(status).body(error);
    }
}
