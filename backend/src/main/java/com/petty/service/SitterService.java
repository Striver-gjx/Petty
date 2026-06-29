package com.petty.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.petty.entity.Sitter;
import java.util.List;

public interface SitterService extends IService<Sitter> {
    List<Sitter> listActive();
}
