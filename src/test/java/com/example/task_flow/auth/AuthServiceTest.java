package com.example.task_flow.auth;

import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import com.example.task_flow.auth.dto.AuthTokensDto;
import com.example.task_flow.common.exception.ConflictException;
import com.example.task_flow.common.exception.TokenValidationException;
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

    @Nested
    @DisplayName("Регистрация (register)")
    class Register {

       private AuthRegisterDto request;
       
        @BeforeEach
        void setUp() {
        request = new AuthRegisterDto("testnick","test@mail.com", "testPass");
       }

        private void givenSuccessfulRegister(){
        when(repository.existsByEmail("test@mail.com")).thenReturn(false);
        when(passwordEncoder.encode("testPass")).thenReturn("$2a$hashed");
        when(repository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);         
            return u;
        });
        }

        @Test
        @DisplayName("Регистрация: ошибка email уже занят в базе данных")
        void register_whenEmailExists_throwsConflict_andDoesNotSave() {
        when(repository.existsByEmail("test@mail.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("conflict");

          verify(repository,never()).save(any());
          verifyNoInteractions(passwordEncoder);
        }

        @Test
        @DisplayName("Регистрация:  email свободен и сохранение пользователя с валидными данными")
        void register_whenEmailFree_andSaveUserValidateParam() {
        givenSuccessfulRegister();

        authService.register(request);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(repository).save(captor.capture());
        User saved = captor.getValue();

        assertThat(saved.getNickname()).isEqualTo("testnick");
        assertThat(saved.getEmail()).isEqualTo("test@mail.com");
        assertThat(saved.getPassword()).isEqualTo("$2a$hashed"); 
        assertThat(saved.getPassword()).isNotEqualTo("testPass");

        verify(passwordEncoder,times(1)).encode("testPass");
        }

        @Test
        @DisplayName("Регистрация: ответ пользователю")
        void register_whenEmailFree_ReturnsResponse() {
        givenSuccessfulRegister();

        AuthRegisterResponse response = authService.register(request);
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.nickname()).isEqualTo("testnick");
        assertThat(response.email()).isEqualTo("test@mail.com");
        }

    }

    @Nested
    @DisplayName("Логин (login)")
    class Login {

       private AuthLoginDto request;
       private User user;

      @BeforeEach
      void setUp() {
        request = new AuthLoginDto("test@mail.com", "testPass");
        user = new User();
        user.setId(1L);
        user.setEmail("test@mail.com");
       }

       private void givenSuccessfulLogin(){
        when(repository.existsByEmail("test@mail.com")).thenReturn(true);
        when(repository.findByEmail("test@mail.com")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(1L, "test@mail.com")).thenReturn("test-access-token");
       }

    @Test
    @DisplayName("Логин: успех → аутентификация вызвана с email и паролем")
    void login_authenticatesWithEmailAndPassword() {
        givenSuccessfulLogin();
        authService.login(request);

        ArgumentCaptor<UsernamePasswordAuthenticationToken> authCaptor = ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(authCaptor.capture());
        assertThat(authCaptor.getValue().getPrincipal()).isEqualTo("test@mail.com");
        assertThat(authCaptor.getValue().getCredentials()).isEqualTo("testPass");
    }

    @Test
    @DisplayName("Логин: успех → возвращает access-токен из jwtService")
    void login_returnAccessTokenJwtService() {
      givenSuccessfulLogin();
      AuthTokensDto response = authService.login(request);

      verify(jwtService).generateAccessToken(1L, "test@mail.com");
      assertThat(response.accessToken()).isEqualTo("test-access-token");
    }


    @Test
    @DisplayName("Логин: успех → возвращает непустой refresh-токен в формате UUID")
    void login_returnValidRefreshToken() {
      givenSuccessfulLogin();

      AuthTokensDto response = authService.login(request);

      assertThat(response.refreshToken()).isNotBlank();
      assertThat(response.refreshToken()).matches("[0-9a-f-]{36}");    
    }

    @Test
    @DisplayName("Логин: успех → сохраняет refresh-токен в Redis с userId")
    void login_succesSavedRefrestokenRedis() {
      givenSuccessfulLogin();

      AuthTokensDto response = authService.login(request);

      ArgumentCaptor<String> refreshCaptor = ArgumentCaptor.forClass(String.class);
      ArgumentCaptor<String> userIdCaptor = ArgumentCaptor.forClass(String.class);

      verify(redisService).saveRefreshToken(refreshCaptor.capture(),userIdCaptor.capture());

      assertThat(refreshCaptor.getValue()).isEqualTo(response.refreshToken());
      assertThat(refreshCaptor.getValue()).matches("[0-9a-f-]{36}");
      assertThat(userIdCaptor.getValue()).isEqualTo("1");
    }

    @Test
    @DisplayName("Логин: успех → пароль не хешируется повторно")
    void login_doesNotUsePasswordEncoder() {
        givenSuccessfulLogin();

        authService.login(request);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("Логин: email не найден → пользователь получит исключение NotFound")
    void login_whenEmailNotFound_throwsNotFound_andDoesNotLogin() {
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
         when(repository.existsByEmail("test@mail.com")).thenReturn(true);

         var request = new AuthLoginDto("test@mail.com", "wrongPass");

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

    @Nested
    @DisplayName("Рефреш (refresh)")
    class Refresh{

        private String refreshToken = "test-refresh-token";
        private String newAccessToken = "new-test-access-token";
        private Long userId = 1L;
        private User user;


        @BeforeEach
        void setUp() {
        user = new User();
        user.setId(userId);
        user.setEmail("test@mail.ru");
       }

        private void givenSuccessfulRefresh(){
        when(redisService.hasRefreshToken(refreshToken)).thenReturn(true);
        when(redisService.getUserIdRefreshToken(refreshToken)).thenReturn(userId);
        when(repository.findById(userId)).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(userId,"test@mail.ru")).thenReturn(newAccessToken);
        when(redisService.isExpiringRefresh(refreshToken,userId)).thenReturn(true);
        }

        @Test
        @DisplayName("Resfresh: ошибка refreshToken отсутствует в redis будет исключение TokenValidationException")
        void refresh_whenRefreshTokenMissingFromRedis() {
         when(redisService.hasRefreshToken(refreshToken)).thenReturn(false);

         assertThatThrownBy(() -> authService.refresh(refreshToken))
            .isInstanceOf(TokenValidationException.class)
            .hasMessageContaining("Невалидный или просроченный токен");

         verify(redisService).hasRefreshToken(refreshToken);
         verify(redisService,never()).getUserIdRefreshToken(any());
         verify(repository,never()).findById(any());
         verify(jwtService,never()).generateAccessToken(any(),any());
         verify(redisService,never()).isExpiringRefresh(any(),anyLong());
        }

        @Test
        @DisplayName("Resfresh: успешное извлечение userId из getUserIdRefreshToken но отсутствует пользователь по такому userId NotFound исключение")
        void refresh_WhenRefreshTokenIsValidAndNotNullUserId_AndNotFoundUser_throwNotFound() {
         when(redisService.hasRefreshToken(refreshToken)).thenReturn(true);
         when(redisService.getUserIdRefreshToken(refreshToken)).thenReturn(userId);
         when(repository.findById(userId)).thenReturn(Optional.empty());

         assertThatThrownBy(() -> authService.refresh(refreshToken))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("user not found");

         verify(repository).findById(userId);
         verify(jwtService,never()).generateAccessToken(any(),any());
         verify(redisService,never()).isExpiringRefresh(any(),anyLong());
        }

        @Test
        @DisplayName("Resfresh: успешное возвращение пользователю accesstoken, и новый refresh токенов")
        void refresh_WhenRefreshTokenIsValidAndNotNullUserId_ReturnsAccessTokenAndRefresh() {
         givenSuccessfulRefresh();
         AuthTokensDto result = authService.refresh(refreshToken);

         assertThat(result.accessToken()).isEqualTo(newAccessToken);
         assertThat(result.refreshToken()).isNotBlank();
         assertThat(result.refreshToken()).matches("[0-9a-f-]{36}");

         verify(redisService).deleteRefreshToken(refreshToken);
         verify(redisService).isExpiringRefresh(refreshToken,userId);

         ArgumentCaptor<String> newRefreshTokenArg = ArgumentCaptor.forClass(String.class);
         ArgumentCaptor<String> saveUserIdRefreshToken = ArgumentCaptor.forClass(String.class);
         verify(redisService).saveRefreshToken(newRefreshTokenArg.capture(),saveUserIdRefreshToken.capture());
         assertThat(newRefreshTokenArg.getValue()).matches("[0-9a-f-]{36}");
         assertThat(saveUserIdRefreshToken.getValue()).isEqualTo(String.valueOf(userId));
        }

        @Test
        @DisplayName("Resfresh: успешное возвращение пользователю accesstoken, и пустой refresh потомучто старый еще валиден")
        void refresh_WhenRefreshTokenIsValidAndNotNullUserId_ReturnsAccessToken() {
         when(redisService.hasRefreshToken(refreshToken)).thenReturn(true);
         when(redisService.getUserIdRefreshToken(refreshToken)).thenReturn(userId);
         when(repository.findById(userId)).thenReturn(Optional.of(user));
         when(jwtService.generateAccessToken(userId,"test@mail.ru")).thenReturn(newAccessToken);
         when(redisService.isExpiringRefresh(refreshToken,userId)).thenReturn(false);

         AuthTokensDto result = authService.refresh(refreshToken);

         assertThat(result.accessToken()).isEqualTo(newAccessToken);
         assertThat(result.refreshToken()).isNull();

         verify(redisService,never()).deleteRefreshToken(any());
         verify(redisService,never()).saveRefreshToken(any(), any());
         verify(jwtService).generateAccessToken(userId,"test@mail.ru");
       
        }
    }

    @Nested
    @DisplayName("(logout)")
    class Logout{

        private String refreshToken = "test-refresh-token";
        private String testJti = UUID.randomUUID().toString();
        private Date testExpiration = Date.from(Instant.now().plus(15, ChronoUnit.MINUTES));

        @Test
        @DisplayName("Logout: ошибка истек refreshToken в redis исключение TokenValidationException")
        void logout_whenRefreshTokenIsNotExpired_returnsThrow() {
         when(redisService.hasRefreshToken(refreshToken)).thenReturn(false);

         assertThatThrownBy(() -> authService.logout(testJti,testExpiration,refreshToken))
            .isInstanceOf(TokenValidationException.class)
            .hasMessageContaining("Невалидный или просроченный токен");

         verify(redisService,times(1)).hasRefreshToken(refreshToken);
        
         verify(redisService,never()).deleteRefreshToken(any());
         verify(redisService,never()).saveBlackListAccess(any(),any());
        }

        @Test
        @DisplayName("Logout: ttl > 0 → сохраняется в blacklist")
         void logout_whenTtlPositive_savesBlackList() {
         when(redisService.hasRefreshToken(refreshToken)).thenReturn(true);
         
         Date expiration = Date.from(Instant.now().plus(15, ChronoUnit.MINUTES));

         authService.logout(testJti, expiration, refreshToken);

         verify(redisService).deleteRefreshToken(refreshToken);
         verify(redisService).saveBlackListAccess(eq(testJti), longThat(ttl -> ttl > 0));
         }

        @Test
        @DisplayName("Logout: ttl <= 0 → blacklist не сохраняется")
         void logout_whenTtlNegative_doesNotSaveBlackList() {
         when(redisService.hasRefreshToken(refreshToken)).thenReturn(true);
         
         Date expiration = Date.from(Instant.now().minus(1, ChronoUnit.MINUTES));

         authService.logout(testJti, expiration, refreshToken);

         verify(redisService).deleteRefreshToken(refreshToken);
         verify(redisService, never()).saveBlackListAccess(any(), any());
        }      

    }
}

         

         
         