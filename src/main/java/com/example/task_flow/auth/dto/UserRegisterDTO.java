package com.example.task_flow.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record UserRegisterDTO(
    @NotBlank(message = "Никнейм не может быть пустым")
    @Size(min = 3, max = 20, message = "Никнейм должен быть от 3 до 20 символов")
    String nickname,

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный формат email")
    @Size(max = 100, message = "Email слишком длинный")
    String email,

    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 8, max = 25, message = "Пароль должен быть от 8 до 25 символов")
    String password
) {}
// public class UserRegisterDTO{

// 	@NotBlank(message = "Никнейм не может быть пустым")
// 	@Size(min = 3 , max = 20 , message = "Никнейм должен быть от 3 до 20 символов")
// 	private String nickname;

// 	@NotBlank(message = "Email не может быть пустым")
// 	@Email(message = "Некорректный формат email")
// 	@Size(max = 100, message = "Email слишком длинный")
// 	private String email;

// 	@NotBlank(message = "Пароль не может быть пустым")
// 	@Size(min = 8 , max = 25, message = "Пароль должен быть от 8 до 25 символов")
// 	private String password;

// 	public String getEmail(){
// 		return email;
// 	}
// }