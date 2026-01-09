package com.codeforge.config;

import com.codeforge.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // ✅ IGNORE STATIC FILES COMPLETELY
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(
                "/",
                "/*.html",
                "/index.html",
                "/css/**",
                "/static/**",
                "/js/**",
                "/images/**",
                "/favicon.ico"
        );
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // ✅ Disable CSRF for JWT
                .csrf(csrf -> csrf.disable())

                // ✅ Stateless session
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // ✅ Authorization
                .authorizeHttpRequests(auth -> auth
                        // PUBLIC APIs
                        .requestMatchers("/api/auth/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/enrollments/course/**")
                        .hasAnyRole("ADMIN", "STUDENT")

                        // STUDENT
                        .requestMatchers("/api/enrollments/**").hasRole("STUDENT")

                        // ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/courses/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/courses/**")
                        .hasAnyRole("ADMIN", "STUDENT")

                        .requestMatchers("/api/chat/**").authenticated()
                        // Everything else must be authenticated
                        .anyRequest().authenticated()
                )

                // ✅ JWT Filter
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}