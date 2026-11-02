package com.example.task_flow.auth.dto;

public record AuthTokensDTO(
	String accessToken,
	String refreshToken
){}