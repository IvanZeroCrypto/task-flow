package com.example.task_flow.auth;

import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import com.example.task_flow.auth.dto.AuthRegisterDto;
import com.example.task_flow.user.UserRepository;
import tools.jackson.databind.json.JsonMapper;


import jakarta.transaction.Transactional;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc(addFilters = false) 
public class AuthFeatureIntegrationTest{

	@Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("register: валидный запрос → 200 и  пользователь реально в БД")
    void register_persistsUserToDb() throws Exception{
    	AuthRegisterDto request = new AuthRegisterDto("testnick", "test@mail.com", "testPass123");
    	String body = mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(request)))
    	            .andExpect(status().isOk())
    	            .andExpect(jsonPath("$.nickname").value("testnick"))
                    .andReturn().getResponse().getContentAsString();
        Long id = jsonMapper.readTree(body).get("id").asLong();

        assertThat(userRepository.findById(id)).isPresent()
        			.get().satisfies(u -> {
        				assertThat(u.getEmail()).isEqualTo("test@mail.com");
        				assertThat(u.getNickname()).isEqualTo("testnick");
        				assertThat(u.getPassword()).isNotEqualTo("testPass123");
        			});
    }

    @Test
    @DisplayName("register: валидный запрос → 409  conflict email в БД")
    void register_EmailNotFree() throws Exception{
        AuthRegisterDto request = new AuthRegisterDto("testnick", "test@mail.com", "testPass123");
        mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

        AuthRegisterDto secondRequest = new AuthRegisterDto("nicktest", "test@mail.com", "testpassword");

        mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(secondRequest)))
                    .andExpect(status().isConflict());
       
    }

}