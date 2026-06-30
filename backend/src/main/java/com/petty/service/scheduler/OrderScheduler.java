package com.petty.service.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petty.entity.ServiceOrder;
import com.petty.enums.OrderStatus;
import com.petty.mapper.ServiceOrderMapper;
import com.petty.service.OrderService;
import com.petty.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单相关定时任务:
 * 1. 接单超时(30分钟)重新匹配
 * 2. 服务完成24h后自动确认
 * 3. 确认后72h无评价自动默认5星好评
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderScheduler {

    private final ServiceOrderMapper orderMapper;
    private final OrderService orderService;
    private final ReviewService reviewService;

    /**
     * 每5分钟检查: 待接单超过30分钟的订单，重置为待匹配状态
     */
    @Scheduled(fixedDelay = 300_000, initialDelay = 60_000)
    public void handleAcceptTimeout() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(30);
        List<ServiceOrder> expiredOrders = orderMapper.selectList(
                new LambdaQueryWrapper<ServiceOrder>()
                        .eq(ServiceOrder::getStatus, OrderStatus.PENDING_ACCEPT.name())
                        .lt(ServiceOrder::getUpdatedAt, threshold));

        for (ServiceOrder order : expiredOrders) {
            try {
                order.setStatus(OrderStatus.PENDING_MATCH.name());
                order.setSitterId(null);
                order.setUpdatedAt(LocalDateTime.now());
                orderMapper.updateById(order);
                log.info("订单 {} 接单超时，已重置为待匹配", order.getOrderNo());
            } catch (Exception e) {
                log.error("处理接单超时失败: orderId={}", order.getId(), e);
            }
        }
        if (!expiredOrders.isEmpty()) {
            log.info("接单超时处理完成，共 {} 单", expiredOrders.size());
        }
    }

    /**
     * 每30分钟检查: 服务完成超过24小时未确认的订单，自动确认
     * 必须通过 orderService.confirmOrder() 走完整结算流程
     */
    @Scheduled(fixedDelay = 1_800_000, initialDelay = 120_000)
    public void handleAutoConfirm() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(24);
        List<ServiceOrder> unconfirmed = orderMapper.selectList(
                new LambdaQueryWrapper<ServiceOrder>()
                        .eq(ServiceOrder::getStatus, OrderStatus.SERVICE_COMPLETED.name())
                        .lt(ServiceOrder::getUpdatedAt, threshold));

        for (ServiceOrder order : unconfirmed) {
            try {
                orderService.confirmOrder(order.getId(), order.getOwnerId());
                log.info("订单 {} 超时自动确认（含结算）", order.getOrderNo());
            } catch (Exception e) {
                log.error("自动确认失败: orderId={}", order.getId(), e);
            }
        }
        if (!unconfirmed.isEmpty()) {
            log.info("自动确认处理完成，共 {} 单", unconfirmed.size());
        }
    }

    /**
     * 每小时检查: 确认超过72小时未评价的订单，自动生成5星好评
     */
    @Scheduled(fixedDelay = 3_600_000, initialDelay = 180_000)
    public void handleAutoReview() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(72);
        List<ServiceOrder> unreviewedOrders = orderMapper.selectList(
                new LambdaQueryWrapper<ServiceOrder>()
                        .eq(ServiceOrder::getStatus, OrderStatus.OWNER_CONFIRMED.name())
                        .lt(ServiceOrder::getUpdatedAt, threshold));

        for (ServiceOrder order : unreviewedOrders) {
            try {
                reviewService.createAutoReview(order.getId(), order.getOwnerId(), order.getSitterId());
                log.info("订单 {} 超时自动评价(5星)", order.getOrderNo());
            } catch (Exception e) {
                log.error("自动评价失败: orderId={}", order.getId(), e);
            }
        }
        if (!unreviewedOrders.isEmpty()) {
            log.info("自动评价处理完成，共 {} 单", unreviewedOrders.size());
        }
    }
}
