package com.petty.controller;

import com.petty.common.result.Result;
import com.petty.service.ServiceTypeService;
import com.petty.vo.ServiceTypeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/service-types")
@RequiredArgsConstructor
public class ServiceTypeController {

    private final ServiceTypeService serviceTypeService;

    @GetMapping
    public Result<List<ServiceTypeVO>> list() {
        return Result.success(serviceTypeService.listActive());
    }

    @GetMapping("/{id}")
    public Result<ServiceTypeVO> get(@PathVariable Long id) {
        return Result.success(serviceTypeService.getById(id));
    }
}
