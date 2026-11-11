package com.example.task_flow.memberships.dto;

import com.example.task_flow.common.enums.Role;
import jakarta.validation.constraints.NotNull;

public record RequestUpdateWorkspaceMemberRole(
   @NotNull(message = "роль не может быть null")
   Role role
){}