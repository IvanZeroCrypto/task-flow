package com.example.task_flow.tasks.dto;

import java.time.LocalDateTime;

public record TaskDto(
	Long id,
	String title,
	Boolean done,
	Long workspaceId,
	Long authorId,
	LocalDateTime createdAt,
    LocalDateTime updatedAt
){}
