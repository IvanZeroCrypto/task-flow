package com.example.task_flow.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.task_flow.auth.dto.AuthRegisterResponse;
import com.example.task_flow.auth.dto.UserRegisterDTO;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class AuthController{

	private static final Logger log = LoggerFactory.getLogger(AuthController.class);
	private final AuthService authService;

	public AuthController(AuthService authService){
		this.authService = authService;
	}

	@PostMapping("/register")
	public ResponseEntity<AuthRegisterResponse> register(@RequestBody @Valid UserRegisterDTO dto){
		log.info("called register controller");
		AuthRegisterResponse result = authService.register(dto);
		return ResponseEntity.ok(result);
	}

}