package com.petty.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petty.common.exception.BusinessException;
import com.petty.dto.WithdrawalDTO;
import com.petty.entity.Sitter;
import com.petty.entity.SitterWithdrawal;
import com.petty.mapper.SitterMapper;
import com.petty.mapper.SitterWithdrawalMapper;
import com.petty.service.WithdrawalService;
import com.petty.vo.WithdrawalVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WithdrawalServiceImpl implements WithdrawalService {

    private final SitterWithdrawalMapper withdrawalMapper;
    private final SitterMapper sitterMapper;

    @Override
    @Transactional
    public void requestWithdrawal(Long sitterId, WithdrawalDTO dto) {
        Sitter sitter = sitterMapper.selectById(sitterId);
        if (sitter == null) throw new BusinessException("喂养师不存在");

        BigDecimal balance = sitter.getWalletBalance() != null ? sitter.getWalletBalance() : BigDecimal.ZERO;
        if (dto.getAmount().compareTo(balance) > 0) {
            throw new BusinessException("余额不足，当前可提现: ¥" + balance);
        }
        if (dto.getAmount().compareTo(new BigDecimal("10")) < 0) {
            throw new BusinessException("最低提现金额为10元");
        }

        Long todayCount = withdrawalMapper.selectCount(
                new LambdaQueryWrapper<SitterWithdrawal>()
                        .eq(SitterWithdrawal::getSitterId, sitterId)
                        .ge(SitterWithdrawal::getCreatedAt, LocalDate.now().atStartOfDay()));
        if (todayCount >= 1) {
            throw new BusinessException("每日最多提现1次");
        }

        sitter.setWalletBalance(balance.subtract(dto.getAmount()));
        sitterMapper.updateById(sitter);

        SitterWithdrawal withdrawal = new SitterWithdrawal();
        withdrawal.setSitterId(sitterId);
        withdrawal.setAmount(dto.getAmount());
        withdrawal.setStatus("PENDING");
        withdrawal.setCreatedAt(LocalDateTime.now());
        withdrawalMapper.insert(withdrawal);

        log.info("提现申请创建: sitterId={}, amount={}, remainBalance={}", sitterId, dto.getAmount(), sitter.getWalletBalance());
    }

    @Override
    public List<WithdrawalVO> listBySitter(Long sitterId) {
        List<SitterWithdrawal> list = withdrawalMapper.selectList(
                new LambdaQueryWrapper<SitterWithdrawal>()
                        .eq(SitterWithdrawal::getSitterId, sitterId)
                        .orderByDesc(SitterWithdrawal::getCreatedAt));
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    private WithdrawalVO toVO(SitterWithdrawal w) {
        WithdrawalVO vo = new WithdrawalVO();
        vo.setId(w.getId());
        vo.setAmount(w.getAmount());
        vo.setStatus(w.getStatus());
        vo.setCreatedAt(w.getCreatedAt());
        vo.setCompletedAt(w.getCompletedAt());
        return vo;
    }
}
