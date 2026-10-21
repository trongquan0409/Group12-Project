package vn.edu.auction.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> badRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of(
          "timestamp", Instant.now().toString(), "status", 400, "error", ex.getMessage()));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> internal(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
          "timestamp", Instant.now().toString(), "status", 500, "error", "Lỗi máy chủ"));
    }
}
