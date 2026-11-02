package com.example.task_flow.auth;

import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.task_flow.auth.dto.AuthRegisterResponse;
import com.example.task_flow.auth.dto.AuthTokensDTO;
import com.example.task_flow.auth.dto.UserLoginDTO;
import com.example.task_flow.auth.dto.UserRegisterDTO;
import com.example.task_flow.common.exception.EmailAlreadyExistsException;
import com.example.task_flow.common.exception.TokenValidationException;
import com.example.task_flow.infrastructure.redis.RedisService;
import com.example.task_flow.security.JwtService;

import jakarta.persistence.EntityNotFoundException;

@Service 
public class AuthService{
	private  final AuthRepository repository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final RedisService redisService;

	public AuthService(AuthRepository repository,
		PasswordEncoder passwordEncoder,
		AuthenticationManager authenticationManager,
		JwtService jwtService,
    	RedisService redisService
){
		this.repository = repository;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
		this.redisService = redisService;
	}

	public AuthRegisterResponse register(UserRegisterDTO request){
		if(repository.existsByEmail(request.email())){
			throw new EmailAlreadyExistsException(request.email());
		}

		User user = new User();
		user.setNickname(request.nickname());
		user.setEmail(request.email());
		user.setPassword(passwordEncoder.encode(request.password()));

		User savedUser = repository.save(user);

		return new AuthRegisterResponse(savedUser.getId(),savedUser.getNickname(),savedUser.getEmail());
	}

	public AuthTokensDTO login(UserLoginDTO request){
		if(!repository.existsByEmail(request.email())){
			throw new EntityNotFoundException("email not found in database");
		}

		authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        User user = repository.findByEmail(request.email())
            .orElseThrow(() -> new EntityNotFoundException("user not found by email = " + request.email()));

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = UUID.randomUUID().toString();

        redisService.saveRefreshToken(refreshToken,String.valueOf(user.getId()));

		return new AuthTokensDTO(accessToken,refreshToken);
	}

	public AuthTokensDTO refresh(String refreshToken){
		if(!redisService.hasRefreshToken(refreshToken)){
			throw new TokenValidationException("Невалидный или просроченный токен залогиньтесь заново");
		}

		Long userId = redisService.getUserIdRefreshToken(refreshToken);

		User user = repository.findById(userId).orElseThrow(() -> new  EntityNotFoundException("user not found"));

		String newAccessToken = jwtService.generateAccessToken(user.getId(), user.getEmail());

		if(redisService.isExpiringRefresh(refreshToken, 1L)){
			redisService.deleteRefreshToken(refreshToken);

			String newRefreshToken = UUID.randomUUID().toString();

			redisService.saveRefreshToken(newRefreshToken, String.valueOf(userId));

			return new AuthTokensDTO(newAccessToken,newRefreshToken);
		}
		return new AuthTokensDTO(newAccessToken,null);
	}

	}
