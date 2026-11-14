package com.example.task_flow.tasks.dto;

import jakarta.validation.constraints.Size;

public record TaskPatchDto(

	@Size(min = 1, max = 255, message = "Title не должен быть пустым")
    String title,
    
    Boolean done
){}