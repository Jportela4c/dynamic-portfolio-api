package com.portfolio.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.api.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class,
    excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
    },
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "com\\.portfolio\\.api\\.security\\..*"
    ))
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtTokenProvider tokenProvider;

    @Test
    void shouldGenerateTokenForValidCredentials() throws Exception {
        // Arrange
        Map<String, String> credentials = Map.of("username", "testuser", "password", "testpass");
        String mockToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlciJ9.test";

        when(tokenProvider.generateToken("testuser")).thenReturn(mockToken);

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value(mockToken))
            .andExpect(jsonPath("$.type").value("Bearer"));

        verify(tokenProvider).generateToken("testuser");
    }

    @Test
    void shouldGenerateTokenForDifferentUser() throws Exception {
        // Arrange
        Map<String, String> credentials = Map.of("username", "admin", "password", "admin123");
        String mockToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiJ9.admin";

        when(tokenProvider.generateToken("admin")).thenReturn(mockToken);

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value(mockToken))
            .andExpect(jsonPath("$.type").value("Bearer"));

        verify(tokenProvider).generateToken("admin");
    }
}
