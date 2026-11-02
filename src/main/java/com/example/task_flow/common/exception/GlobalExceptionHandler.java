package com.example.task_flow.common.exception;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.persistence.EntityNotFoundException;



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


	@ExceptionHandler(EmailAlreadyExistsException.class)
	public ResponseEntity<ErrorResponseDTO> handleEmailAlreadyExists(EmailAlreadyExistsException e){
	    log.error("Handle exist email",e);

	    var errorDto = new ErrorResponseDTO(
	    	"email if existing",
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

