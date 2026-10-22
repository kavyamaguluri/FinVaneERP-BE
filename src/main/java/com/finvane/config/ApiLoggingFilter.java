package com.finvane.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class ApiLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ApiLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();
        String uri = request.getRequestURI();
        String method = request.getMethod();
        String queryString = request.getQueryString() != null ? "?" + request.getQueryString() : "";
        String tenant = request.getHeader("X-Entity-ID");

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = responseWrapper.getStatus();

            String responseBody = "";
            byte[] responseArray = responseWrapper.getContentAsByteArray();
            if (responseArray.length > 0) {
                String contentType = responseWrapper.getContentType();
                if (contentType != null && (contentType.contains("json") || contentType.contains("text"))) {
                    responseBody = new String(responseArray, StandardCharsets.UTF_8);
                    if (responseBody.length() > 500) {
                        responseBody = responseBody.substring(0, 500) + "... [truncated]";
                    }
                }
            }

            log.info("[API AUDIT] {} {}{} | Status: {} | Duration: {}ms | Tenant: {} | Response: {}",
                    method, uri, queryString, status, duration, (tenant != null ? tenant : "default"),
                    responseBody.isEmpty() ? "<empty/binary>" : responseBody);

            responseWrapper.copyBodyToResponse();
        }
    }
}
