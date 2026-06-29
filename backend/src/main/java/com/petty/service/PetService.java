package com.petty.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.petty.entity.Pet;
import java.util.List;

public interface PetService extends IService<Pet> {
    List<Pet> listByOwnerId(Long ownerId);
}
