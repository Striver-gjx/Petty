package com.petty.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petty.common.exception.BusinessException;
import com.petty.dto.CheckInDTO;
import com.petty.dto.CheckOutDTO;
import com.petty.dto.OrderCreateDTO;
import com.petty.entity.*;
import com.petty.enums.OrderStatus;
import com.petty.mapper.*;
import com.petty.service.impl.OrderServiceImpl;
import com.petty.service.matching.MatchingEngine;
import com.petty.vo.OrderVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService - 订单服务测试")
class OrderServiceTest {

    @Mock private ServiceOrderMapper orderMapper;
    @Mock private OwnerMapper ownerMapper;
    @Mock private PetMapper petMapper;
    @Mock private SitterMapper sitterMapper;
    @Mock private ServiceTypeMapper serviceTypeMapper;
    @Mock private ServiceLogMapper serviceLogMapper;
    @Mock private MatchingEngine matchingEngine;
    @Mock private PaymentService paymentService;

    @InjectMocks
    private OrderServiceImpl orderService;

    @org.mockito.Captor
    private org.mockito.ArgumentCaptor<ServiceOrder> orderCaptor;

    private Owner mockOwner;
    private Pet mockPet;
    private ServiceType mockServiceType;
    private Sitter mockSitter;
    private ServiceOrder mockOrder;

    @BeforeEach
    void setUp() {
        mockOwner = new Owner();
        mockOwner.setId(1L);
        mockOwner.setNickname("张小花");
        mockOwner.setAddress("北京市朝阳区");
        mockOwner.setLatitude(new BigDecimal("39.9087"));
        mockOwner.setLongitude(new BigDecimal("116.4716"));

        mockPet = new Pet();
        mockPet.setId(1L);
        mockPet.setOwnerId(1L);
        mockPet.setName("小橘");
        mockPet.setSpecies("CAT");

        mockServiceType = new ServiceType();
        mockServiceType.setId(1L);
        mockServiceType.setName("上门喂养");
        mockServiceType.setBasePrice(new BigDecimal("49.00"));
        mockServiceType.setExtraPetPrice(new BigDecimal("20.00"));

        mockSitter = new Sitter();
        mockSitter.setId(1L);
        mockSitter.setName("王大勇");
        mockSitter.setStatus("ACTIVE");
        mockSitter.setTotalOrders(50);
        mockSitter.setRating(new BigDecimal("4.8"));

        mockOrder = new ServiceOrder();
        mockOrder.setId(1L);
        mockOrder.setOrderNo("ORD123");
        mockOrder.setOwnerId(1L);
        mockOrder.setSitterId(1L);
        mockOrder.setServiceTypeId(1L);
        mockOrder.setTotalAmount(new BigDecimal("49.00"));
        mockOrder.setScheduledDate(LocalDate.now().plusDays(2));
        mockOrder.setScheduledStartTime(LocalTime.of(10, 0));
        mockOrder.setScheduledEndTime(LocalTime.of(10, 30));
        mockOrder.setServiceLatitude(new BigDecimal("39.9087"));
        mockOrder.setServiceLongitude(new BigDecimal("116.4716"));
        mockOrder.setPetCount(1);
    }

    @Nested
    @DisplayName("createOrder - 创建订单")
    class CreateOrderTest {

