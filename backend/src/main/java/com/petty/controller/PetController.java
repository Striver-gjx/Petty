package com.petty.controller;

import com.petty.common.result.Result;
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
        petService.save(pet);
        return Result.success(pet);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Pet pet) {
        pet.setId(id);
        petService.updateById(pet);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        petService.removeById(id);
        return Result.success();
    }
}
