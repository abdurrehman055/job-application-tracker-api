package com.abdurrehman.jobtracker.service;

import com.abdurrehman.jobtracker.dto.request.LoginRequest;
import com.abdurrehman.jobtracker.dto.request.RegisterRequest;
import com.abdurrehman.jobtracker.dto.response.AuthResponse;
import com.abdurrehman.jobtracker.entity.User;
import com.abdurrehman.jobtracker.exception.DuplicateResourceException;
import com.abdurrehman.jobtracker.repository.UserRepository;
import com.abdurrehman.jobtracker.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already registered: " + request.email());
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .build();

        userRepository.save(user);

        return new AuthResponse(jwtService.generateToken(user.getEmail()));
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        return new AuthResponse(jwtService.generateToken(user.getEmail()));
    }
}
