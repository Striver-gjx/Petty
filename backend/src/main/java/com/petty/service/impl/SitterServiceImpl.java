package com.petty.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petty.common.exception.BusinessException;
import com.petty.entity.Sitter;
import com.petty.mapper.SitterMapper;
import com.petty.service.SitterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class SitterServiceImpl extends ServiceImpl<SitterMapper, Sitter> implements SitterService {

    @Override
    @Cacheable(value = "sitters", key = "'active'")
    public List<Sitter> listActive() {
        return list(new LambdaQueryWrapper<Sitter>()
                .eq(Sitter::getStatus, "ACTIVE")
                .orderByDesc(Sitter::getRating));
    }

    @Override
    public void applyOnboard(Sitter sitter) {
        if (sitter.getName() == null || sitter.getPhone() == null || sitter.getIdCard() == null) {
            throw new BusinessException("姓名、手机号、身份证号为必填项");
        }
        sitter.setStatus("PENDING_REVIEW");
        sitter.setBackgroundCheckStatus("PENDING");
        sitter.setRating(new BigDecimal("5.00"));
        sitter.setTotalOrders(0);
        sitter.setTotalReviews(0);
        sitter.setCompletionRate(new BigDecimal("100.00"));
        sitter.setWalletBalance(BigDecimal.ZERO);
        sitter.setCreatedAt(LocalDateTime.now());
        sitter.setUpdatedAt(LocalDateTime.now());
        save(sitter);
        log.info("喂养师入驻申请: name={}, phone={}", sitter.getName(), sitter.getPhone());
    }

    @Override
    @CacheEvict(value = "sitters", allEntries = true)
    public void approveOnboard(Long sitterId) {
        Sitter sitter = getById(sitterId);
        if (sitter == null) throw new BusinessException("喂养师不存在");
        if (!"PENDING_REVIEW".equals(sitter.getStatus())) {
            throw new BusinessException("当前状态不允许审批: " + sitter.getStatus());
        }
        sitter.setStatus("ACTIVE");
        sitter.setBackgroundCheckStatus("PASSED");
        sitter.setUpdatedAt(LocalDateTime.now());
        updateById(sitter);
        log.info("喂养师审批通过: id={}, name={}", sitterId, sitter.getName());
    }

    @Override
    @CacheEvict(value = "sitters", allEntries = true)
    public void rejectOnboard(Long sitterId, String reason) {
        Sitter sitter = getById(sitterId);
        if (sitter == null) throw new BusinessException("喂养师不存在");
        if (!"PENDING_REVIEW".equals(sitter.getStatus())) {
            throw new BusinessException("当前状态不允许审批: " + sitter.getStatus());
        }
        sitter.setStatus("REJECTED");
        sitter.setUpdatedAt(LocalDateTime.now());
        updateById(sitter);
        log.info("喂养师审批拒绝: id={}, reason={}", sitterId, reason);
    }
}
