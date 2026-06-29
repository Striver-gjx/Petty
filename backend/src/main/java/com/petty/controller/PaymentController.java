package com.petty.controller;

import com.petty.common.result.Result;
import com.petty.dto.PaymentInitDTO;
import com.petty.dto.RefundDTO;
import com.petty.service.PaymentService;
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

    @GetMapping("/order/{orderId}")
    public Result<PaymentVO> getByOrder(@PathVariable Long orderId) {
        return Result.success(paymentService.getByOrderId(orderId));
    }

    @PostMapping("/pay")
    public Result<PaymentResultVO> pay(
            @RequestParam(defaultValue = "1") Long ownerId,
            @Valid @RequestBody PaymentInitDTO dto) {
        return Result.success(paymentService.initiatePayment(ownerId, dto));
    }

    @PostMapping("/refund")
    public Result<Void> refund(
            @RequestParam(defaultValue = "1") Long ownerId,
            @Valid @RequestBody RefundDTO dto) {
        paymentService.requestRefund(ownerId, dto);
        return Result.success();
    }
}
