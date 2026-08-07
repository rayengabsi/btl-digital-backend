package com.btl.digital.mock.security;

import com.btl.digital.config.BrevoConfig;
import com.btl.digital.mock.store.MockDataStore;
import com.btl.digital.mock.model.MockClientUser;
import com.btl.digital.security.jwt.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MockJwtAuthenticationFilter extends OncePerRequestFilter {

    private final MockDataStore store;
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = header.substring(7);
            try {
                String email = jwtService.extractUsername(token);
                if (email != null) {
                    MockClientUser user = store.clientByEmail(email).orElse(null);
                    if (user != null && jwtService.isTokenValid(token, new org.springframework.security.core.userdetails.User(
                            user.getEmail(), user.getPasswordHash(), java.util.List.of()))) {
                        MockPrincipal principal = new MockPrincipal(user.getId(), user.getEmail());
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                principal, null, java.util.List.of(new SimpleGrantedAuthority("ROLE_CLIENT")));
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            } catch (Exception ignored) {
                // invalid/expired token => leave unauthenticated, handled by entry point
            }
        }
        filterChain.doFilter(request, response);
    }
}