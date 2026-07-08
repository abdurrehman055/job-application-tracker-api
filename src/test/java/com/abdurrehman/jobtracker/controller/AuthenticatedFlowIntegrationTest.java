package com.abdurrehman.jobtracker.controller;

import com.abdurrehman.jobtracker.config.SecurityConfig;
import com.abdurrehman.jobtracker.dto.response.AuthResponse;
import com.abdurrehman.jobtracker.entity.User;
import com.abdurrehman.jobtracker.repository.UserRepository;
import com.abdurrehman.jobtracker.security.JwtAuthenticationEntryPoint;
import com.abdurrehman.jobtracker.security.JwtAuthenticationFilter;
import com.abdurrehman.jobtracker.security.JwtService;
import com.abdurrehman.jobtracker.service.AuthService;
import com.abdurrehman.jobtracker.service.JobApplicationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Reproduces the exact real-world flow: hit /api/auth/register through the
 * real AuthController/AuthService, take the token straight out of the JSON
 * response, then use that exact token against a protected endpoint - instead
 * of minting a token directly via JwtService, which could hide a bug that
 * only shows up going through the real endpoint.
 */
@WebMvcTest(controllers = {AuthController.class, JobApplicationController.class})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class,
        JwtService.class, AuthService.class})
class AuthenticatedFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JobApplicationService jobApplicationService;

    @Test
    void tokenFromRegisterEndpointAuthenticatesSubsequentRequest() throws Exception {
        String email = "pipeline@example.com";

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String registerBody = """
                {"email":"%s","password":"password123","fullName":"Pipeline Test"}
                """.formatted(email);

        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(registerBody))
                .andExpect(status().isCreated())
                .andReturn();

        AuthResponse authResponse = objectMapper.readValue(
                registerResult.getResponse().getContentAsString(), AuthResponse.class);

        User savedUser = User.builder().id(1L).email(email).password("hashed").build();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(savedUser));
        when(jobApplicationService.search(eq(1L), any(), any(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/job-applications")
                        .header("Authorization", authResponse.tokenType() + " " + authResponse.token()))
                .andExpect(status().isOk());
    }
}
