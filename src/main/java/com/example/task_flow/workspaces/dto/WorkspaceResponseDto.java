package com.example.task_flow.workspaces.dto;

import java.time.LocalDateTime;

public record WorkspaceResponseDto(
	Long id,
    String name,
    Long owner_id,
    LocalDateTime createdAt
){}