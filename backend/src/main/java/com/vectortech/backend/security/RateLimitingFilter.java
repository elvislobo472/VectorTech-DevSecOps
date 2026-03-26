package com.vectortech.backend.security;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Rate Limiting Filter
 * Prevents brute force attacks by limiting requests per IP address
 * 
 * Rate Limits:
 * - Login endpoint: 5 requests per 60 seconds
 * - General API: 100 requests per 60 seconds
 * - Register endpoint: 3 requests per 3600 seconds (hourly)
 * 
 * Uses Redis for distributed rate limiting
 * 
 * @author VectorTech DevSecOps Team
 */
@Slf4j
@Component
public class RateLimitingFilter implements Filter {

    private final StringRedisTemplate redisTemplate;
    
    // Rate limiting configuration
    private static final int LOGIN_RATE_LIMIT = 5;
    private static final int LOGIN_WINDOW_SECONDS = 60;
    
    private static final int REGISTER_RATE_LIMIT = 3;
    private static final int REGISTER_WINDOW_SECONDS = 3600;
    
    private static final int API_RATE_LIMIT = 100;
    private static final int API_WINDOW_SECONDS = 60;

    public RateLimitingFilter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String clientIp = getClientIp(httpRequest);
        String requestPath = httpRequest.getRequestURI();

        try {
            // Check if request is rate limited
            if (isRateLimited(clientIp, requestPath)) {
                handleRateLimitExceeded(httpResponse, clientIp, requestPath);
                return;
            }

            // Continue with the request
            chain.doFilter(request, response);
        } catch (Exception e) {
            log.error("Error in rate limiting filter: {}", e.getMessage());
            chain.doFilter(request, response);
        }
    }

    /**
     * Check if the client IP has exceeded rate limit for the requested endpoint
     */
    private boolean isRateLimited(String clientIp, String requestPath) {
        String redisKey = getRedisKey(clientIp, requestPath);
        int limit = getRateLimit(requestPath);
        int window = getRateWindow(requestPath);

        // Increment request counter
        Long requestCount = redisTemplate.opsForValue().increment(redisKey);
        if (requestCount == null) {
            log.warn("Rate limit counter not initialized for key: {}", redisKey);
            return false;
        }

        // Set expiration on first request
        if (requestCount == 1) {
            redisTemplate.expire(redisKey, window, TimeUnit.SECONDS);
        }

        // Check if exceeded
        boolean rateLimited = requestCount > limit;
        
        if (rateLimited) {
            log.warn("Rate limit exceeded for IP: {} on endpoint: {} (count: {})", 
                clientIp, requestPath, requestCount);
        }

        return rateLimited;
    }

    /**
     * Get rate limit for specific endpoint
     */
    private int getRateLimit(String requestPath) {
        if (requestPath.contains("/auth/login")) {
            return LOGIN_RATE_LIMIT;
        } else if (requestPath.contains("/auth/register")) {
            return REGISTER_RATE_LIMIT;
        } else {
            return API_RATE_LIMIT;
        }
    }

    /**
     * Get rate limit window (in seconds) for specific endpoint
     */
    private int getRateWindow(String requestPath) {
        if (requestPath.contains("/auth/login")) {
            return LOGIN_WINDOW_SECONDS;
        } else if (requestPath.contains("/auth/register")) {
            return REGISTER_WINDOW_SECONDS;
        } else {
            return API_WINDOW_SECONDS;
        }
    }

    /**
     * Generate Redis key for rate limiting
     */
    private String getRedisKey(String clientIp, String requestPath) {
        return "ratelimit:" + clientIp + ":" + requestPath;
    }

    /**
     * Handle rate limit exceeded response
     */
    private void handleRateLimitExceeded(HttpServletResponse response, String clientIp, String requestPath) 
            throws IOException {
        
        response.setStatus(429);  // HTTP 429 Too Many Requests
        response.setContentType("application/json;charset=UTF-8");
        
        String errorResponse = String.format(
            "{\"error\": \"Rate limit exceeded\", \"message\": \"Too many requests from IP: %s\", " +
            "\"timestamp\": \"%s\", \"path\": \"%s\"}",
            clientIp,
            System.currentTimeMillis(),
            requestPath
        );

        response.getWriter().write(errorResponse);
        log.warn("Rate limit response sent to IP: {} on path: {}", clientIp, requestPath);
    }

    /**
     * Extract client IP address from request
     * Handles proxies and load balancers
     */
    private String getClientIp(HttpServletRequest request) {
        String clientIp = request.getHeader("X-Forwarded-For");
        
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getHeader("X-Real-IP");
        }
        
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getHeader("CLIENT-IP");
        }
        
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        }

        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = "unknown";
        }
        
        // Extract first IP in case of multiple IPs (comma separated)
        if (clientIp.contains(",")) {
            clientIp = clientIp.split(",")[0].trim();
        }
        
        return clientIp;
    }

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void destroy() {}
}
