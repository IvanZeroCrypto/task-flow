package com.example.task_flow.auth;

import java.util.Date;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.task_flow.auth.dto.AuthLoginDto;
import com.example.task_flow.auth.dto.AuthRegisterDto;
import com.example.task_flow.auth.dto.AuthRegisterResponse;
import com.example.task_flow.auth.dto.AuthTokensDto;
import com.example.task_flow.common.exception.ConflictException;
import com.example.task_flow.common.exception.TokenValidationException;
import com.example.task_flow.infrastructure.redis.RedisService;
import com.example.task_flow.security.JwtService;
import com.example.task_flow.user.User;
import com.example.task_flow.user.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@Service 
public class AuthService{
	private  final UserRepository repository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final RedisService redisService;

	public AuthService(UserRepository repository,
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

	public AuthRegisterResponse register(AuthRegisterDto request){
		if(repository.existsByEmail(request.email())){
			throw new ConflictException("conflict registration:");
		}

		User user = new User();
		user.setNickname(request.nickname());
		user.setEmail(request.email());
		user.setPassword(passwordEncoder.encode(request.password()));

		User savedUser = repository.save(user);

		return new AuthRegisterResponse(savedUser.getId(),savedUser.getNickname(),savedUser.getEmail());
	}

	public AuthTokensDto login(AuthLoginDto request){
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

		return new AuthTokensDto(accessToken,refreshToken);
	}

	public AuthTokensDto refresh(String refreshToken){
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

			return new AuthTokensDto(newAccessToken,newRefreshToken);
		}
		return new AuthTokensDto(newAccessToken,null);
	}

	public void logout(String jti,Date expiration,String refreshToken){
		if(!redisService.hasRefreshToken(refreshToken)){
			throw new TokenValidationException("Невалидный или просроченный токен залогиньтесь заново");
		}
		redisService.deleteRefreshToken(refreshToken);
		Long ttl = expiration.getTime() - System.currentTimeMillis();

		if(ttl > 0){
			redisService.saveBlackListAccess(jti, ttl);
		}
	}

	}
