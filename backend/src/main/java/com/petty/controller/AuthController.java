package com.petty.controller;

import com.petty.common.result.Result;
import com.petty.common.security.JwtUtil;
import com.petty.common.security.UserContext;
import com.petty.entity.Owner;
import com.petty.entity.Sitter;
import com.petty.mapper.OwnerMapper;
import com.petty.mapper.SitterMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;
    private final OwnerMapper ownerMapper;
    private final SitterMapper sitterMapper;

    /**
     * 模拟登录（开发阶段直接用 phone 登录，无需微信授权）
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDTO dto) {
        if ("SITTER".equalsIgnoreCase(dto.getRole())) {
            Sitter sitter = sitterMapper.selectOne(
                    new LambdaQueryWrapper<Sitter>().eq(Sitter::getPhone, dto.getPhone()));
            if (sitter == null) return Result.error(401, "喂养师不存在");
            String token = jwtUtil.generateToken(sitter.getId(), "SITTER");
            return Result.success(Map.of("token", token, "userId", sitter.getId(), "name", sitter.getName()));
        } else {
            Owner owner = ownerMapper.selectOne(
                    new LambdaQueryWrapper<Owner>().eq(Owner::getPhone, dto.getPhone()));
            if (owner == null) return Result.error(401, "用户不存在");
            String token = jwtUtil.generateToken(owner.getId(), "OWNER");
            return Result.success(Map.of("token", token, "userId", owner.getId(), "name", owner.getNickname()));
        }
    }

    @GetMapping("/me")
    public Result<Map<String, Object>> me() {
        Long userId = UserContext.getUserId();
        String role = UserContext.getRole();
        return Result.success(Map.of("userId", userId, "role", role));
    }

    @Data
    public static class LoginDTO {
        private String phone;
        private String role;
    }
}
