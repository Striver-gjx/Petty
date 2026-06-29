package com.petty.vo;

import lombok.Data;

@Data
public class PaymentResultVO {
    private String prepayId;
    private String paySign;
    private String timeStamp;
    private String nonceStr;
}
