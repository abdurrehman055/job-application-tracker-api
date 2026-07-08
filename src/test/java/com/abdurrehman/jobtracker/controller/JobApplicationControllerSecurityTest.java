package com.abdurrehman.jobtracker.controller;

import com.abdurrehman.jobtracker.config.SecurityConfig;
import com.abdurrehman.jobtracker.entity.User;
import com.abdurrehman.jobtracker.repository.UserRepository;
import com.abdurrehman.jobtracker.security.JwtAuthenticationEntryPoint;
import com.abdurrehman.jobtracker.security.JwtAuthenticationFilter;
import com.abdurrehman.jobtracker.security.JwtService;
import com.abdurrehman.jobtracker.service.JobApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = JobApplicationController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class, JwtService.class})
class JobApplicationControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private JobApplicationService jobApplicationService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void validBearerTokenIsAuthenticated() throws Exception {
        User user = User.builder().id(1L).email("test@example.com").password("hashed").build();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(jobApplicationService.search(eq(1L), any(), any(), any())).thenReturn(Page.empty());

        String token = jwtService.generateToken("test@example.com");

        mockMvc.perform(get("/api/job-applications").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void missingTokenIsRejectedWithUnauthorized() throws Exception {
        mockMvc.perform(get("/api/job-applications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void malformedTokenIsRejectedWithUnauthorized() throws Exception {
        mockMvc.perform(get("/api/job-applications").header("Authorization", "Bearer not-a-real-jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unknownUserIsRejectedWithUnauthorized() throws Exception {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());
        String token = jwtService.generateToken("ghost@example.com");

        mockMvc.perform(get("/api/job-applications").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }
}