        @Test
        @DisplayName("正常创建订单 - 单只宠物")
        void createOrder_singlePet_success() {
            when(ownerMapper.selectById(1L)).thenReturn(mockOwner);
            when(serviceTypeMapper.selectById(1L)).thenReturn(mockServiceType);
            when(petMapper.selectBatchIds(List.of(1L))).thenReturn(List.of(mockPet));
            when(sitterMapper.selectList(any())).thenReturn(List.of(mockSitter));
            when(matchingEngine.filterByDistance(any(), any(), any())).thenReturn(List.of(mockSitter));
            when(matchingEngine.filterBySpecies(any(), eq("CAT"))).thenReturn(List.of(mockSitter));
            when(matchingEngine.rank(any(), any(), any()))
                    .thenReturn(List.of(new MatchingEngine.ScoredSitter(mockSitter, 0.9, 1.0)));

            OrderCreateDTO dto = new OrderCreateDTO();
            dto.setServiceTypeId(1L);
            dto.setPetIds(List.of(1L));
            dto.setScheduledDate("2026-07-01");
            dto.setScheduledStartTime("10:00");
            dto.setScheduledEndTime("10:30");
            dto.setLatitude(new BigDecimal("39.9087"));
            dto.setLongitude(new BigDecimal("116.4716"));

            OrderVO result = orderService.createOrder(1L, dto);

            assertThat(result).isNotNull();
            assertThat(result.getTotalAmount()).isEqualByComparingTo("49.00");
            verify(orderMapper).insert(any(ServiceOrder.class));
        }

        @Test
        @DisplayName("多只宠物 - 额外加价")
        void createOrder_multiplePets_extraCharge() {
            Pet pet2 = new Pet();
            pet2.setId(2L);
            pet2.setSpecies("CAT");

            when(ownerMapper.selectById(1L)).thenReturn(mockOwner);
            when(serviceTypeMapper.selectById(1L)).thenReturn(mockServiceType);
            when(petMapper.selectBatchIds(List.of(1L, 2L))).thenReturn(List.of(mockPet, pet2));
            when(sitterMapper.selectList(any())).thenReturn(List.of(mockSitter));
            when(matchingEngine.filterByDistance(any(), any(), any())).thenReturn(List.of(mockSitter));
            when(matchingEngine.filterBySpecies(any(), eq("CAT"))).thenReturn(List.of(mockSitter));
            when(matchingEngine.rank(any(), any(), any()))
                    .thenReturn(List.of(new MatchingEngine.ScoredSitter(mockSitter, 0.9, 1.0)));

            OrderCreateDTO dto = new OrderCreateDTO();
            dto.setServiceTypeId(1L);
            dto.setPetIds(List.of(1L, 2L));
            dto.setScheduledDate("2026-07-01");
            dto.setScheduledStartTime("10:00");
            dto.setScheduledEndTime("10:30");
            dto.setLatitude(new BigDecimal("39.9087"));
            dto.setLongitude(new BigDecimal("116.4716"));

            OrderVO result = orderService.createOrder(1L, dto);

            assertThat(result.getTotalAmount()).isEqualByComparingTo("69.00");
        }

        @Test
        @DisplayName("宠物主人不存在 - 抛异常")
        void createOrder_ownerNotFound_throws() {
            when(ownerMapper.selectById(99L)).thenReturn(null);

            OrderCreateDTO dto = new OrderCreateDTO();
            dto.setServiceTypeId(1L);
            dto.setPetIds(List.of(1L));

            assertThatThrownBy(() -> orderService.createOrder(99L, dto))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("宠物主人不存在");
        }
    }

    @Nested
    @DisplayName("acceptOrder - 接单")
    class AcceptOrderTest {

        @Test
        @DisplayName("正确喂养师接单成功")
        void accept_correctSitter_success() {
            mockOrder.setStatus(OrderStatus.PENDING_ACCEPT.name());
            when(orderMapper.selectById(1L)).thenReturn(mockOrder);

            orderService.acceptOrder(1L, 1L);

            verify(orderMapper).updateById(argThat((ServiceOrder o) ->
                    OrderStatus.ACCEPTED.name().equals(o.getStatus())));
        }

