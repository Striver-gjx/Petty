package com.petty.controller;

import com.petty.common.result.Result;
import com.petty.common.security.RequireRole;
import com.petty.entity.Owner;
import com.petty.service.OwnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/owners")
@RequiredArgsConstructor
public class OwnerController {

    private final OwnerService ownerService;

    @GetMapping
    @RequireRole("ADMIN")
    public Result<List<Owner>> list() {
        return Result.success(ownerService.list());
    }

    @GetMapping("/{id}")
    @RequireRole("ADMIN")
    public Result<Owner> get(@PathVariable Long id) {
        return Result.success(ownerService.getById(id));
    }

    @PostMapping
    @RequireRole("ADMIN")
    public Result<Owner> create(@RequestBody Owner owner) {
        ownerService.save(owner);
        return Result.success(owner);
    }

    @PutMapping("/{id}")
    @RequireRole("ADMIN")
    public Result<Void> update(@PathVariable Long id, @RequestBody Owner owner) {
        owner.setId(id);
        ownerService.updateById(owner);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @RequireRole("ADMIN")
    public Result<Void> delete(@PathVariable Long id) {
        ownerService.removeById(id);
        return Result.success();
    }
}
