package com.example.task_flow.common.exception;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;



@ControllerAdvice 
public class GlobalExceptionHandler{
	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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
}