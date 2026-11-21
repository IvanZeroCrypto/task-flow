package com.example.task_flow.auth;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Date;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.task_flow.auth.dto.AuthLoginDto;
import com.example.task_flow.auth.dto.AuthRegisterDto;
import com.example.task_flow.auth.dto.AuthRegisterResponse;
import com.example.task_flow.auth.dto.AuthTokensDto;
import com.example.task_flow.common.exception.ConflictException;
import com.example.task_flow.common.exception.TokenValidationException;
import com.example.task_flow.infrastructure.redis.RedisService;
import com.example.task_flow.security.JwtDetails;
import com.example.task_flow.security.JwtService;

import jakarta.servlet.http.Cookie;
import tools.jackson.databind.json.JsonMapper;


@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private RedisService redisService;

    @Autowired
    private JsonMapper jsonMapper;

    @Nested
    @DisplayName("Register")
    class Register{

     private AuthRegisterDto request;
     private AuthRegisterResponse response;

     @BeforeEach 
     void setUp(){
       request = new AuthRegisterDto("testnick","test@mail.com", "testPass");
       response = new AuthRegisterResponse(1L, "testnick", "test@mail.com");
     }

     @Test 
     @DisplayName("register: валидный запрос → 200 и корректный ответ")
     public  void shouldRegisterSuccessfully()throws Exception{
     	when(authService.register(any(AuthRegisterDto.class))).thenReturn(response);
        
        mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.nickname").value("testnick"))
            .andExpect(jsonPath("$.email").value("test@mail.com"));
        verify(authService).register(eq(request));
     }

     @Test 
     @DisplayName("register: пустой nickname → 400")
     public  void register_blankNickname_returnsBadRequest()throws Exception{
        AuthRegisterDto request = new AuthRegisterDto("", "test@mail.com", "testPass123");

        mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
            
        verifyNoInteractions(authService);
     }

     @Test 
     @DisplayName("register: nickname < 3 символов → 400")
     public  void register_shortNickname_returnsBadRequest()throws Exception{
        AuthRegisterDto request = new AuthRegisterDto("ni", "test@mail.com", "testPass123");

        mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
            
        verifyNoInteractions(authService);
     }

     @Test 
     @DisplayName("register: nickname > 20 символов → 400")
     public  void register_longNickname_returnsBadRequest()throws Exception{
        AuthRegisterDto request = new AuthRegisterDto("nicknamenicknamenickname", "test@mail.com", "testPass123");

        mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
            
        verifyNoInteractions(authService);
     }

     @Test 
     @DisplayName("register: невалидный email → 400")
     public  void register_inValidEmail_returnsBadRequest()throws Exception{
        AuthRegisterDto request = new AuthRegisterDto("nickname", "test.-mail.com", "testPass123");

        mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
            
        verifyNoInteractions(authService);
     }
     @Test 
     @DisplayName("register: password < 8 символов → 400")
     public  void register_shortPassword_returnsBadRequest()throws Exception{
        AuthRegisterDto request = new AuthRegisterDto("nickname", "test.-mail.com", "testp");

        mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
            
        verifyNoInteractions(authService);
     }

     @Test 
     @DisplayName("register: password > 25 символов → 400")
     public  void register_longPassword_returnsBadRequest()throws Exception{
        AuthRegisterDto request = new AuthRegisterDto("nickname", "test.-mail.com", "testpasswordtestpassword");

        mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        verifyNoInteractions(authService);
     }
     

     @Test 
     @DisplayName("register: битый JSON → 400")
     public  void register_invalidFromatJson()throws Exception{
        mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{ \"nickname\": }"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Malformed JSON"))
            .andExpect(jsonPath("$.message").exists());
            
        verifyNoInteractions(authService);
     }

     @Test 
     @DisplayName("register: ошибка email занят исключение ConflictException")
     public  void register_emailNotFree_returnThrowsConflict()throws Exception{
        when(authService.register(any(AuthRegisterDto.class))).thenThrow(new ConflictException("conflict registration"));
        mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
            
        verify(authService).register(eq(request));
     }
    }

    @Nested
    @DisplayName("Login")
    class Login{

     private AuthLoginDto request;
     // private AuthAccessTokenResponse response;
     private AuthTokensDto tokens;

     @BeforeEach 
     void setUp(){
       request = new AuthLoginDto("test@mail.com", "testPass");
       // response = new AuthAccessTokenResponse("test-access-token");
       tokens = new AuthTokensDto("test-access-token","test-refresh-token");
      }

     private void givenSuccessfulLogin(){
      when(authService.login(any())).thenReturn(tokens);
     }
 
     @Test 
     @DisplayName("login: валидный запрос → 200 и корректный ответ")
     public  void shouldRegisterSuccessfully()throws Exception{
        givenSuccessfulLogin();
        mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.accessToken").value("test-access-token"));

        verify(authService).login(eq(request));
     }

     @Test 
     @DisplayName("login: валидный запрос → 200 и успешное добавление refreshtoken в cookie")
     public  void shouldRegisterSuccessfullyAddRefreshtokenCookie()throws Exception{
        givenSuccessfulLogin();
        mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(cookie().exists("refreshToken"))
            .andExpect(cookie().value("refreshToken", "test-refresh-token"))
            .andExpect(cookie().httpOnly("refreshToken", true))
            .andExpect(cookie().secure("refreshToken", true))
            .andExpect(cookie().path("refreshToken", "/api/auth"))
            .andExpect(cookie().maxAge("refreshToken", 604800));

        verify(authService).login(eq(request));
     }

     @Test 
     @DisplayName("login: ошибка пустой accessToken сервис бросит 500")
     public  void login_whenErrorIsEmprtyAccessToken()throws Exception{
        when(authService.login(any())).thenThrow(new IllegalStateException("error generate access-refresh tokens"));

        mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(request)))
            .andExpect(status().isInternalServerError())
            .andExpect(cookie().doesNotExist("refreshToken"));
        verify(authService).login(eq(request));   
     }

     @Test 
     @DisplayName("login: невалидный email → 400")
     public  void login_inValidEmail_returnsBadRequest()throws Exception{
        request = new AuthLoginDto("test-.#mail.com", "testPass");
        mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
            
        verifyNoInteractions(authService);
     }
     @Test 
     @DisplayName("login: password < 8 символов → 400")
     public  void login_shortPassword_returnsBadRequest()throws Exception{
        request = new AuthLoginDto("test@mmail.com", "test");

        mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
            
        verifyNoInteractions(authService);
     }

     @Test 
     @DisplayName("login: password > 25 символов → 400")
     public  void login_longPassword_returnsBadRequest()throws Exception{
        request = new AuthLoginDto("test@mmail.com", "testPasstestPasstestPasstestPass");

        mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        verifyNoInteractions(authService);
     }

    }

    @Nested
    @DisplayName("Refresh")
    class Refresh{

     @Test 
     @DisplayName("refresh: refreshtoken отсутствует в куках -> ")
     public  void refresh_refreshtokenEmptyOrNull()throws Exception{
        Cookie refreshTokenCookie = new Cookie("refreshToken", "");
            mockMvc.perform(post("/api/auth/refresh")
                    .cookie(refreshTokenCookie))
                    .andExpect(status().isUnauthorized())
                    .andExpect(result -> assertInstanceOf(TokenValidationException.class, result.getResolvedException()
            ));

        verifyNoInteractions(authService);
     }

     @Test 
     @DisplayName("refresh: успешное обновление с ротацией refresh-токена -> 200 OK, новая кука и accessToken")
     public  void refresh_successUpdateTokens()throws Exception{
        String oldRefreshToken = "old-refresh-token";
        AuthTokensDto tokens = new AuthTokensDto("new-access-token", "new-refresh-token");
        when(authService.refresh(oldRefreshToken)).thenReturn(tokens);
        Cookie refreshTokenCookie = new Cookie("refreshToken", oldRefreshToken);

            mockMvc.perform(post("/api/auth/refresh")
                    .cookie(refreshTokenCookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                    .andExpect(cookie().value("refreshToken", "new-refresh-token"))
                    .andExpect(cookie().httpOnly("refreshToken", true))
                    .andExpect(cookie().secure("refreshToken", true))
                    .andExpect(cookie().path("refreshToken", "/api/auth"))
                    .andExpect(cookie().maxAge("refreshToken", 604800));
                    
        verify(authService).refresh(oldRefreshToken);
     }

     @Test 
     @DisplayName("refresh: успешное обновление accessToken но refreshToken остается старый")
     public  void refresh_successUpdateAccessTokenAndNotUpdateOldRefreshtoken()throws Exception{
        String oldRefreshToken = "old-refresh-token";
        AuthTokensDto tokens = new AuthTokensDto("new-access-token", null);
        when(authService.refresh(oldRefreshToken)).thenReturn(tokens);
        Cookie refreshTokenCookie = new Cookie("refreshToken", oldRefreshToken);

            mockMvc.perform(post("/api/auth/refresh")
                    .cookie(refreshTokenCookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                    .andExpect(cookie().doesNotExist("refreshToken"));
                    
        verify(authService).refresh(oldRefreshToken);
     }

  }

    @Nested
    @DisplayName("Logout")
    class Logout{
        
     @Test 
     @DisplayName("refresh: refreshtoken отсутствует в куках -> ")
     public  void refresh_refreshtokenEmptyOrNull()throws Exception{
        Cookie refreshTokenCookie = new Cookie("refreshToken", "");
            mockMvc.perform(post("/api/auth/logout")
                    .cookie(refreshTokenCookie))
                    .andExpect(status().isUnauthorized())
                    .andExpect(result -> assertInstanceOf(TokenValidationException.class, result.getResolvedException()
            ));

        verifyNoInteractions(authService);
     }

  }

}