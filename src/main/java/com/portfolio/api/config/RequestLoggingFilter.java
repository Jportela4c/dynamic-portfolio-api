package com.portfolio.api.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Adds request context to MDC for logging.
 * MDC values can be used in log patterns to show which request triggered a log.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String remoteAddr = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");

            // Identify source
            String source = identifySource(remoteAddr, request.getRemoteHost(), userAgent);

            // Add essential request context to MDC
            MDC.put("source", source);
            MDC.put("method", request.getMethod());
            MDC.put("uri", request.getRequestURI());
            MDC.put("ip", remoteAddr);
            MDC.put("userAgent", userAgent != null ? userAgent : "");

            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }

    private String identifySource(String remoteAddr, String remoteHost, String userAgent) {
        // Docker health check (localhost)
        if ("0:0:0:0:0:0:0:1".equals(remoteAddr) || "127.0.0.1".equals(remoteAddr)) {
            return "docker-health";
        }

        // Prometheus scraper
        if (userAgent != null && userAgent.contains("Prometheus")) {
            return "prometheus";
        }

        // Browser
        if (userAgent != null && (userAgent.contains("Mozilla") || userAgent.contains("Chrome"))) {
            return "browser";
        }

        // Use hostname if available
        if (!remoteHost.equals(remoteAddr)) {
            return remoteHost;
        }

        // Default to IP
        return remoteAddr;
    }
}
