package com.petty.controller;

import com.petty.common.result.Result;
import com.petty.entity.Sitter;
import com.petty.service.SitterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sitters")
@RequiredArgsConstructor
public class SitterController {

    private final SitterService sitterService;

    @GetMapping
    public Result<List<Sitter>> list(@RequestParam(required = false) String status) {
        if ("ACTIVE".equals(status)) {
            return Result.success(sitterService.listActive());
        }
        return Result.success(sitterService.list());
    }

    @GetMapping("/{id}")
    public Result<Sitter> get(@PathVariable Long id) {
        return Result.success(sitterService.getById(id));
    }

    @PostMapping
    public Result<Sitter> create(@RequestBody Sitter sitter) {
        sitterService.save(sitter);
        return Result.success(sitter);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Sitter sitter) {
        sitter.setId(id);
        sitterService.updateById(sitter);
        return Result.success();
    }
}
