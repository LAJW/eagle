package org.example.eagle.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.eagle.service.Auth;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final Auth auth;

    public JwtFilter(Auth auth) {
        this.auth = auth;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
        var authorization = request.getHeader("Authorization");
        final var bearer = "Bearer ";
        if (authorization == null || !authorization.startsWith(bearer)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        var token = authorization.substring(bearer.length());
        var userId = auth.getUserIdFromToken(token);
        if (userId.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        request.setAttribute("userId", userId.get());
    }
}
