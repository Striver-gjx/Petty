package com.petty.common.security;

import com.petty.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

@Component
public class RoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequireRole annotation = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (annotation == null) {
            return true;
        }

        String currentRole = UserContext.getRole();
        if (currentRole == null) {
            throw new BusinessException(403, "未授权访问");
        }

        boolean hasRole = Arrays.asList(annotation.value()).contains(currentRole);
        if (!hasRole) {
            throw new BusinessException(403, "权限不足，需要角色: " + String.join("/", annotation.value()));
        }

        return true;
    }
}
