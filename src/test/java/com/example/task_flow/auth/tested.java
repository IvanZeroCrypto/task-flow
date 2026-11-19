package com.example.task_flow.auth;


/*
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

    @Test
    @DisplayName("Логин: email  найден →  пароль верный , пользователь получит токены")
    void login_whenEmailExists_returnsTokens_andSavesRefreshToken() {
        var request = new AuthLoginDto("test@mail.com", "testPass");
        //email найден
        when(repository.existsByEmail("test@mail.com")).thenReturn(true);



        //достаём User из БД
        User user = new User();
        user.setId(1L);
        user.setEmail("test@mail.com");

        when(repository.findByEmail("test@mail.com")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(1L, "test@mail.com")).thenReturn("test-access-token");

        AuthTokensDto response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("test-access-token");
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(response.refreshToken()).matches("[0-9a-f-]{36}");

        verify(jwtService).generateAccessToken(1L, "test@mail.com");

        ArgumentCaptor<UsernamePasswordAuthenticationToken> authCaptor = ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(authCaptor.capture());
        assertThat(authCaptor.getValue().getPrincipal()).isEqualTo("test@mail.com");
        assertThat(authCaptor.getValue().getCredentials()).isEqualTo("testPass");

        //refresh-токен сохранён в Redis с правильным userId

        ArgumentCaptor<String> refreshCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> userIdCaptor = ArgumentCaptor.forClass(String.class);

        verify(redisService).saveRefreshToken(refreshCaptor.capture(),userIdCaptor.capture());

        assertThat(refreshCaptor.getValue()).isEqualTo(response.refreshToken());
        assertThat(refreshCaptor.getValue()).matches("[0-9a-f-]{36}");
        assertThat(userIdCaptor.getValue()).isEqualTo("1");

        verifyNoInteractions(passwordEncoder);
    }

 
}

*/