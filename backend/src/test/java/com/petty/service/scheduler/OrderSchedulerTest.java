package com.petty.service.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petty.entity.ServiceOrder;
import com.petty.enums.OrderStatus;
import com.petty.mapper.ServiceOrderMapper;
import com.petty.service.OrderService;
import com.petty.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderScheduler - 定时任务测试")
class OrderSchedulerTest {

    @Mock
    private ServiceOrderMapper orderMapper;

    @Mock
    private OrderService orderService;

    @Mock
    private ReviewService reviewService;

    @InjectMocks
    private OrderScheduler scheduler;

    @Nested
    @DisplayName("handleAcceptTimeout")
    class AcceptTimeoutTest {

        @Test
        @DisplayName("超时订单重置为待匹配")
        void resetsExpiredOrders() {
            ServiceOrder expired = new ServiceOrder();
            expired.setId(1L);
            expired.setOrderNo("T20260601001");
            expired.setStatus(OrderStatus.PENDING_ACCEPT.name());
            expired.setSitterId(5L);
            expired.setUpdatedAt(LocalDateTime.now().minusMinutes(35));

            when(orderMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(expired));
            when(orderMapper.updateById(any(ServiceOrder.class))).thenReturn(1);

            scheduler.handleAcceptTimeout();

            verify(orderMapper).updateById(argThat((ServiceOrder order) ->
                    order.getStatus().equals(OrderStatus.PENDING_MATCH.name())
                            && order.getSitterId() == null));
        }

        @Test
        @DisplayName("无超时订单时不操作")
        void noExpiredOrders_noop() {
            when(orderMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.emptyList());

            scheduler.handleAcceptTimeout();

            verify(orderMapper, never()).updateById(any(ServiceOrder.class));
        }

        @Test
        @DisplayName("单笔异常不影响其他订单")
        void exceptionIsolation() {
            ServiceOrder order1 = new ServiceOrder();
            order1.setId(1L);
            order1.setOrderNo("T001");
            order1.setStatus(OrderStatus.PENDING_ACCEPT.name());

            ServiceOrder order2 = new ServiceOrder();
            order2.setId(2L);
            order2.setOrderNo("T002");
            order2.setStatus(OrderStatus.PENDING_ACCEPT.name());

            when(orderMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(order1, order2));
            when(orderMapper.updateById(eq(order1))).thenThrow(new RuntimeException("DB error"));
            when(orderMapper.updateById(eq(order2))).thenReturn(1);

            scheduler.handleAcceptTimeout();

            verify(orderMapper, times(2)).updateById(any(ServiceOrder.class));
        }
    }

    @Nested
    @DisplayName("handleAutoConfirm")
    class AutoConfirmTest {

        @Test
        @DisplayName("超时订单调用 confirmOrder 完整结算")
        void callsConfirmOrderForSettlement() {
            ServiceOrder order = new ServiceOrder();
            order.setId(10L);
            order.setOrderNo("T20260601010");
            order.setOwnerId(1L);
            order.setStatus(OrderStatus.SERVICE_COMPLETED.name());
            order.setUpdatedAt(LocalDateTime.now().minusHours(25));

            when(orderMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(order));

            scheduler.handleAutoConfirm();

            verify(orderService).confirmOrder(10L, 1L);
        }

        @Test
        @DisplayName("无待确认订单时不操作")
        void noUnconfirmed_noop() {
            when(orderMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.emptyList());

            scheduler.handleAutoConfirm();

            verify(orderService, never()).confirmOrder(any(), any());
        }

        @Test
        @DisplayName("confirmOrder 异常不影响其他订单")
        void exceptionIsolation() {
            ServiceOrder order1 = new ServiceOrder();
            order1.setId(1L);
            order1.setOrderNo("T001");
            order1.setOwnerId(1L);
            order1.setStatus(OrderStatus.SERVICE_COMPLETED.name());

            ServiceOrder order2 = new ServiceOrder();
            order2.setId(2L);
            order2.setOrderNo("T002");
            order2.setOwnerId(2L);
            order2.setStatus(OrderStatus.SERVICE_COMPLETED.name());

            when(orderMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(order1, order2));
            doThrow(new RuntimeException("Settlement error")).when(orderService).confirmOrder(1L, 1L);

            scheduler.handleAutoConfirm();

            verify(orderService).confirmOrder(1L, 1L);
            verify(orderService).confirmOrder(2L, 2L);
        }
    }

    @Nested
    @DisplayName("handleAutoReview")
    class AutoReviewTest {

        @Test
        @DisplayName("超时未评价订单生成自动好评")
        void createsAutoReview() {
            ServiceOrder order = new ServiceOrder();
            order.setId(20L);
            order.setOrderNo("T20260601020");
            order.setOwnerId(1L);
            order.setSitterId(2L);
            order.setStatus(OrderStatus.OWNER_CONFIRMED.name());
            order.setUpdatedAt(LocalDateTime.now().minusHours(73));

            when(orderMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(order));

            scheduler.handleAutoReview();

            verify(reviewService).createAutoReview(20L, 1L, 2L);
        }

        @Test
        @DisplayName("无待评价订单时不操作")
        void noUnreviewed_noop() {
            when(orderMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.emptyList());

            scheduler.handleAutoReview();

            verify(reviewService, never()).createAutoReview(any(), any(), any());
        }

        @Test
        @DisplayName("已有评价的订单异常不影响其他")
        void exceptionIsolation() {
            ServiceOrder order1 = new ServiceOrder();
            order1.setId(1L);
            order1.setOrderNo("T001");
            order1.setOwnerId(1L);
            order1.setSitterId(2L);
            order1.setStatus(OrderStatus.OWNER_CONFIRMED.name());

            ServiceOrder order2 = new ServiceOrder();
            order2.setId(2L);
            order2.setOrderNo("T002");
            order2.setOwnerId(3L);
            order2.setSitterId(4L);
            order2.setStatus(OrderStatus.OWNER_CONFIRMED.name());

            when(orderMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(order1, order2));
            doThrow(new RuntimeException("Already reviewed")).when(reviewService).createAutoReview(1L, 1L, 2L);

            scheduler.handleAutoReview();

            verify(reviewService).createAutoReview(1L, 1L, 2L);
            verify(reviewService).createAutoReview(2L, 3L, 4L);
        }
    }
}
