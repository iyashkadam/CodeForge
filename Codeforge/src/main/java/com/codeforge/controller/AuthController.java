package com.codeforge.controller;

import com.codeforge.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> req) {

        authService.register(
                req.get("name"),
                req.get("email"),
                req.get("password"),
                req.get("branch")
        );

        return ResponseEntity.ok("User registered");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> req) {

        String token = authService.login(
                req.get("email"),
                req.get("password")
        );

        return ResponseEntity.ok(Map.of("token", token));
    }
}