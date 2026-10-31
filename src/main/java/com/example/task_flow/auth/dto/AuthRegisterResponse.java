package com.example.task_flow.auth.dto;

public record AuthRegisterResponse(
	Long id,
	String nickname,
	String email
){}