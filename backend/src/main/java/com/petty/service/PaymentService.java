package com.petty.service;

import com.petty.dto.PaymentInitDTO;
import com.petty.dto.RefundDTO;
import com.petty.vo.PaymentResultVO;
import com.petty.vo.PaymentVO;

public interface PaymentService {
    PaymentResultVO initiatePayment(Long ownerId, PaymentInitDTO dto);
    PaymentVO getByOrderId(Long orderId);
    void requestRefund(Long ownerId, RefundDTO dto);
    void capturePayment(Long orderId);
}
