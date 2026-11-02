package com.example.task_flow.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.task_flow.auth.dto.AuthAccessTokenResponse;
import com.example.task_flow.auth.dto.AuthRegisterResponse;
import com.example.task_flow.auth.dto.AuthTokensDTO;
import com.example.task_flow.auth.dto.UserLoginDTO;
import com.example.task_flow.auth.dto.UserRegisterDTO;
import com.example.task_flow.common.exception.TokenValidationException;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
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

	@PostMapping("/login")
	public ResponseEntity<AuthAccessTokenResponse> login(@RequestBody @Valid UserLoginDTO dto , HttpServletResponse response ){
	  AuthTokensDTO tokens  = authService.login(dto);

	  Cookie refreshTokenCookie = new Cookie("refresToken", tokens.refreshToken());

	  refreshTokenCookie.setHttpOnly(true);
	  refreshTokenCookie.setSecure(true);
	  refreshTokenCookie.setPath("/api/auth");
	  refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60);

	  response.addCookie(refreshTokenCookie);
      return ResponseEntity.ok().body(new AuthAccessTokenResponse(tokens.accessToken()));
	}


    @PostMapping("/refresh")
	public ResponseEntity<AuthAccessTokenResponse> refresh(
		@CookieValue(name = "refresToken",required = false) String refreshToken,
		HttpServletResponse response 
	){

		if(refreshToken == null || refreshToken.isEmpty()){
			throw new  TokenValidationException("Refresh token отсутствует в куках залогиньтесь заново");
		}

		 AuthTokensDTO result  = authService.refresh(refreshToken);

		 if(result.refreshToken() != null){
		 	Cookie refreshTokenCookie = new Cookie("refresToken", result.refreshToken());

	        refreshTokenCookie.setHttpOnly(true);
	        refreshTokenCookie.setSecure(true);
	        refreshTokenCookie.setPath("/api/auth");
	        refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60);

	        response.addCookie(refreshTokenCookie);
		 }
	 
		return ResponseEntity.ok().body(new AuthAccessTokenResponse(result.accessToken()));
	}

    @PostMapping("/logout")
	public ResponseEntity<Void> logout(
		@CookieValue(name = "refresToken",required = false) String refreshToken	,
		HttpServletResponse response 	
	){
		if(refreshToken == null || refreshToken.isEmpty()){
			throw new  TokenValidationException("Refresh token отсутствует в куках залогиньтесь заново");
		}

		 authService.logout(refreshToken);

		 Cookie refreshTokenCookie = new Cookie("refresToken",null);

         refreshTokenCookie.setHttpOnly(true);
	     refreshTokenCookie.setSecure(true);
	     refreshTokenCookie.setPath("/api/auth");
	     refreshTokenCookie.setMaxAge(0);
	     response.addCookie(refreshTokenCookie);

	     return ResponseEntity.status(HttpStatus.OK).build();
	}

}