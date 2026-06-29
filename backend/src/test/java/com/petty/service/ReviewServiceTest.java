package com.petty.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petty.common.exception.BusinessException;
import com.petty.dto.ReviewCreateDTO;
import com.petty.entity.*;
import com.petty.enums.OrderStatus;
import com.petty.mapper.*;
import com.petty.service.impl.ReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewService - 评价服务测试")
class ReviewServiceTest {

    @Mock private ReviewMapper reviewMapper;
    @Mock private ServiceOrderMapper orderMapper;
    @Mock private OwnerMapper ownerMapper;
    @Mock private SitterMapper sitterMapper;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private ServiceOrder confirmedOrder;
    private Sitter sitter;

    @BeforeEach
    void setUp() {
        confirmedOrder = new ServiceOrder();
        confirmedOrder.setId(1L);
        confirmedOrder.setOwnerId(1L);
        confirmedOrder.setSitterId(1L);
        confirmedOrder.setStatus(OrderStatus.OWNER_CONFIRMED.name());

        sitter = new Sitter();
        sitter.setId(1L);
        sitter.setRating(new BigDecimal("4.5"));
        sitter.setTotalReviews(10);
        sitter.setStatus("ACTIVE");
    }

    @Test
    @DisplayName("正常评价 - 成功创建")
    void createReview_success() {
        when(orderMapper.selectById(1L)).thenReturn(confirmedOrder);
        when(reviewMapper.selectCount(any())).thenReturn(0L);
        when(sitterMapper.selectById(1L)).thenReturn(sitter);

        Review existingReview = new Review();
        existingReview.setRating(new BigDecimal("5.0"));
        when(reviewMapper.selectList(any())).thenReturn(List.of(existingReview));

        ReviewCreateDTO dto = new ReviewCreateDTO();
        dto.setOrderId(1L);
        dto.setRating(new BigDecimal("5.0"));
        dto.setContent("非常好");

        reviewService.createReview(1L, "OWNER", dto);

        verify(reviewMapper).insert(any(Review.class));
        verify(sitterMapper).updateById(any(Sitter.class));
    }

    @Test
    @DisplayName("订单未完成不能评价")
    void createReview_orderNotCompleted_throws() {
        ServiceOrder pendingOrder = new ServiceOrder();
        pendingOrder.setId(2L);
        pendingOrder.setStatus(OrderStatus.IN_SERVICE.name());
        when(orderMapper.selectById(2L)).thenReturn(pendingOrder);

        ReviewCreateDTO dto = new ReviewCreateDTO();
        dto.setOrderId(2L);
        dto.setRating(new BigDecimal("5.0"));
        dto.setContent("好");

        assertThatThrownBy(() -> reviewService.createReview(1L, "OWNER", dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("订单未完成");
    }

    @Test
    @DisplayName("重复评价 - 抛异常")
    void createReview_duplicate_throws() {
        when(orderMapper.selectById(1L)).thenReturn(confirmedOrder);
        when(reviewMapper.selectCount(any())).thenReturn(1L);

        ReviewCreateDTO dto = new ReviewCreateDTO();
        dto.setOrderId(1L);
        dto.setRating(new BigDecimal("4.0"));
        dto.setContent("重复");

        assertThatThrownBy(() -> reviewService.createReview(1L, "OWNER", dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("已评价过");
    }

    @Test
    @DisplayName("评分低于 3.0 时喂养师被 BANNED")
    void createReview_lowRating_sitterBanned() {
        when(orderMapper.selectById(1L)).thenReturn(confirmedOrder);
        when(reviewMapper.selectCount(any())).thenReturn(0L);

        Review lowReview = new Review();
        lowReview.setRating(new BigDecimal("2.0"));
        when(reviewMapper.selectList(any())).thenReturn(List.of(lowReview));
        when(sitterMapper.selectById(1L)).thenReturn(sitter);

        ReviewCreateDTO dto = new ReviewCreateDTO();
        dto.setOrderId(1L);
        dto.setRating(new BigDecimal("2.0"));
        dto.setContent("很差");

        reviewService.createReview(1L, "OWNER", dto);

        verify(sitterMapper).updateById(argThat((Sitter s) -> "BANNED".equals(s.getStatus())));
    }

    @Test
    @DisplayName("评分 3.0-3.5 时喂养师被 SUSPENDED")
    void createReview_marginalRating_sitterSuspended() {
        when(orderMapper.selectById(1L)).thenReturn(confirmedOrder);
        when(reviewMapper.selectCount(any())).thenReturn(0L);

        Review marginalReview = new Review();
        marginalReview.setRating(new BigDecimal("3.2"));
        when(reviewMapper.selectList(any())).thenReturn(List.of(marginalReview));
        when(sitterMapper.selectById(1L)).thenReturn(sitter);

        ReviewCreateDTO dto = new ReviewCreateDTO();
        dto.setOrderId(1L);
        dto.setRating(new BigDecimal("3.2"));
        dto.setContent("一般");

        reviewService.createReview(1L, "OWNER", dto);

        verify(sitterMapper).updateById(argThat((Sitter s) -> "SUSPENDED".equals(s.getStatus())));
    }
}
