package com.example.task_flow.auth;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.task_flow.auth.dto.AuthLoginDto;
import com.example.task_flow.auth.dto.AuthRegisterDto;
import com.example.task_flow.auth.dto.AuthRegisterResponse;
import com.example.task_flow.common.exception.ConflictException;
import com.example.task_flow.infrastructure.redis.RedisService;
import com.example.task_flow.security.JwtService;
import com.example.task_flow.user.User;
import com.example.task_flow.user.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest{

	@Mock private UserRepository repository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;
    @Mock private RedisService redisService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_whenEmailExists_throwsConflict_andDoesNotSave() {
    	  var request = new AuthRegisterDto("nick", "test@mail.com", "testPass");
    	  when(repository.existsByEmail("test@mail.com")).thenReturn(true);

    	  assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("conflict");

          verify(repository,never()).save(any());
          verifyNoInteractions(passwordEncoder);
    }

    @Test
    void register_whenEmailFree_savesUser_andReturnsResponse() {
        var request = new AuthRegisterDto("nick", "test@mail.com", "testPass");
        when(repository.existsByEmail("test@mail.com")).thenReturn(false);
        when(passwordEncoder.encode("testPass")).thenReturn("$2a$hashed");

        when(repository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(42L);         
            return u;
        });

        AuthRegisterResponse response = authService.register(request);
        assertThat(response.id()).isEqualTo(42L);
        assertThat(response.nickname()).isEqualTo("nick");
        assertThat(response.email()).isEqualTo("test@mail.com");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(repository).save(captor.capture());
        User saved = captor.getValue();

        assertThat(saved.getNickname()).isEqualTo("nick");
        assertThat(saved.getEmail()).isEqualTo("test@mail.com");
        assertThat(saved.getPassword()).isEqualTo("$2a$hashed"); 
        assertThat(saved.getPassword()).isNotEqualTo("testPass"); 

        verify(passwordEncoder, times(1)).encode("testPass");
     }

     @Test
     @DisplayName("Логин: email не найден → пользователь получит исключение NotFound")
     void login_whenEmailNotFound_throwsNotFound_andDoesNotLogin() {
         var request = new AuthLoginDto("test@mail.com", "testPass");
         when(repository.existsByEmail("test@mail.com")).thenReturn(false);

         assertThatThrownBy(() -> authService.login(request)).
         isInstanceOf(EntityNotFoundException.class).
         hasMessageContaining("email not found");

         verifyNoInteractions(authenticationManager);
         verifyNoInteractions(jwtService);
         verifyNoInteractions(redisService);
         verifyNoInteractions(passwordEncoder);

         verify(repository,never()).findByEmail(any());
     }

    @Test
    @DisplayName("Логин: email  найден → но пароль не верный пользователь получит исключение BadCredentials")
    void login_whenEmailExists_throwsBadCredentials() {
         var request = new AuthLoginDto("test@mail.com", "wrongPass");
         when(repository.existsByEmail("test@mail.com")).thenReturn(true);
         when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).
         thenThrow(new BadCredentialsException("Bad credentials"));

         assertThatThrownBy(() -> authService.login(request)).
         isInstanceOf(BadCredentialsException.class);

         verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
         verify(repository,never()).findByEmail(any());

         verifyNoInteractions(jwtService);
         verifyNoInteractions(redisService);
         verifyNoInteractions(passwordEncoder);
     }

 
}