package com.petty.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petty.entity.Sitter;
import com.petty.mapper.SitterMapper;
import com.petty.service.SitterService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SitterServiceImpl extends ServiceImpl<SitterMapper, Sitter> implements SitterService {

    @Override
    public List<Sitter> listActive() {
        return list(new LambdaQueryWrapper<Sitter>()
                .eq(Sitter::getStatus, "ACTIVE")
                .orderByDesc(Sitter::getRating));
    }
}
