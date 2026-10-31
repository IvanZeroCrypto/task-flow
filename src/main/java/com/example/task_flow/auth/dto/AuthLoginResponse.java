package com.example.task_flow.auth.dto;

public record AuthLoginResponse(
	String accessToken,
	String refreshToken
){}