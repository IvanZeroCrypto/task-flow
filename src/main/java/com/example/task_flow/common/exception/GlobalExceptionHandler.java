package com.example.task_flow.common.exception;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;



@ControllerAdvice 
public class GlobalExceptionHandler{
	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(exception = {Exception.class , RuntimeException.class})
	public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception e){
		log.error("Handle exception",e);

		var errorDto = new ErrorResponseDTO(
          "Internal server error",
          e.getMessage(),
          LocalDateTime.now()
		);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDto);
	}

    @ExceptionHandler(RequestRejectedException.class)
    public ResponseEntity<String> handleRequestRejected(RequestRejectedException ex) {
        log.error("Некорректный URL: проверьте, что все обязательные сегменты пути заполнены");
        return ResponseEntity.badRequest()
                .body("Некорректный URL: проверьте, что все обязательные сегменты пути заполнены (например, отсутствует ID рабочей области).");
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponseDTO> handleDataAccessException(DataAccessException e){

        var errorDto = new ErrorResponseDTO(
          "Ошибка при работе с базой данных, попробуйте позже",
          e.getMessage(),
          LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDto);
    }

     @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleConstraintViolation(ConstraintViolationException ex) {
        
        String errorMessage = ex.getConstraintViolations()
                .stream()
                .map(violation -> violation.getMessage())
                .findFirst()
                .orElse("Некорректный параметр запроса");
        var errorDto = new ErrorResponseDTO(
          "Ошибка невалидные данные:",
          ex.getMessage(),
          LocalDateTime.now()
        );
     
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponseDTO> handleUnauthorized(UnauthorizedException e){
        log.error("handleUnauthorized",e);

        var errorDto = new ErrorResponseDTO(
          "Пользватель не авторизован",
          e.getMessage(),
          LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorDto);
    }

	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<ErrorResponseDTO> handleConflictException(ConflictException e){
	    var errorDto = new ErrorResponseDTO(
	    	"is conflict",
	    	e.getMessage(),
	    	LocalDateTime.now()
	  	    );
	    return ResponseEntity.status(HttpStatus.CONFLICT).body(errorDto);
	}

    @ExceptionHandler(TokenValidationException.class)
    public ResponseEntity<ErrorResponseDTO> handleTokenValidation(TokenValidationException e){
        log.error("Handle refresh invalid",e);

        var errorDto = new ErrorResponseDTO(
            "refresh token error",
            e.getMessage(),
            LocalDateTime.now()
            );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorDto);
    }

	@ExceptionHandler(EntityNotFoundException.class)
	public ResponseEntity<ErrorResponseDTO> handleEntityNotFound(EntityNotFoundException e){
		log.error("Handle EntityNotFoundException",e);
		var errorDto = new ErrorResponseDTO(
          "Entity not found",
          e.getMessage(),
          LocalDateTime.now()
		);
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDto);
	}
	@ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<ErrorResponseDTO> handleBadCredentials(Exception e) {
        log.error("Handle EntityNotFoundException",e);
		var errorDto = new ErrorResponseDTO(
          "Entity not found",
          e.getMessage(),
          LocalDateTime.now()
		);
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDto);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleExceptionForbidden(Exception e) {
        log.error("handleExceptionForbidden",e);
        var errorDto = new ErrorResponseDTO(
          "Доступ закрыт",
          e.getMessage(),
          LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorDto);
    }

   @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
    
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(
        MethodArgumentTypeMismatchException ex) {
    String fieldName = ex.getName();
    Object value = ex.getValue();
    String message = String.format(
        "Параметр '%s' имеет неверный формат: '%s' не является допустимым числом", 
        fieldName, value
    );
    return ResponseEntity.badRequest().body(Map.of(
        "status", 400,
        "error", "Bad Request",
        "message", message
    ));
}


@ExceptionHandler(HttpMessageNotReadableException.class)
public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", LocalDateTime.now().toString());
    body.put("status", HttpStatus.BAD_REQUEST.value()); // 400
    body.put("error", "Malformed JSON");
    body.put("message", "Некорректный синтаксис JSON запроса. Проверьте закрывающие скобки, запятые и кавычки.");
  
    return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(body);
}
}

