package com.codeforge.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/student")
    @PreAuthorize("hasRole('STUDENT')")
    public String studentApi() {
        return "Hello STUDENT";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminApi() {
        return "Hello ADMIN";
    }
}