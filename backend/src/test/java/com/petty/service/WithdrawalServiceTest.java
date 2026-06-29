package com.petty.service;

import com.petty.common.exception.BusinessException;
import com.petty.dto.WithdrawalDTO;
import com.petty.entity.Sitter;
import com.petty.entity.SitterWithdrawal;
import com.petty.mapper.SitterMapper;
import com.petty.mapper.SitterWithdrawalMapper;
import com.petty.service.impl.WithdrawalServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WithdrawalService - 提现服务测试")
class WithdrawalServiceTest {

    @Mock private SitterWithdrawalMapper withdrawalMapper;
    @Mock private SitterMapper sitterMapper;

    @InjectMocks
    private WithdrawalServiceImpl withdrawalService;

    private Sitter mockSitter;

    @BeforeEach
    void setUp() {
        mockSitter = new Sitter();
        mockSitter.setId(1L);
        mockSitter.setName("王大勇");
        mockSitter.setStatus("ACTIVE");
    }

    @Test
    @DisplayName("正常提现 - 首次当日提现成功")
    void requestWithdrawal_success() {
        when(sitterMapper.selectById(1L)).thenReturn(mockSitter);
        when(withdrawalMapper.selectCount(any())).thenReturn(0L);

        WithdrawalDTO dto = new WithdrawalDTO();
        dto.setAmount(new BigDecimal("100.00"));

        withdrawalService.requestWithdrawal(1L, dto);

        verify(withdrawalMapper).insert(any(SitterWithdrawal.class));
    }

    @Test
    @DisplayName("喂养师不存在 - 抛异常")
    void requestWithdrawal_sitterNotFound_throws() {
        when(sitterMapper.selectById(99L)).thenReturn(null);

        WithdrawalDTO dto = new WithdrawalDTO();
        dto.setAmount(new BigDecimal("100.00"));

        assertThatThrownBy(() -> withdrawalService.requestWithdrawal(99L, dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("喂养师不存在");
    }

    @Test
    @DisplayName("当日已提现 1 次 - 抛异常")
    void requestWithdrawal_dailyLimitReached_throws() {
        when(sitterMapper.selectById(1L)).thenReturn(mockSitter);
        when(withdrawalMapper.selectCount(any())).thenReturn(1L);

        WithdrawalDTO dto = new WithdrawalDTO();
        dto.setAmount(new BigDecimal("50.00"));

        assertThatThrownBy(() -> withdrawalService.requestWithdrawal(1L, dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("每日最多提现1次");
    }
}
