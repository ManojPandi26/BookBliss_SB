package com.BookBliss.Utils;

import com.BookBliss.Exception.AuthenticationException;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${jwt.secret}")
    private String secretKey;

    // 15 mins default
    @Value("${jwt.expiration}")
    private long expiration;

    // 7 days default
    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    private final UserDetailsService userDetailsService;

    public JwtTokenProvider(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    private Key getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
    }

    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Map<String, Object> claims = new HashMap<>();

        // Add user roles to claims
        claims.put("roles", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        // Add token identifier
        claims.put("tokenId", UUID.randomUUID().toString());

        return createToken(claims, userDetails.getUsername(), expiration);
    }

    public String generateRefreshToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Map<String, Object> claims = new HashMap<>();

        // Add refresh token identifier
        claims.put("tokenId", UUID.randomUUID().toString());
        claims.put("type", "REFRESH");

        return createToken(claims, userDetails.getUsername(), refreshExpiration);
    }

    private String createToken(Map<String, Object> claims, String subject, long expirationTime) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .setId(UUID.randomUUID().toString())
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
            throw e;
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
            throw e;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractTokenId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("tokenId", String.class);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("roles", List.class);
    }

    public long getTokenExpiration() {
        return expiration;
    }

    public long getRefreshTokenExpiration() {
        return refreshExpiration;
    }

    public boolean validateRefreshToken(String token) {
        try {
            if (token == null) {
                return false;
            }

            // Clean the token string
            String cleanedToken = cleanTokenString(token);

            // Basic format validation
            if (!isValidTokenFormat(cleanedToken)) {
                logger.error("Invalid token format");
                return false;
            }

            // Extract claims to check token type
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(cleanedToken)
                    .getBody();

            // Validate it's a refresh token
            String tokenType = claims.get("type", String.class);
            if (!"REFRESH".equals(tokenType)) {
                logger.error("Token is not a refresh token");
                return false;
            }

            // Check if token is expired
            if (claims.getExpiration().before(new Date())) {
                logger.error("Refresh token is expired");
                return false;
            }

            return true;
        } catch (JwtException e) {
            logger.error("Invalid refresh token: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Token validation error: {}", e.getMessage());
            return false;
        }
    }

    private String cleanTokenString(String token) {
        if (token == null) {
            return null;
        }

        // Remove any whitespace, quotes, and potential JSON formatting
        return token
                .replace("\"", "")
                .replace("{", "")
                .replace("}", "")
                .replace(":", "")
                .replace("refreshToken", "")
                .trim();
    }

    private boolean isValidTokenFormat(String token) {
        // Check if token has three parts separated by dots
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return false;
        }

        // Check if each part is base64url encoded
        for (String part : parts) {
            if (!isBase64UrlEncoded(part)) {
                return false;
            }
        }

        return true;
    }

    private boolean isBase64UrlEncoded(String str) {
        // Check if string matches base64url pattern
        return str.matches("^[A-Za-z0-9_-]*$");
    }

    public Authentication getAuthenticationFromToken(String token) {
        try {
            // Extract username from token
            String username = extractUsername(token);

            // Load user details using UserDetailsService
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Create authentication token with proper authorities
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );

            return authentication;
        } catch (Exception e) {
            logger.error("Could not create authentication from token: {}", e.getMessage());
            throw new AuthenticationException("Invalid token");
        }
    }
}