        @Test
        @DisplayName("非指派喂养师接单 - 抛异常")
        void accept_wrongSitter_throws() {
            mockOrder.setStatus(OrderStatus.PENDING_ACCEPT.name());
            when(orderMapper.selectById(1L)).thenReturn(mockOrder);

            assertThatThrownBy(() -> orderService.acceptOrder(1L, 999L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("非指派喂养师");
        }

        @Test
        @DisplayName("非 PENDING_ACCEPT 状态接单 - 抛异常")
        void accept_wrongStatus_throws() {
            mockOrder.setStatus(OrderStatus.IN_SERVICE.name());
            when(orderMapper.selectById(1L)).thenReturn(mockOrder);

            assertThatThrownBy(() -> orderService.acceptOrder(1L, 1L))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("checkIn - 到达打卡")
    class CheckInTest {

        @Test
        @DisplayName("距离在 200m 内 - 打卡成功")
        void checkIn_withinRange_success() {
            mockOrder.setStatus(OrderStatus.ACCEPTED.name());
            when(orderMapper.selectById(1L)).thenReturn(mockOrder);

            CheckInDTO dto = new CheckInDTO();
            dto.setLatitude(new BigDecimal("39.9087"));
            dto.setLongitude(new BigDecimal("116.4716"));
            dto.setPhotoUrl("https://example.com/photo.jpg");

            orderService.checkIn(1L, 1L, dto);

            verify(orderMapper).updateById(argThat((ServiceOrder o) ->
                    OrderStatus.IN_SERVICE.name().equals(o.getStatus())));
            verify(serviceLogMapper).insert(any(ServiceLog.class));
        }

        @Test
        @DisplayName("距离超过 200m - 打卡失败")
        void checkIn_outOfRange_throws() {
            mockOrder.setStatus(OrderStatus.ACCEPTED.name());
            when(orderMapper.selectById(1L)).thenReturn(mockOrder);

            CheckInDTO dto = new CheckInDTO();
            dto.setLatitude(new BigDecimal("40.0000"));
            dto.setLongitude(new BigDecimal("117.0000"));
            dto.setPhotoUrl("https://example.com/photo.jpg");

            assertThatThrownBy(() -> orderService.checkIn(1L, 1L, dto))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("GPS距离");
        }
    }

    @Nested
    @DisplayName("checkOut - 离开打卡")
    class CheckOutTest {

        @Test
        @DisplayName("距离在 500m 内 - 离开打卡成功")
        void checkOut_withinRange_success() {
            mockOrder.setStatus(OrderStatus.IN_SERVICE.name());
            when(orderMapper.selectById(1L)).thenReturn(mockOrder);

            CheckOutDTO dto = new CheckOutDTO();
            dto.setLatitude(new BigDecimal("39.9090"));
            dto.setLongitude(new BigDecimal("116.4720"));
            dto.setPhotoUrl("https://example.com/leave.jpg");
            dto.setServiceReport("服务完成");

            orderService.checkOut(1L, 1L, dto);

            verify(orderMapper).updateById(argThat((ServiceOrder o) ->
                    OrderStatus.SERVICE_COMPLETED.name().equals(o.getStatus())));
        }

        @Test
        @DisplayName("非 IN_SERVICE 状态 - 离开打卡失败")
        void checkOut_wrongStatus_throws() {
            mockOrder.setStatus(OrderStatus.ACCEPTED.name());
            when(orderMapper.selectById(1L)).thenReturn(mockOrder);

            CheckOutDTO dto = new CheckOutDTO();
            dto.setLatitude(new BigDecimal("39.9087"));
            dto.setLongitude(new BigDecimal("116.4716"));

            assertThatThrownBy(() -> orderService.checkOut(1L, 1L, dto))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("confirmOrder - 确认完成")
    class ConfirmOrderTest {

        @Test
        @DisplayName("主人确认 - 成功并触发扣款")
        void confirm_success_capturesPayment() {
            mockOrder.setStatus(OrderStatus.SERVICE_COMPLETED.name());
            when(orderMapper.selectById(1L)).thenReturn(mockOrder);
            when(sitterMapper.selectById(1L)).thenReturn(mockSitter);

            orderService.confirmOrder(1L, 1L);

            verify(orderMapper).updateById(argThat((ServiceOrder so) -> {
                return OrderStatus.OWNER_CONFIRMED.name().equals(so.getStatus())
                        && so.getPlatformCommission() != null
                        && so.getSitterIncome() != null;
            }));
            verify(paymentService).capturePayment(1L);
        }

        @Test
        @DisplayName("非订单主人确认 - 抛异常")
        void confirm_wrongOwner_throws() {
            mockOrder.setStatus(OrderStatus.SERVICE_COMPLETED.name());
            when(orderMapper.selectById(1L)).thenReturn(mockOrder);

            assertThatThrownBy(() -> orderService.confirmOrder(1L, 999L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("无权确认");
        }
    }

    @Nested
    @DisplayName("cancelOrder - 取消订单")
    class CancelOrderTest {

        @Test
        @DisplayName("PENDING_MATCH 状态取消成功")
        void cancel_pendingMatch_success() {
            mockOrder.setStatus(OrderStatus.PENDING_MATCH.name());
            when(orderMapper.selectById(1L)).thenReturn(mockOrder);

            orderService.cancelOrder(1L, 1L, "不需要了");

            verify(orderMapper).updateById(argThat((ServiceOrder o) ->
                    OrderStatus.CANCELLED.name().equals(o.getStatus())));
        }

        @Test
        @DisplayName("IN_SERVICE 状态不允许取消")
        void cancel_inService_throws() {
            mockOrder.setStatus(OrderStatus.IN_SERVICE.name());
            when(orderMapper.selectById(1L)).thenReturn(mockOrder);

            assertThatThrownBy(() -> orderService.cancelOrder(1L, 1L, "取消"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("不允许取消");
        }

        @Test
        @DisplayName("SERVICE_COMPLETED 状态不允许取消")
        void cancel_completed_throws() {
            mockOrder.setStatus(OrderStatus.SERVICE_COMPLETED.name());
            when(orderMapper.selectById(1L)).thenReturn(mockOrder);

            assertThatThrownBy(() -> orderService.cancelOrder(1L, 1L, "取消"))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("佣金计算")
    class CommissionTest {

        @Test
        @DisplayName("订单数 > 100 的喂养师佣金 15%")
        void commission_highVolume_15percent() {
            mockOrder.setStatus(OrderStatus.SERVICE_COMPLETED.name());
            mockSitter.setTotalOrders(101);
            when(orderMapper.selectById(1L)).thenReturn(mockOrder);
            when(sitterMapper.selectById(1L)).thenReturn(mockSitter);

            orderService.confirmOrder(1L, 1L);

            verify(orderMapper).updateById(argThat((ServiceOrder so) -> {
                BigDecimal expectedCommission = new BigDecimal("7.35");
                return expectedCommission.compareTo(so.getPlatformCommission()) == 0;
            }));
        }

        @Test
        @DisplayName("订单数 10-100 的喂养师佣金 20%")
        void commission_midVolume_20percent() {
            mockOrder.setStatus(OrderStatus.SERVICE_COMPLETED.name());
            mockSitter.setTotalOrders(50);
            when(orderMapper.selectById(1L)).thenReturn(mockOrder);
            when(sitterMapper.selectById(1L)).thenReturn(mockSitter);

            orderService.confirmOrder(1L, 1L);

            verify(orderMapper).updateById(argThat((ServiceOrder so) -> {
                BigDecimal expectedCommission = new BigDecimal("9.80");
                return expectedCommission.compareTo(so.getPlatformCommission()) == 0;
            }));
        }

        @Test
        @DisplayName("订单数 < 10 的喂养师佣金 25%")
        void commission_newSitter_25percent() {
            mockOrder.setStatus(OrderStatus.SERVICE_COMPLETED.name());
            mockSitter.setTotalOrders(5);
            when(orderMapper.selectById(1L)).thenReturn(mockOrder);
            when(sitterMapper.selectById(1L)).thenReturn(mockSitter);

            orderService.confirmOrder(1L, 1L);

            verify(orderMapper).updateById(argThat((ServiceOrder so) -> {
                BigDecimal expectedCommission = new BigDecimal("12.25");
                return expectedCommission.compareTo(so.getPlatformCommission()) == 0;
            }));
        }
    }
}
