package com.codeforge.service;

import com.codeforge.models.Role;
import com.codeforge.models.User;
import com.codeforge.repository.UserRepository;
import com.codeforge.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // =========================
    // REGISTER
    // =========================
    public User register(String name,
                         String email,
                         String password,
                         String branch) {

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);

        // 🔐 ALWAYS encode password before saving
        user.setPassword(passwordEncoder.encode(password));

        user.setBranch(branch);

        // Default role
        user.setRole(Role.STUDENT);

        return userRepository.save(user);
    }

    // =========================
    // LOGIN
    // =========================
    public String login(String email, String password) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 🔥 THIS IS THE CRITICAL CHECK
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        // Generate JWT with role
        return jwtService.generateToken(
                user.getEmail(),
                user.getRole().name()   // STUDENT / ADMIN
        );
    }
}