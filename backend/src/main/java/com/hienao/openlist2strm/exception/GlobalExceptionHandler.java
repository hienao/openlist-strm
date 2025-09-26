package com.hienao.openlist2strm.exception;

import com.hienao.openlist2strm.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
  @ExceptionHandler(value = {BusinessException.class})
  public ResponseEntity<ApiResponse<Void>> handleBusinessException(
      BusinessException ex, WebRequest request) {
    log.error("Business Error Handled  ===> ", ex);
    ApiResponse<Void> response = ApiResponse.error(500, ex.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }

  @SuppressWarnings("NullableProblems")
  @Override
  @Nullable public ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    log.error("MethodArgumentNotValidException Handled  ===> ", ex);
    
    // 提取第一个字段错误
    FieldError firstFieldError = ex.getBindingResult().getFieldErrors().get(0);
    String errorMessage = firstFieldError.getDefaultMessage();
    
    ApiResponse<Void> response = ApiResponse.error(status.value(), errorMessage);
    return ResponseEntity.status(status).body(response);
  }

  @ExceptionHandler(value = {RequestRejectedException.class})
  public ResponseEntity<ApiResponse<Void>> handleRequestRejectedException(
      RequestRejectedException ex, WebRequest request) {
    log.error("RequestRejectedException Handled  ===> ", ex);
    ApiResponse<Void> response = ApiResponse.error(400, ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(value = {AccessDeniedException.class})
  public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex) {
    throw ex;
  }

  @ExceptionHandler(value = {Throwable.class})
  public ResponseEntity<ApiResponse<Void>> handleException(Throwable ex, WebRequest request) {
    log.error("System Error Handled  ===> ", ex);
    ApiResponse<Void> response = ApiResponse.error(500, "系统错误");
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }
}
