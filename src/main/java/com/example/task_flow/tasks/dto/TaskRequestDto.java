package com.example.task_flow.tasks.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaskRequestDto(
	@NotBlank (message = "title не может быть пустым")
    @Size (min = 3, max = 100, message = "Длинна title  не может быть меньше 3 и больше 100")
	String title
){}