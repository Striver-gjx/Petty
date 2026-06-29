package com.petty.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petty.common.exception.BusinessException;
import com.petty.dto.*;
import com.petty.entity.*;
import com.petty.enums.OrderStatus;
import com.petty.mapper.*;
import com.petty.service.OrderService;
import com.petty.service.PaymentService;
import com.petty.service.matching.MatchingEngine;
import com.petty.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final ServiceOrderMapper orderMapper;
    private final OwnerMapper ownerMapper;
    private final PetMapper petMapper;
    private final SitterMapper sitterMapper;
    private final ServiceTypeMapper serviceTypeMapper;
    private final ServiceLogMapper serviceLogMapper;
    private final OrderPetMapper orderPetMapper;
    private final MatchingEngine matchingEngine;
    private final PaymentService paymentService;

    private static final BigDecimal GPS_CHECK_IN_THRESHOLD_KM = new BigDecimal("0.2");
    private static final BigDecimal GPS_CHECK_OUT_THRESHOLD_KM = new BigDecimal("0.5");

    @Override
    @Transactional
    public OrderVO createOrder(Long ownerId, OrderCreateDTO dto) {
        Owner owner = ownerMapper.selectById(ownerId);
        if (owner == null) throw new BusinessException("宠物主人不存在");

        ServiceType serviceType = serviceTypeMapper.selectById(dto.getServiceTypeId());
        if (serviceType == null) throw new BusinessException("服务类型不存在");

        List<Pet> pets = petMapper.selectBatchIds(dto.getPetIds());
        if (pets.isEmpty()) throw new BusinessException("宠物列表为空");
        if (pets.size() != dto.getPetIds().size()) {
            throw new BusinessException("部分宠物ID无效");
        }
        for (Pet pet : pets) {
            if (!ownerId.equals(pet.getOwnerId())) {
                throw new BusinessException("宠物 " + pet.getName() + " 不属于当前用户");
            }
        }

        BigDecimal serviceAmount = serviceType.getBasePrice();
        BigDecimal extraAmount = serviceType.getExtraPetPrice()
                .multiply(BigDecimal.valueOf(Math.max(0, pets.size() - 1)));
        BigDecimal totalAmount = serviceAmount.add(extraAmount);

        String address = dto.getServiceAddress() != null ? dto.getServiceAddress() : owner.getAddress();
        BigDecimal lat = dto.getLatitude() != null ? dto.getLatitude() : owner.getLatitude();
        BigDecimal lng = dto.getLongitude() != null ? dto.getLongitude() : owner.getLongitude();

        ServiceOrder order = new ServiceOrder();
        order.setOrderNo("ORD" + System.currentTimeMillis());
        order.setOwnerId(ownerId);
        order.setServiceTypeId(dto.getServiceTypeId());
        order.setServiceAddress(address);
        order.setServiceLatitude(lat);
        order.setServiceLongitude(lng);
        order.setScheduledDate(LocalDate.parse(dto.getScheduledDate()));
        order.setScheduledStartTime(LocalTime.parse(dto.getScheduledStartTime(), DateTimeFormatter.ofPattern("HH:mm")));
        order.setScheduledEndTime(LocalTime.parse(dto.getScheduledEndTime(), DateTimeFormatter.ofPattern("HH:mm")));
        order.setPetCount(pets.size());
        order.setServiceAmount(serviceAmount);
        order.setExtraAmount(extraAmount);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setTotalAmount(totalAmount);
        order.setRemark(dto.getRemark());
        order.setPaymentStatus("UNPAID");
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        if (dto.getPreferredSitterId() != null) {
            Sitter preferred = sitterMapper.selectById(dto.getPreferredSitterId());
            if (preferred != null && "ACTIVE".equals(preferred.getStatus())) {
                order.setSitterId(dto.getPreferredSitterId());
                order.setStatus(OrderStatus.PENDING_ACCEPT.name());
            } else {
                order.setStatus(OrderStatus.PENDING_MATCH.name());
            }
        } else {
            order.setStatus(OrderStatus.PENDING_MATCH.name());
            if (lat != null && lng != null) {
                tryAutoMatch(order, pets, lat, lng);
            }
        }

        orderMapper.insert(order);

        for (Pet pet : pets) {
            OrderPet op = new OrderPet();
            op.setOrderId(order.getId());
            op.setPetId(pet.getId());
            orderPetMapper.insert(op);
        }

        log.info("订单创建成功: {}, 状态: {}", order.getOrderNo(), order.getStatus());
        return toVO(order);
    }

    @Override
    public OrderDetailVO getOrderDetail(Long orderId) {
        ServiceOrder order = getOrderOrThrow(orderId);
        OrderDetailVO vo = new OrderDetailVO();
        copyToVO(order, vo);
        vo.setServiceAddress(order.getServiceAddress());
        vo.setRemark(order.getRemark());
        vo.setActualStartTime(order.getActualStartTime());
        vo.setActualEndTime(order.getActualEndTime());

        if (order.getSitterId() != null) {
            Sitter sitter = sitterMapper.selectById(order.getSitterId());
            if (sitter != null) vo.setSitterPhone(sitter.getPhone());
        }

        List<ServiceLog> logs = serviceLogMapper.selectList(
                new LambdaQueryWrapper<ServiceLog>()
                        .eq(ServiceLog::getOrderId, orderId)
                        .orderByAsc(ServiceLog::getCreatedAt));
        vo.setServiceLogs(logs.stream().map(this::toLogVO).collect(Collectors.toList()));

        List<OrderPet> orderPets = orderPetMapper.selectList(
                new LambdaQueryWrapper<OrderPet>().eq(OrderPet::getOrderId, orderId));
        List<OrderPetVO> petVOs = orderPets.stream().map(op -> {
            OrderPetVO pvo = new OrderPetVO();
            pvo.setPetId(op.getPetId());
            pvo.setSpecialNotes(op.getSpecialNotes());
            Pet pet = petMapper.selectById(op.getPetId());
            if (pet != null) {
                pvo.setPetName(pet.getName());
                pvo.setSpecies(pet.getSpecies());
                pvo.setAvatarUrl(pet.getAvatarUrl());
            }
            return pvo;
        }).collect(Collectors.toList());
        vo.setPets(petVOs);
        return vo;
    }

    @Override
    public List<OrderVO> listOrders(String status, Long ownerId, Long sitterId) {
        LambdaQueryWrapper<ServiceOrder> w = new LambdaQueryWrapper<>();
        if (status != null) w.eq(ServiceOrder::getStatus, status);
        if (ownerId != null) w.eq(ServiceOrder::getOwnerId, ownerId);
        if (sitterId != null) w.eq(ServiceOrder::getSitterId, sitterId);
        w.orderByDesc(ServiceOrder::getCreatedAt);
        return orderMapper.selectList(w).stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void acceptOrder(Long orderId, Long sitterId) {
        ServiceOrder order = getOrderOrThrow(orderId);
        assertStatus(order, OrderStatus.PENDING_ACCEPT);
        if (!sitterId.equals(order.getSitterId())) {
            throw new BusinessException("非指派喂养师，无法接单");
        }
        order.setStatus(OrderStatus.ACCEPTED.name());
        order.setUpdatedAt(LocalDateTime.now());
        int rows = orderMapper.updateById(order);
        if (rows == 0) throw new BusinessException("操作冲突，请刷新后重试");
        log.info("喂养师 {} 接单: {}", sitterId, order.getOrderNo());
    }

    @Override
    @Transactional
    public void rejectOrder(Long orderId, Long sitterId, String reason) {
        ServiceOrder order = getOrderOrThrow(orderId);
        assertStatus(order, OrderStatus.PENDING_ACCEPT);
        order.setStatus(OrderStatus.PENDING_MATCH.name());
        order.setSitterId(null);
        order.setCancelReason(reason);
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        log.info("喂养师 {} 拒单: {}, 原因: {}", sitterId, order.getOrderNo(), reason);
    }

    @Override
    @Transactional
    public void checkIn(Long orderId, Long sitterId, CheckInDTO dto) {
        ServiceOrder order = getOrderOrThrow(orderId);
        if (!sitterId.equals(order.getSitterId())) {
            throw new BusinessException("非指派喂养师，无法操作");
        }
        OrderStatus current = OrderStatus.valueOf(order.getStatus());
        if (current != OrderStatus.ACCEPTED && current != OrderStatus.SITTER_EN_ROUTE) {
            throw new BusinessException("当前状态不允许打卡: " + current.getLabel());
        }

        validateGps(dto.getLatitude(), dto.getLongitude(),
                order.getServiceLatitude(), order.getServiceLongitude(),
                GPS_CHECK_IN_THRESHOLD_KM, "到达打卡");

        order.setStatus(OrderStatus.IN_SERVICE.name());
        order.setActualStartTime(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        ServiceLog logEntry = new ServiceLog();
        logEntry.setOrderId(orderId);
        logEntry.setSitterId(sitterId);
        logEntry.setLogType("CHECK_IN");
        logEntry.setDescription("到达打卡");
        logEntry.setPhotoUrls(dto.getPhotoUrl());
        logEntry.setGpsLatitude(dto.getLatitude());
        logEntry.setGpsLongitude(dto.getLongitude());
        logEntry.setCreatedAt(LocalDateTime.now());
        serviceLogMapper.insert(logEntry);

        log.info("喂养师 {} 到达打卡: {}", sitterId, order.getOrderNo());
    }

    @Override
    @Transactional
    public void checkOut(Long orderId, Long sitterId, CheckOutDTO dto) {
        ServiceOrder order = getOrderOrThrow(orderId);
        if (!sitterId.equals(order.getSitterId())) {
            throw new BusinessException("非指派喂养师，无法操作");
        }
        assertStatus(order, OrderStatus.IN_SERVICE);

        validateGps(dto.getLatitude(), dto.getLongitude(),
                order.getServiceLatitude(), order.getServiceLongitude(),
                GPS_CHECK_OUT_THRESHOLD_KM, "离开打卡");

        order.setStatus(OrderStatus.SERVICE_COMPLETED.name());
        order.setActualEndTime(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        ServiceLog logEntry = new ServiceLog();
        logEntry.setOrderId(orderId);
        logEntry.setSitterId(sitterId);
        logEntry.setLogType("CHECK_OUT");
        logEntry.setDescription(dto.getServiceReport());
        logEntry.setPhotoUrls(dto.getPhotoUrl());
        logEntry.setGpsLatitude(dto.getLatitude());
        logEntry.setGpsLongitude(dto.getLongitude());
        logEntry.setCreatedAt(LocalDateTime.now());
        serviceLogMapper.insert(logEntry);

        log.info("喂养师 {} 完成服务: {}", sitterId, order.getOrderNo());
    }

    @Override
    @Transactional
    public void confirmOrder(Long orderId, Long ownerId) {
        ServiceOrder order = getOrderOrThrow(orderId);
        assertStatus(order, OrderStatus.SERVICE_COMPLETED);
        if (!ownerId.equals(order.getOwnerId())) {
            throw new BusinessException("无权确认此订单");
        }

        order.setStatus(OrderStatus.OWNER_CONFIRMED.name());
        order.setPaymentStatus("SETTLED");
        order.setUpdatedAt(LocalDateTime.now());

        BigDecimal commission = calculateCommission(order);
        order.setPlatformCommission(commission);
        order.setSitterIncome(order.getTotalAmount().subtract(commission));

        orderMapper.updateById(order);
        
        paymentService.capturePayment(orderId);
        
        log.info("主人 {} 确认服务: {}", ownerId, order.getOrderNo());
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, Long userId, String reason) {
        ServiceOrder order = getOrderOrThrow(orderId);
        OrderStatus current = OrderStatus.valueOf(order.getStatus());
        if (!current.isCancellable()) {
            throw new BusinessException("当前状态不允许取消: " + current.getLabel());
        }

        if (!userId.equals(order.getOwnerId()) && !userId.equals(order.getSitterId())) {
            throw new BusinessException("无权取消此订单");
        }

        boolean isSitterCancel = userId.equals(order.getSitterId());
        BigDecimal refundRate = isSitterCancel ? BigDecimal.ONE : calculateRefundRate(order);

        order.setStatus(OrderStatus.CANCELLED.name());
        order.setCancelReason(reason);
        order.setCancelBy(isSitterCancel ? "SITTER" : "OWNER");
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        paymentService.processRefund(orderId, refundRate, reason);

        log.info("订单取消: {}, 退款比例: {}%, 原因: {}", order.getOrderNo(),
                refundRate.multiply(BigDecimal.valueOf(100)), reason);
    }

    @Override
    public List<ServiceLogVO> getServiceLogs(Long orderId) {
        List<ServiceLog> logs = serviceLogMapper.selectList(
                new LambdaQueryWrapper<ServiceLog>()
                        .eq(ServiceLog::getOrderId, orderId)
                        .orderByAsc(ServiceLog::getCreatedAt));
        return logs.stream().map(this::toLogVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addServiceLog(Long orderId, Long sitterId, ServiceLogCreateDTO dto) {
        ServiceOrder order = getOrderOrThrow(orderId);
        if (!sitterId.equals(order.getSitterId())) {
            throw new BusinessException("非指派喂养师，无法操作");
        }
        if (!OrderStatus.IN_SERVICE.name().equals(order.getStatus())) {
            throw new BusinessException("只有服务中的订单才能添加记录");
        }

        ServiceLog logEntry = new ServiceLog();
        logEntry.setOrderId(orderId);
        logEntry.setSitterId(sitterId);
        logEntry.setLogType(dto.getLogType());
        logEntry.setDescription(dto.getDescription());
        logEntry.setPhotoUrls(dto.getPhotoUrls() != null ? String.join(",", dto.getPhotoUrls()) : null);
        logEntry.setVideoUrl(dto.getVideoUrl());
        logEntry.setGpsLatitude(dto.getLatitude());
        logEntry.setGpsLongitude(dto.getLongitude());
        logEntry.setPetStatus(dto.getPetStatus());
        logEntry.setCreatedAt(LocalDateTime.now());
        serviceLogMapper.insert(logEntry);
    }

    // --- Private helpers ---

    private void tryAutoMatch(ServiceOrder order, List<Pet> pets, BigDecimal lat, BigDecimal lng) {
        List<Sitter> active = sitterMapper.selectList(
                new LambdaQueryWrapper<Sitter>().eq(Sitter::getStatus, "ACTIVE"));

        List<Sitter> inRange = matchingEngine.filterByDistance(active, lat, lng);

        String species = pets.get(0).getSpecies();
        List<Sitter> speciesMatch = matchingEngine.filterBySpecies(inRange, species);

        if (!speciesMatch.isEmpty()) {
            List<MatchingEngine.ScoredSitter> ranked = matchingEngine.rank(speciesMatch, lat, lng);
            if (!ranked.isEmpty()) {
                order.setSitterId(ranked.get(0).sitter().getId());
                order.setStatus(OrderStatus.PENDING_ACCEPT.name());
                log.info("自动匹配喂养师: {}, 分数: {}", ranked.get(0).sitter().getName(), String.format("%.2f", ranked.get(0).score()));
            }
        }
    }

    private BigDecimal calculateCommission(ServiceOrder order) {
        if (order.getSitterId() == null) return BigDecimal.ZERO;
        Sitter sitter = sitterMapper.selectById(order.getSitterId());
        if (sitter == null) return order.getTotalAmount().multiply(new BigDecimal("0.20"));

        int totalOrders = sitter.getTotalOrders() != null ? sitter.getTotalOrders() : 0;
        BigDecimal rate;
        if (totalOrders > 100) {
            rate = new BigDecimal("0.15");
        } else if (totalOrders >= 10) {
            rate = new BigDecimal("0.20");
        } else {
            rate = new BigDecimal("0.25");
        }
        return order.getTotalAmount().multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateRefundRate(ServiceOrder order) {
        LocalDateTime scheduledStart = LocalDateTime.of(order.getScheduledDate(), order.getScheduledStartTime());
        long hoursUntilService = ChronoUnit.HOURS.between(LocalDateTime.now(), scheduledStart);

        if (hoursUntilService >= 24) {
            return BigDecimal.ONE;
        } else if (hoursUntilService >= 2) {
            return new BigDecimal("0.80");
        } else {
            return new BigDecimal("0.50");
        }
    }

    private void validateGps(BigDecimal lat, BigDecimal lng, BigDecimal targetLat, BigDecimal targetLng,
                             BigDecimal thresholdKm, String action) {
        if (targetLat == null || targetLng == null) return;
        double dist = haversineKm(lat.doubleValue(), lng.doubleValue(),
                targetLat.doubleValue(), targetLng.doubleValue());
        if (dist > thresholdKm.doubleValue()) {
            throw new BusinessException(String.format("%s失败：GPS距离服务地址%.0f米，超过限制%.0f米",
                    action, dist * 1000, thresholdKm.doubleValue() * 1000));
        }
    }

    private double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private void assertStatus(ServiceOrder order, OrderStatus expected) {
        OrderStatus current = OrderStatus.valueOf(order.getStatus());
        if (current != expected) {
            throw new BusinessException(String.format("订单状态错误：期望[%s]，实际[%s]",
                    expected.getLabel(), current.getLabel()));
        }
    }

    private ServiceOrder getOrderOrThrow(Long orderId) {
        ServiceOrder order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException(404, "订单不存在");
        return order;
    }

    private OrderVO toVO(ServiceOrder order) {
        OrderVO vo = new OrderVO();
        copyToVO(order, vo);
        return vo;
    }

    private void copyToVO(ServiceOrder order, OrderVO vo) {
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setScheduledDate(order.getScheduledDate());
        vo.setScheduledStartTime(order.getScheduledStartTime());
        vo.setScheduledEndTime(order.getScheduledEndTime());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setStatus(order.getStatus());
        vo.setPetCount(order.getPetCount());
        vo.setCreatedAt(order.getCreatedAt());

        try {
            vo.setStatusLabel(OrderStatus.valueOf(order.getStatus()).getLabel());
        } catch (IllegalArgumentException e) {
            vo.setStatusLabel(order.getStatus());
        }

        if (order.getServiceTypeId() != null) {
            ServiceType st = serviceTypeMapper.selectById(order.getServiceTypeId());
            if (st != null) vo.setServiceTypeName(st.getName());
        }
        if (order.getSitterId() != null) {
            Sitter sitter = sitterMapper.selectById(order.getSitterId());
            if (sitter != null) {
                vo.setSitterName(sitter.getName());
                vo.setSitterAvatarUrl(sitter.getAvatarUrl());
            }
        }
        if (order.getOwnerId() != null) {
            Owner owner = ownerMapper.selectById(order.getOwnerId());
            if (owner != null) vo.setOwnerNickname(owner.getNickname());
        }
    }

    private ServiceLogVO toLogVO(ServiceLog log) {
        ServiceLogVO vo = new ServiceLogVO();
        vo.setId(log.getId());
        vo.setLogType(log.getLogType());
        vo.setDescription(log.getDescription());
        vo.setVideoUrl(log.getVideoUrl());
        vo.setPetStatus(log.getPetStatus());
        vo.setCreatedAt(log.getCreatedAt());
        if (log.getPhotoUrls() != null && !log.getPhotoUrls().isEmpty()) {
            vo.setPhotoUrls(List.of(log.getPhotoUrls().split(",")));
        }
        return vo;
    }
}
