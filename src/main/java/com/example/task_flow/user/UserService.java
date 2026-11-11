package com.example.task_flow.user;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;

@Service 
public class UserService{

	private final UserRepository userRepository;

	public UserService(UserRepository userRepository){
		this.userRepository = userRepository;
	}

	public UserDomain findUserEmail(String email){
		User user = userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("User not found"));
		return new UserDomain(user.getId(),user.getNickname(),user.getEmail(),user.getCreatedAt());
	}
	public UserDomain findUserById(Long userId){
		User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
		return new UserDomain(user.getId(),user.getNickname(),user.getEmail(),user.getCreatedAt());
	}

}