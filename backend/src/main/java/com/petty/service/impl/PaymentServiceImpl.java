package com.petty.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petty.common.exception.BusinessException;
import com.petty.dto.PaymentInitDTO;
import com.petty.dto.RefundDTO;
import com.petty.entity.Payment;
import com.petty.entity.ServiceOrder;
import com.petty.mapper.PaymentMapper;
import com.petty.mapper.ServiceOrderMapper;
import com.petty.service.PaymentService;
import com.petty.vo.PaymentResultVO;
import com.petty.vo.PaymentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentMapper paymentMapper;
    private final ServiceOrderMapper orderMapper;

    @Override
    @Transactional
    public PaymentResultVO initiatePayment(Long ownerId, PaymentInitDTO dto) {
        ServiceOrder order = orderMapper.selectById(dto.getOrderId());
        if (order == null) throw new BusinessException("订单不存在");
        if (!ownerId.equals(order.getOwnerId())) throw new BusinessException("无权操作此订单");
        if ("PAID".equals(order.getPaymentStatus()) || "AUTHORIZED".equals(order.getPaymentStatus())) {
            throw new BusinessException("订单已支付");
        }

        Payment existing = getPaymentEntity(dto.getOrderId());
        if (existing != null && "AUTHORIZED".equals(existing.getStatus())) {
            throw new BusinessException("订单已完成预授权");
        }

        String outTradeNo = "PAY" + System.currentTimeMillis();
        String transactionNo = "TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        Payment payment = new Payment();
        payment.setOrderId(dto.getOrderId());
        payment.setOwnerId(ownerId);
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentMethod(dto.getPaymentMethod());
        payment.setStatus("AUTHORIZED");
        payment.setOutTradeNo(outTradeNo);
        payment.setTransactionNo(transactionNo);
        payment.setAuthorizedAt(LocalDateTime.now());
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        paymentMapper.insert(payment);

        order.setPaymentStatus("AUTHORIZED");
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        PaymentResultVO result = new PaymentResultVO();
        result.setPrepayId("prepay_" + outTradeNo);
        result.setPaySign("mock_sign_" + transactionNo);
        result.setTimeStamp(String.valueOf(System.currentTimeMillis() / 1000));
        result.setNonceStr(UUID.randomUUID().toString().replace("-", "").substring(0, 16));

        log.info("支付预授权完成: orderId={}, amount={}, method={}", dto.getOrderId(), order.getTotalAmount(), dto.getPaymentMethod());
        return result;
    }

    @Override
    public PaymentVO getByOrderId(Long orderId) {
        Payment payment = getPaymentEntity(orderId);
        if (payment == null) throw new BusinessException(404, "支付记录不存在");
        return toVO(payment);
    }

    @Override
    @Transactional
    public void requestRefund(Long ownerId, RefundDTO dto) {
        Payment payment = getPaymentEntity(dto.getOrderId());
        if (payment == null) throw new BusinessException("支付记录不存在");
        if (!ownerId.equals(payment.getOwnerId())) {
            throw new BusinessException("无权操作此订单的退款");
        }
        if (!"AUTHORIZED".equals(payment.getStatus()) && !"CAPTURED".equals(payment.getStatus())) {
            throw new BusinessException("当前支付状态不支持退款");
        }

        payment.setStatus("REFUNDED");
        payment.setRefundAmount(payment.getAmount());
        payment.setRefundReason(dto.getReason());
        payment.setRefundAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        paymentMapper.updateById(payment);

        ServiceOrder order = orderMapper.selectById(dto.getOrderId());
        if (order != null) {
            order.setPaymentStatus("REFUNDED");
            order.setUpdatedAt(LocalDateTime.now());
            orderMapper.updateById(order);
        }

        log.info("退款处理完成: orderId={}, amount={}", dto.getOrderId(), payment.getAmount());
    }

    @Override
    @Transactional
    public void processRefund(Long orderId, BigDecimal refundRate, String reason) {
        Payment payment = getPaymentEntity(orderId);
        if (payment == null) return;
        if (!"AUTHORIZED".equals(payment.getStatus()) && !"CAPTURED".equals(payment.getStatus())) return;

        BigDecimal refundAmount = payment.getAmount().multiply(refundRate).setScale(2, RoundingMode.HALF_UP);

        payment.setStatus("REFUNDED");
        payment.setRefundAmount(refundAmount);
        payment.setRefundReason(reason);
        payment.setRefundAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        paymentMapper.updateById(payment);

        ServiceOrder order = orderMapper.selectById(orderId);
        if (order != null) {
            order.setPaymentStatus("REFUNDED");
            order.setUpdatedAt(LocalDateTime.now());
            orderMapper.updateById(order);
        }

        log.info("取消退款: orderId={}, 退款金额={}, 退款比例={}%", orderId, refundAmount,
                refundRate.multiply(BigDecimal.valueOf(100)));
    }

    @Override
    @Transactional
    public void capturePayment(Long orderId) {
        Payment payment = getPaymentEntity(orderId);
        if (payment == null) {
            log.warn("订单 {} 无支付记录，跳过扣款", orderId);
            return;
        }
        if (!"AUTHORIZED".equals(payment.getStatus())) {
            log.warn("订单 {} 支付状态为 {}，无法扣款", orderId, payment.getStatus());
            if ("REFUNDED".equals(payment.getStatus())) {
                throw new BusinessException("订单已退款，无法确认");
            }
            return;
        }

        payment.setStatus("CAPTURED");
        payment.setCapturedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        paymentMapper.updateById(payment);

        log.info("支付确认扣款: orderId={}", orderId);
    }

    private Payment getPaymentEntity(Long orderId) {
        return paymentMapper.selectOne(
                new LambdaQueryWrapper<Payment>()
                        .eq(Payment::getOrderId, orderId)
                        .orderByDesc(Payment::getCreatedAt)
                        .last("LIMIT 1"));
    }

    private PaymentVO toVO(Payment payment) {
        PaymentVO vo = new PaymentVO();
        vo.setId(payment.getId());
        vo.setOrderId(payment.getOrderId());
        vo.setAmount(payment.getAmount());
        vo.setPaymentMethod(payment.getPaymentMethod());
        vo.setStatus(payment.getStatus());
        vo.setTransactionNo(payment.getTransactionNo());
        vo.setAuthorizedAt(payment.getAuthorizedAt());
        vo.setCapturedAt(payment.getCapturedAt());
        return vo;
    }
}
