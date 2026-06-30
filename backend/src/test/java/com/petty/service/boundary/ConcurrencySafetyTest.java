package com.petty.service.boundary;

import com.petty.common.exception.BusinessException;
import com.petty.dto.PaymentInitDTO;
import com.petty.dto.ReviewCreateDTO;
import com.petty.entity.*;
import com.petty.enums.OrderStatus;
import com.petty.mapper.*;
import com.petty.service.PaymentService;
import com.petty.service.impl.OrderServiceImpl;
import com.petty.service.impl.PaymentServiceImpl;
import com.petty.service.impl.ReviewServiceImpl;
import com.petty.service.matching.MatchingEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("并发安全 - 重复操作防护")
class ConcurrencySafetyTest {

    @Nested
    @DisplayName("重复接单防护")
    class DuplicateAcceptTest {

        @Mock private ServiceOrderMapper orderMapper;
        @Mock private OwnerMapper ownerMapper;
        @Mock private PetMapper petMapper;
        @Mock private SitterMapper sitterMapper;
        @Mock private ServiceTypeMapper serviceTypeMapper;
        @Mock private ServiceLogMapper serviceLogMapper;
        @Mock private OrderPetMapper orderPetMapper;
        @Mock private SitterScheduleMapper sitterScheduleMapper;
        @Mock private MatchingEngine matchingEngine;
        @Mock private PaymentService paymentService;
        @Mock private com.petty.common.lock.DistributedLock distributedLock;

        @InjectMocks
        private OrderServiceImpl orderService;

        @Test
        @DisplayName("已接单的订单再次接单 - 状态异常")
        void accept_alreadyAccepted_throws() {
            org.mockito.Mockito.when(distributedLock.tryLock(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyLong())).thenReturn(true);
            ServiceOrder order = new ServiceOrder();
            order.setId(1L);
            order.setSitterId(1L);
            order.setStatus(OrderStatus.ACCEPTED.name());
            when(orderMapper.selectById(1L)).thenReturn(order);

            assertThatThrownBy(() -> orderService.acceptOrder(1L, 1L))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("已在服务中的订单不能再接单")
        void accept_inService_throws() {
            org.mockito.Mockito.when(distributedLock.tryLock(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyLong())).thenReturn(true);
            ServiceOrder order = new ServiceOrder();
            order.setId(1L);
            order.setSitterId(1L);
            order.setStatus(OrderStatus.IN_SERVICE.name());
            when(orderMapper.selectById(1L)).thenReturn(order);

            assertThatThrownBy(() -> orderService.acceptOrder(1L, 1L))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("重复支付防护")
    class DuplicatePaymentTest {

        @Mock private PaymentMapper paymentMapper;
        @Mock private ServiceOrderMapper orderMapper;

        @InjectMocks
        private PaymentServiceImpl paymentService;

        @Test
        @DisplayName("AUTHORIZED 状态的订单不能重复支付")
        void pay_alreadyAuthorized_throws() {
            ServiceOrder order = new ServiceOrder();
            order.setId(1L);
            order.setOwnerId(1L);
            order.setPaymentStatus("AUTHORIZED");
            order.setTotalAmount(new BigDecimal("49.00"));
            when(orderMapper.selectById(1L)).thenReturn(order);

            PaymentInitDTO dto = new PaymentInitDTO();
            dto.setOrderId(1L);
            dto.setPaymentMethod("WECHAT");

            assertThatThrownBy(() -> paymentService.initiatePayment(1L, dto))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("已支付");
        }

        @Test
        @DisplayName("PAID 状态的订单不能重复支付")
        void pay_alreadyPaid_throws() {
            ServiceOrder order = new ServiceOrder();
            order.setId(1L);
            order.setOwnerId(1L);
            order.setPaymentStatus("PAID");
            order.setTotalAmount(new BigDecimal("49.00"));
            when(orderMapper.selectById(1L)).thenReturn(order);

            PaymentInitDTO dto = new PaymentInitDTO();
            dto.setOrderId(1L);
            dto.setPaymentMethod("WECHAT");

            assertThatThrownBy(() -> paymentService.initiatePayment(1L, dto))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("已支付");
        }
    }

    @Nested
    @DisplayName("重复评价防护")
    class DuplicateReviewTest {

        @Mock private ReviewMapper reviewMapper;
        @Mock private ServiceOrderMapper orderMapper;
        @Mock private OwnerMapper ownerMapper;
        @Mock private SitterMapper sitterMapper;

        @InjectMocks
        private ReviewServiceImpl reviewService;

        @Test
        @DisplayName("同一用户同一订单不能重复评价")
        void review_duplicate_throws() {
            ServiceOrder order = new ServiceOrder();
            order.setId(1L);
            order.setOwnerId(1L);
            order.setSitterId(2L);
            order.setStatus(OrderStatus.OWNER_CONFIRMED.name());
            when(orderMapper.selectById(1L)).thenReturn(order);
            when(reviewMapper.selectCount(any())).thenReturn(1L);

            ReviewCreateDTO dto = new ReviewCreateDTO();
            dto.setOrderId(1L);
            dto.setRating(new BigDecimal("5.0"));

            assertThatThrownBy(() -> reviewService.createReview(1L, "OWNER", dto))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("已评价");
        }
    }
}
