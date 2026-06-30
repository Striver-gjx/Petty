package com.petty.common.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Slf4j
@Component
@Order(1)
public class JwtAuthFilter implements Filter {

    private final JwtUtil jwtUtil;
    private final boolean devBypassEnabled;

    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/service-types",
            "/actuator"
    );

    public JwtAuthFilter(JwtUtil jwtUtil,
                         @Value("${petty.security.dev-bypass:false}") boolean devBypassEnabled) {
        this.jwtUtil = jwtUtil;
        this.devBypassEnabled = devBypassEnabled;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getRequestURI();

        if (isPublicPath(path) || "OPTIONS".equalsIgnoreCase(req.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = req.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.validateToken(token)) {
                Long userId = jwtUtil.getUserId(token);
                String role = jwtUtil.getRole(token);
                UserContext.set(new UserContext.UserInfo(userId, role));
                try {
                    chain.doFilter(request, response);
                } finally {
                    UserContext.clear();
                }
                return;
            }
        }

        if (devBypassEnabled) {
            log.warn("Dev bypass active: treating unauthenticated request as OWNER(id=1) — path={}", path);
            UserContext.set(new UserContext.UserInfo(1L, "OWNER"));
            try {
                chain.doFilter(request, response);
            } finally {
                UserContext.clear();
            }
            return;
        }

        res.setStatus(401);
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write("{\"code\":401,\"message\":\"未登录或token已过期\",\"data\":null}");
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }
}
