package com.example.task_flow.security;

import org.springframework.security.core.context.SecurityContextHolder;

import com.example.task_flow.common.exception.AccessDeniedException;

public final class SecurityUtils{

	private SecurityUtils(){}

	private static CustomUserDetails getRequiredUser() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
        return userDetails;
    }
    throw new AccessDeniedException("Не авторизован");
   }

   public static Long getRequiredUserId() {
        return getRequiredUser().getUserId();
   }

   

   public static String getRequiredUserEmail() {
        return getRequiredUser().getEmail();
    }


}