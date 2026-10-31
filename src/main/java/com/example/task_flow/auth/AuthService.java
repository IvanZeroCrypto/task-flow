package com.example.task_flow.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.task_flow.auth.dto.AuthRegisterResponse;
import com.example.task_flow.auth.dto.UserRegisterDTO;
import com.example.task_flow.common.exception.EmailAlreadyExistsException;

@Service 
public class AuthService{
	private  final AuthRepository repository;
	private final PasswordEncoder passwordEncoder;

	public AuthService(AuthRepository repository,PasswordEncoder passwordEncoder){
		this.repository = repository;
		this.passwordEncoder = passwordEncoder;
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

}