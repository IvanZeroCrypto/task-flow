package com.example.task_flow.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.task_flow.auth.dto.AuthLoginResponse;
import com.example.task_flow.auth.dto.AuthRegisterResponse;
import com.example.task_flow.auth.dto.UserLoginDTO;
import com.example.task_flow.auth.dto.UserRegisterDTO;
import com.example.task_flow.common.exception.EmailAlreadyExistsException;

import jakarta.persistence.EntityNotFoundException;

@Service 
public class AuthService{
	private  final AuthRepository repository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;

	public AuthService(AuthRepository repository,PasswordEncoder passwordEncoder,AuthenticationManager authenticationManager){
		this.repository = repository;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
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

	public AuthLoginResponse login(UserLoginDTO request){
		if(!repository.existsByEmail(request.email())){
			throw new EntityNotFoundException("email not found in database");
		}

		authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
		return new AuthLoginResponse("access-token-1234","refresh-token-5678");
	}

}