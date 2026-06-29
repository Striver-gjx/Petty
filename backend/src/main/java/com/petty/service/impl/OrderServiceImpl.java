package com.petty.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petty.entity.ServiceOrder;
import com.petty.mapper.ServiceOrderMapper;
import com.petty.service.OrderService;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl extends ServiceImpl<ServiceOrderMapper, ServiceOrder> implements OrderService {
}
