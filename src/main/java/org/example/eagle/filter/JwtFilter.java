package org.example.eagle.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.eagle.service.Auth;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.lang.NonNullApi;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final Auth auth;

    public JwtFilter(Auth auth) {
        this.auth = auth;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        // Exclude POST /v1/users and POST /v1/login from JWT filter
        String path = request.getRequestURI();
        if ((path.equals("/v1/users") || path.equals("/v1/login")) && request.getMethod().equals("POST")) {
            filterChain.doFilter(request, response);
            return;
        }
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
        // Set authenticated principal in SecurityContext
        var authentication = new UsernamePasswordAuthenticationToken(userId.get(), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }
}
