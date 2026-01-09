package com.codeforge.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // ✅ SKIP LOGIN & REGISTER
        String path = request.getServletPath();

        // 🔥 SKIP JWT FILTER FOR LOGIN & REGISTER
        if (path.startsWith("/api/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 1️⃣ Read Authorization header
        final String authHeader = request.getHeader("Authorization");

        // 2️⃣ If header missing or not Bearer → skip filter
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3️⃣ Extract token
        final String token = authHeader.substring(7).trim();

        // 4️⃣ Extract username(email) from token
        final String username = jwtService.extractUsername(token);

        // 5️⃣ Only authenticate if not already authenticated
        if (username != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

            // 6️⃣ Load user from DB
            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(username);

            // 7️⃣ Extract role from JWT
            String role = jwtService.extractRole(token);

            // 🔥 THIS LINE IS THE MOST IMPORTANT PART
            List<GrantedAuthority> authority =
                    List .of(new SimpleGrantedAuthority("ROLE_" + role));

            // 8️⃣ Create Authentication object
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            authority
                    );

            authToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            // 9️⃣ Set authentication in SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        // 🔟 Continue filter chain
        filterChain.doFilter(request, response);
    }
}