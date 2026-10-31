package com.example.task_flow.common.exception;

import java.time.LocalDateTime;

public record ErrorResponseDTO(String message,String detailedMessage,LocalDateTime errorTime){}