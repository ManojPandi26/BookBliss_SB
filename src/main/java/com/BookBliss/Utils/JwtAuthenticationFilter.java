package com.BookBliss.Utils;

import com.BookBliss.DTO.Auth.TokenResponse;
import com.BookBliss.Service.Auth.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";
    private static final List<String> EXCLUDED_URLS = Arrays.asList(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/forgot-password",
            "/api/auth/refresh-token",

            // OpenAPI/Swagger endpoints
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api-docs/**",
            "/v3/api-docs/**",
            "/favicon.ico",

            "/actuator/health"
    );

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final TokenService tokenService;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return EXCLUDED_URLS.stream().anyMatch(path::startsWith) ||
                request.getMethod().equals("OPTIONS");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String jwt = extractJwtFromRequest(request);

            if (jwt != null) {
                processJwtAuthentication(jwt, request);
            }
        } catch (ExpiredJwtException e) {
            handleExpiredToken(request, response);
            return;
        } catch (Exception e) {
            handleAuthenticationError(e, response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
            return authorizationHeader.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    private void handleAuthenticationError(Exception e, HttpServletResponse response) throws IOException {
        logger.error("Authentication error: {}", e.getMessage());

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        var errorResponse = Map.of(
                "timestamp", java.time.LocalDateTime.now().toString(),
                "status", HttpServletResponse.SC_UNAUTHORIZED,
                "error", "Unauthorized",
                "message", e.getMessage(),
                "path", "/"
        );

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }

    private void processJwtAuthentication(String jwt, HttpServletRequest request) {
        String username = jwtTokenProvider.extractUsername(jwt);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Use tokenService to validate token
            if (tokenService.validateAccessToken(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authentication = createAuthenticationToken(userDetails, request);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.debug("Security context updated for user: {}", username);
            }
        }
    }

    private void handleExpiredToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String refreshToken = request.getHeader("Refresh-Token");
        if (refreshToken != null) {
            try {
                // Use tokenService to refresh tokens
                TokenResponse newTokens = tokenService.refreshTokens(refreshToken);
                sendTokenResponse(response, newTokens);
            } catch (Exception e) {
                logger.error("Failed to refresh token: {}", e.getMessage());
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid refresh token. Please login again.");
            }
        } else {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token expired. Please login again.");
        }
    }

    private void sendTokenResponse(HttpServletResponse response, TokenResponse tokens) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        objectMapper.writeValue(response.getOutputStream(), tokens);
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");

        var errorResponse = Map.of(
                "timestamp", java.time.LocalDateTime.now().toString(),
                "status", status,
                "error", "Authentication error",
                "message", message
        );

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }

    private UsernamePasswordAuthenticationToken createAuthenticationToken(UserDetails userDetails, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return authentication;
    }
}