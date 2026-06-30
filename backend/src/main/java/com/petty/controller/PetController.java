package com.petty.controller;

import com.petty.common.result.Result;
import com.petty.common.security.RequireRole;
import com.petty.common.security.UserContext;
import com.petty.entity.Pet;
import com.petty.service.PetService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    @GetMapping
    public Result<List<Pet>> list(@RequestParam(required = false) Long ownerId) {
        Long userId = UserContext.getUserId();
        String role = UserContext.getRole();
        if ("OWNER".equals(role)) {
            return Result.success(petService.listByOwnerId(userId));
        }
        if (ownerId != null) {
            return Result.success(petService.listByOwnerId(ownerId));
        }
        return Result.success(petService.list());
    }

    @GetMapping("/{id}")
    public Result<Pet> get(@PathVariable Long id) {
        return Result.success(petService.getById(id));
    }

    @PostMapping
    public Result<Pet> create(@RequestBody Pet pet) {
        Long userId = UserContext.getUserId();
        String role = UserContext.getRole();
        if ("OWNER".equals(role)) {
            pet.setOwnerId(userId);
        }
        petService.save(pet);
        return Result.success(pet);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Pet pet) {
        Long userId = UserContext.getUserId();
        String role = UserContext.getRole();
        if ("OWNER".equals(role)) {
            Pet existing = petService.getById(id);
            if (existing == null || !userId.equals(existing.getOwnerId())) {
                return Result.error(403, "无权修改该宠物");
            }
        }
        pet.setId(id);
        petService.updateById(pet);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        String role = UserContext.getRole();
        if ("OWNER".equals(role)) {
            Pet existing = petService.getById(id);
            if (existing == null || !userId.equals(existing.getOwnerId())) {
                return Result.error(403, "无权删除该宠物");
            }
        }
        petService.removeById(id);
        return Result.success();
    }
}
