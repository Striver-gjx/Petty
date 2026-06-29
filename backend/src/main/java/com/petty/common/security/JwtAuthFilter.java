package com.petty.common.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class JwtAuthFilter implements Filter {

    private final JwtUtil jwtUtil;

    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/service-types",
            "/h2-console"
    );

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

        // 开发模式：无 token 时使用默认用户（生产环境应删除此段）
        if (isDev()) {
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

    private boolean isDev() {
        String env = System.getProperty("spring.profiles.active");
        return env == null || !env.contains("prod");
    }
}
