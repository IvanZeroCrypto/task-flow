package com.example.task_flow.user;

import java.time.LocalDateTime;

public record UserDomain(
	Long id,
	String nickname,
	String email,
	LocalDateTime createdAt
){}