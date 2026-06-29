package com.petty.service;

import com.petty.common.exception.BusinessException;
import com.petty.dto.PaymentInitDTO;
import com.petty.dto.RefundDTO;
import com.petty.entity.Payment;
import com.petty.entity.ServiceOrder;
import com.petty.mapper.PaymentMapper;
import com.petty.mapper.ServiceOrderMapper;
import com.petty.service.impl.PaymentServiceImpl;
import com.petty.vo.PaymentResultVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService - 支付服务测试")
class PaymentServiceTest {

    @Mock private PaymentMapper paymentMapper;
    @Mock private ServiceOrderMapper orderMapper;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private ServiceOrder mockOrder;

    @BeforeEach
    void setUp() {
        mockOrder = new ServiceOrder();
        mockOrder.setId(1L);
        mockOrder.setOwnerId(1L);
        mockOrder.setTotalAmount(new BigDecimal("49.00"));
        mockOrder.setPaymentStatus("UNPAID");
    }

    @Nested
    @DisplayName("initiatePayment - 发起支付")
    class InitiatePaymentTest {

        @Test
        @DisplayName("正常发起预授权")
        void initiate_success() {
            when(orderMapper.selectById(1L)).thenReturn(mockOrder);
            when(paymentMapper.selectOne(any())).thenReturn(null);

            PaymentInitDTO dto = new PaymentInitDTO();
            dto.setOrderId(1L);
            dto.setPaymentMethod("WECHAT");

            PaymentResultVO result = paymentService.initiatePayment(1L, dto);

            assertThat(result.getPrepayId()).startsWith("prepay_");
            assertThat(result.getTimeStamp()).isNotBlank();
            verify(paymentMapper).insert(any(Payment.class));
            verify(orderMapper).updateById(argThat((ServiceOrder o) ->
                    "AUTHORIZED".equals(o.getPaymentStatus())));
        }

        @Test
        @DisplayName("订单不存在 - 抛异常")
        void initiate_orderNotFound_throws() {
            when(orderMapper.selectById(99L)).thenReturn(null);

            PaymentInitDTO dto = new PaymentInitDTO();
            dto.setOrderId(99L);

            assertThatThrownBy(() -> paymentService.initiatePayment(1L, dto))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("订单不存在");
        }

        @Test
        @DisplayName("非本人订单 - 抛异常")
        void initiate_wrongOwner_throws() {
            when(orderMapper.selectById(1L)).thenReturn(mockOrder);

            PaymentInitDTO dto = new PaymentInitDTO();
            dto.setOrderId(1L);

            assertThatThrownBy(() -> paymentService.initiatePayment(999L, dto))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("无权操作");
        }

        @Test
        @DisplayName("已支付的订单不能重复支付")
        void initiate_alreadyPaid_throws() {
            mockOrder.setPaymentStatus("PAID");
            when(orderMapper.selectById(1L)).thenReturn(mockOrder);

            PaymentInitDTO dto = new PaymentInitDTO();
            dto.setOrderId(1L);

            assertThatThrownBy(() -> paymentService.initiatePayment(1L, dto))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("已支付");
        }
    }

    @Nested
    @DisplayName("capturePayment - 确认扣款")
    class CapturePaymentTest {

        @Test
        @DisplayName("AUTHORIZED 状态 - 扣款成功")
        void capture_authorized_success() {
            Payment authorized = new Payment();
            authorized.setId(1L);
            authorized.setOrderId(1L);
            authorized.setStatus("AUTHORIZED");
            when(paymentMapper.selectOne(any())).thenReturn(authorized);

            paymentService.capturePayment(1L);

            verify(paymentMapper).updateById(argThat((Payment p) ->
                    "CAPTURED".equals(p.getStatus())));
        }

        @Test
        @DisplayName("无支付记录 - 静默返回")
        void capture_noPayment_silentReturn() {
            when(paymentMapper.selectOne(any())).thenReturn(null);

            paymentService.capturePayment(1L);

            verify(paymentMapper, never()).updateById(any(Payment.class));
        }
    }

    @Nested
    @DisplayName("requestRefund - 退款")
    class RefundTest {

        @Test
        @DisplayName("AUTHORIZED 状态退款成功")
        void refund_authorized_success() {
            Payment payment = new Payment();
            payment.setId(1L);
            payment.setOrderId(1L);
            payment.setOwnerId(1L);
            payment.setAmount(new BigDecimal("49.00"));
            payment.setStatus("AUTHORIZED");
            when(paymentMapper.selectOne(any())).thenReturn(payment);
            when(orderMapper.selectById(1L)).thenReturn(mockOrder);

            RefundDTO dto = new RefundDTO();
            dto.setOrderId(1L);
            dto.setReason("主人取消");

            paymentService.requestRefund(1L, dto);

            verify(paymentMapper).updateById(argThat((Payment p) -> "REFUNDED".equals(p.getStatus())));
            verify(orderMapper).updateById(argThat((ServiceOrder o) -> "REFUNDED".equals(o.getPaymentStatus())));
        }

        @Test
        @DisplayName("已退款状态不能再退")
        void refund_alreadyRefunded_throws() {
            Payment payment = new Payment();
            payment.setOwnerId(1L);
            payment.setStatus("REFUNDED");
            when(paymentMapper.selectOne(any())).thenReturn(payment);

            RefundDTO dto = new RefundDTO();
            dto.setOrderId(1L);

            assertThatThrownBy(() -> paymentService.requestRefund(1L, dto))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("不支持退款");
        }
    }
}
