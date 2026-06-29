package com.petty.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petty.common.result.Result;
import com.petty.entity.ServiceType;
import com.petty.mapper.ServiceTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/service-types")
@RequiredArgsConstructor
public class ServiceTypeController {

    private final ServiceTypeMapper serviceTypeMapper;

    @GetMapping
    public Result<List<ServiceType>> list() {
        List<ServiceType> types = serviceTypeMapper.selectList(
                new LambdaQueryWrapper<ServiceType>()
                        .eq(ServiceType::getStatus, 1)
                        .orderByAsc(ServiceType::getSortOrder));
        return Result.success(types);
    }

    @GetMapping("/{id}")
    public Result<ServiceType> get(@PathVariable Long id) {
        return Result.success(serviceTypeMapper.selectById(id));
    }
}
