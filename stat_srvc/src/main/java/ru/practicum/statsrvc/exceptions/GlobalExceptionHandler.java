package ru.practicum.statsrvc.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Непредвиденная ошибка на сервере [{}] : [{}]: {}",
                request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);

        // Возвращаем клиенту безопасный ответ без деталей реализации
        return new ResponseEntity<>(
                new ErrorResponse(),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            org.springframework.web.bind.MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String message = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.warn("Ошибка валидации [{}] : [{}]: {}",
                request.getMethod(), request.getRequestURI(), message);

        return new ResponseEntity<>(
                new ErrorResponse(),
                HttpStatus.BAD_REQUEST
        );
    }
}