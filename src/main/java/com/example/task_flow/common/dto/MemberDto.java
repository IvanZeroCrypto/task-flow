package com.example.task_flow.common.dto;

import com.example.task_flow.common.enums.Role;

public  record MemberDto(
	Long userId,
	String nickname,
	Role role
){}