package com.example.task_flow.security;

import java.util.Collections;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.task_flow.user.User;
import com.example.task_flow.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String email)  {
      
        User user = repository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

      
        return org.springframework.security.core.userdetails.User
            .withUsername(user.getEmail())
            .password(user.getPassword()) 
            .authorities(Collections.emptyList()) 
            .build();
    }
}