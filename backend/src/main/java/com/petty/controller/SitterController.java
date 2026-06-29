package com.petty.controller;

import com.petty.common.result.Result;
import com.petty.common.security.RequireRole;
import com.petty.entity.Sitter;
import com.petty.service.SitterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/sitters")
@RequiredArgsConstructor
public class SitterController {

    private final SitterService sitterService;

    @GetMapping
    public Result<List<Sitter>> list(@RequestParam(required = false) String status) {
        if (status != null && !status.isEmpty()) {
            return Result.success(sitterService.list(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Sitter>()
                            .eq(Sitter::getStatus, status)
                            .orderByDesc(Sitter::getRating)));
        }
        return Result.success(sitterService.list());
    }

    @GetMapping("/{id}")
    public Result<Sitter> get(@PathVariable Long id) {
        return Result.success(sitterService.getById(id));
    }

    @PostMapping("/apply")
    public Result<Sitter> apply(@RequestBody Sitter sitter) {
        sitterService.applyOnboard(sitter);
        return Result.success(sitter);
    }

    @PostMapping("/{id}/approve")
    @RequireRole("ADMIN")
    public Result<Void> approve(@PathVariable Long id) {
        sitterService.approveOnboard(id);
        return Result.success();
    }

    @PostMapping("/{id}/reject")
    @RequireRole("ADMIN")
    public Result<Void> reject(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        sitterService.rejectOnboard(id, reason);
        return Result.success();
    }

    @PostMapping
    @RequireRole("ADMIN")
    public Result<Sitter> create(@RequestBody Sitter sitter) {
        sitterService.save(sitter);
        return Result.success(sitter);
    }

    @PutMapping("/{id}")
    @RequireRole("ADMIN")
    public Result<Void> update(@PathVariable Long id, @RequestBody Sitter sitter) {
        sitter.setId(id);
        sitterService.updateById(sitter);
        return Result.success();
    }
}
