package com.example.task_flow.workspaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record WorkSpaceRequestDto(
	@NotBlank (message = "Имя пространства не может быть пустым")
    @Size (min = 3, max = 100, message = "Длинна не может быть меньше 3 и больше 100")
    @Pattern(
    regexp = "^[a-zA-Zа-яА-ЯёЁ]+$", 
    message = "Имя пространства должно содержать только буквы")
	String name

){}