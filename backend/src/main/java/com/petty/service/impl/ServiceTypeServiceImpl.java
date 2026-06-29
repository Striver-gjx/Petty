package com.petty.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petty.common.exception.BusinessException;
import com.petty.entity.ServiceType;
import com.petty.mapper.ServiceTypeMapper;
import com.petty.service.ServiceTypeService;
import com.petty.vo.ServiceTypeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceTypeServiceImpl implements ServiceTypeService {

    private final ServiceTypeMapper serviceTypeMapper;

    @Override
    public List<ServiceTypeVO> listActive() {
        List<ServiceType> types = serviceTypeMapper.selectList(
                new LambdaQueryWrapper<ServiceType>()
                        .eq(ServiceType::getStatus, 1)
                        .orderByAsc(ServiceType::getSortOrder));
        return types.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public ServiceTypeVO getById(Long id) {
        ServiceType type = serviceTypeMapper.selectById(id);
        if (type == null) throw new BusinessException(404, "服务类型不存在");
        return toVO(type);
    }

    private ServiceTypeVO toVO(ServiceType entity) {
        ServiceTypeVO vo = new ServiceTypeVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setCode(entity.getCode());
        vo.setDescription(entity.getDescription());
        vo.setIconUrl(entity.getIconUrl());
        vo.setBaseDurationMin(entity.getBaseDurationMin());
        vo.setBasePrice(entity.getBasePrice());
        vo.setExtraPetPrice(entity.getExtraPetPrice());
        vo.setSortOrder(entity.getSortOrder());
        vo.setApplicableSpecies(parseJsonArray(entity.getApplicableSpecies()));
        vo.setChecklistTemplate(parseJsonArray(entity.getChecklistTemplate()));
        return vo;
    }

    private List<String> parseJsonArray(String json) {
        if (json == null || json.isEmpty()) return Collections.emptyList();
        String cleaned = json.replaceAll("[\\[\\]\"]", "");
        return Arrays.asList(cleaned.split(","));
    }
}
