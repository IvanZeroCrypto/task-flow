package com.example.task_flow.auth.dto;

public record AuthTokensDto(
	String accessToken,
	String refreshToken
){}