package com.petty.service;

import com.petty.dto.*;
import com.petty.entity.ServiceOrder;
import com.petty.vo.OrderDetailVO;
import com.petty.vo.OrderVO;
import com.petty.vo.ServiceLogVO;

import java.util.List;

public interface OrderService {

    OrderVO createOrder(Long ownerId, OrderCreateDTO dto);

    OrderDetailVO getOrderDetail(Long orderId);

    List<OrderVO> listOrders(String status, Long ownerId, Long sitterId);

    void acceptOrder(Long orderId, Long sitterId);

    void rejectOrder(Long orderId, Long sitterId, String reason);

    void checkIn(Long orderId, Long sitterId, CheckInDTO dto);

    void checkOut(Long orderId, Long sitterId, CheckOutDTO dto);

    void confirmOrder(Long orderId, Long ownerId);

    void cancelOrder(Long orderId, Long userId, String reason);

    List<ServiceLogVO> getServiceLogs(Long orderId);

    void addServiceLog(Long orderId, Long sitterId, ServiceLogCreateDTO dto);
}
