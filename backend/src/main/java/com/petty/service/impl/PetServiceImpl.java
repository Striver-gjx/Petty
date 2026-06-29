package com.petty.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petty.entity.Pet;
import com.petty.mapper.PetMapper;
import com.petty.service.PetService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PetServiceImpl extends ServiceImpl<PetMapper, Pet> implements PetService {

    @Override
    public List<Pet> listByOwnerId(Long ownerId) {
        return list(new LambdaQueryWrapper<Pet>()
                .eq(Pet::getOwnerId, ownerId)
                .eq(Pet::getStatus, 1)
                .orderByDesc(Pet::getCreatedAt));
    }
}
