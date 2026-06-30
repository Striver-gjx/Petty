package com.petty.controller;

import com.petty.common.result.Result;
import com.petty.common.security.UserContext;
import com.petty.dto.PaymentInitDTO;
import com.petty.dto.RefundDTO;
import com.petty.service.OrderService;
import com.petty.service.PaymentService;
import com.petty.vo.OrderDetailVO;
import com.petty.vo.PaymentResultVO;
import com.petty.vo.PaymentVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService;

    @GetMapping("/order/{orderId}")
    public Result<PaymentVO> getByOrder(@PathVariable Long orderId) {
        Long userId = UserContext.getUserId();
        String role = UserContext.getRole();
        if (!"ADMIN".equals(role)) {
            OrderDetailVO order = orderService.getOrderDetail(orderId);
            boolean isParticipant = ("OWNER".equals(role) && userId.equals(order.getOwnerId()))
                    || ("SITTER".equals(role) && userId.equals(order.getSitterId()));
            if (!isParticipant) {
                return Result.error(403, "无权查看该支付信息");
            }
        }
        return Result.success(paymentService.getByOrderId(orderId));
    }

    @PostMapping("/pay")
    public Result<PaymentResultVO> pay(@Valid @RequestBody PaymentInitDTO dto) {
        Long ownerId = UserContext.getUserId();
        return Result.success(paymentService.initiatePayment(ownerId, dto));
    }

    @PostMapping("/refund")
    public Result<Void> refund(@Valid @RequestBody RefundDTO dto) {
        Long ownerId = UserContext.getUserId();
        paymentService.requestRefund(ownerId, dto);
        return Result.success();
    }
}
