package com.petty.service.boundary;

import com.petty.common.exception.BusinessException;
import com.petty.dto.CheckInDTO;
import com.petty.dto.CheckOutDTO;
import com.petty.dto.OrderCreateDTO;
import com.petty.entity.*;
import com.petty.enums.OrderStatus;
import com.petty.mapper.*;
import com.petty.service.OrderService;
import com.petty.service.PaymentService;
import com.petty.service.impl.OrderServiceImpl;
import com.petty.service.matching.MatchingEngine;
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
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService - 边界条件测试")
class OrderBoundaryTest {

    @Mock private ServiceOrderMapper orderMapper;
    @Mock private OwnerMapper ownerMapper;
    @Mock private PetMapper petMapper;
    @Mock private SitterMapper sitterMapper;
    @Mock private ServiceTypeMapper serviceTypeMapper;
    @Mock private ServiceLogMapper serviceLogMapper;
    @Mock private OrderPetMapper orderPetMapper;
    @Mock private SitterScheduleMapper sitterScheduleMapper;
    @Mock private MatchingEngine matchingEngine;
    @Mock private PaymentService paymentService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private ServiceOrder mockOrder;

    @BeforeEach
    void setUp() {
        mockOrder = new ServiceOrder();
        mockOrder.setId(1L);
        mockOrder.setOrderNo("ORD123");
        mockOrder.setOwnerId(1L);
        mockOrder.setSitterId(1L);
        mockOrder.setServiceTypeId(1L);
        mockOrder.setTotalAmount(new BigDecimal("49.00"));
        mockOrder.setServiceLatitude(new BigDecimal("39.9087"));
        mockOrder.setServiceLongitude(new BigDecimal("116.4716"));
        mockOrder.setPetCount(1);
    }

    @Nested
    @DisplayName("GPS 边界条件")
    class GpsBoundaryTest {

        @Test
        @DisplayName("打卡距离刚好 0m - 成功")
        void checkIn_exactLocation_success() {
            mockOrder.setStatus(OrderStatus.ACCEPTED.name());
            when(orderMapper.selectById(1L)).thenReturn(mockOrder);

            CheckInDTO dto = new CheckInDTO();
            dto.setLatitude(new BigDecimal("39.9087"));
            dto.setLongitude(new BigDecimal("116.4716"));
            dto.setPhotoUrl("photo.jpg");

            orderService.checkIn(1L, 1L, dto);
            verify(orderMapper).updateById(any(ServiceOrder.class));
        }

        @Test
        @DisplayName("服务地址无坐标时跳过 GPS 校验")
        void checkIn_nullServiceCoords_skipsValidation() {
            mockOrder.setStatus(OrderStatus.ACCEPTED.name());
            mockOrder.setServiceLatitude(null);
            mockOrder.setServiceLongitude(null);
            when(orderMapper.selectById(1L)).thenReturn(mockOrder);

            CheckInDTO dto = new CheckInDTO();
            dto.setLatitude(new BigDecimal("40.0"));
            dto.setLongitude(new BigDecimal("117.0"));
            dto.setPhotoUrl("photo.jpg");

            orderService.checkIn(1L, 1L, dto);
            verify(orderMapper).updateById(any(ServiceOrder.class));
        }

        @Test
        @DisplayName("离开打卡距离 499m - 成功（小于500m阈值）")
        void checkOut_justWithin500m_success() {
            mockOrder.setStatus(OrderStatus.IN_SERVICE.name());
            when(orderMapper.selectById(1L)).thenReturn(mockOrder);
            when(serviceLogMapper.selectCount(any())).thenReturn(1L);

            CheckOutDTO dto = new CheckOutDTO();
            dto.setLatitude(new BigDecimal("39.9117"));
            dto.setLongitude(new BigDecimal("116.4750"));
            dto.setPhotoUrl("photo.jpg");
            dto.setServiceReport("完成");

            orderService.checkOut(1L, 1L, dto);
            verify(orderMapper).updateById(any(ServiceOrder.class));
        }

        @Test
        @DisplayName("到达打卡距离远超 200m - 失败")
        void checkIn_farAway_throws() {
            mockOrder.setStatus(OrderStatus.ACCEPTED.name());
            when(orderMapper.selectById(1L)).thenReturn(mockOrder);

            CheckInDTO dto = new CheckInDTO();
            dto.setLatitude(new BigDecimal("40.5"));
            dto.setLongitude(new BigDecimal("117.5"));
            dto.setPhotoUrl("photo.jpg");

            assertThatThrownBy(() -> orderService.checkIn(1L, 1L, dto))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("GPS距离");
        }
    }

    @Nested
    @DisplayName("取消时间阶梯费用")
    class CancelTiersTest {

        @Test
        @DisplayName("预约时间在24h以后取消 - 免费")
        void cancel_moreThan24h_freeCancel() {
            mockOrder.setStatus(OrderStatus.PENDING_ACCEPT.name());
            mockOrder.setScheduledDate(LocalDate.now().plusDays(3));
            mockOrder.setScheduledStartTime(LocalTime.of(10, 0));
            when(orderMapper.selectById(1L)).thenReturn(mockOrder);

            orderService.cancelOrder(1L, 1L, "有事");

            verify(orderMapper).updateById(argThat((ServiceOrder o) ->
                    OrderStatus.CANCELLED.name().equals(o.getStatus())));
        }

        @Test
        @DisplayName("预约时间在2-24h内取消 - 可取消")
        void cancel_between2and24h_allowed() {
            mockOrder.setStatus(OrderStatus.ACCEPTED.name());
            mockOrder.setScheduledDate(LocalDate.now().plusDays(1));
            mockOrder.setScheduledStartTime(LocalTime.now());
            when(orderMapper.selectById(1L)).thenReturn(mockOrder);

            orderService.cancelOrder(1L, 1L, "临时有事");

            verify(orderMapper).updateById(argThat((ServiceOrder o) ->
                    OrderStatus.CANCELLED.name().equals(o.getStatus())));
        }

        @Test
        @DisplayName("SITTER_EN_ROUTE 状态仍可取消")
        void cancel_enRoute_allowed() {
            mockOrder.setStatus(OrderStatus.SITTER_EN_ROUTE.name());
            mockOrder.setScheduledDate(LocalDate.now());
            mockOrder.setScheduledStartTime(LocalTime.now().plusHours(1));
            when(orderMapper.selectById(1L)).thenReturn(mockOrder);

            orderService.cancelOrder(1L, 1L, "改期");

            verify(orderMapper).updateById(argThat((ServiceOrder o) ->
                    OrderStatus.CANCELLED.name().equals(o.getStatus())));
        }

        @Test
        @DisplayName("IN_SERVICE 后不可取消")
        void cancel_inService_blocked() {
            mockOrder.setStatus(OrderStatus.IN_SERVICE.name());
            when(orderMapper.selectById(1L)).thenReturn(mockOrder);

            assertThatThrownBy(() -> orderService.cancelOrder(1L, 1L, "取消"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("不允许取消");
        }

        @Test
        @DisplayName("OWNER_CONFIRMED 后不可取消")
        void cancel_confirmed_blocked() {
            mockOrder.setStatus(OrderStatus.OWNER_CONFIRMED.name());
            when(orderMapper.selectById(1L)).thenReturn(mockOrder);

            assertThatThrownBy(() -> orderService.cancelOrder(1L, 1L, "取消"))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("订单状态机完整性 - 非法转换")
    class IllegalTransitionsTest {

        @Test
        @DisplayName("PENDING_MATCH 状态不能 checkIn")
        void pendingMatch_cannotCheckIn() {
            mockOrder.setStatus(OrderStatus.PENDING_MATCH.name());
            when(orderMapper.selectById(1L)).thenReturn(mockOrder);

            CheckInDTO dto = new CheckInDTO();
            dto.setLatitude(new BigDecimal("39.9087"));
            dto.setLongitude(new BigDecimal("116.4716"));
            dto.setPhotoUrl("p.jpg");

            assertThatThrownBy(() -> orderService.checkIn(1L, 1L, dto))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("ACCEPTED 状态不能 checkOut")
        void accepted_cannotCheckOut() {
            mockOrder.setStatus(OrderStatus.ACCEPTED.name());
            when(orderMapper.selectById(1L)).thenReturn(mockOrder);

            CheckOutDTO dto = new CheckOutDTO();
            dto.setLatitude(new BigDecimal("39.9087"));
            dto.setLongitude(new BigDecimal("116.4716"));
            dto.setPhotoUrl("p.jpg");
            dto.setServiceReport("done");

            assertThatThrownBy(() -> orderService.checkOut(1L, 1L, dto))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("PENDING_MATCH 状态不能 confirm")
        void pendingMatch_cannotConfirm() {
            mockOrder.setStatus(OrderStatus.PENDING_MATCH.name());
            when(orderMapper.selectById(1L)).thenReturn(mockOrder);

            assertThatThrownBy(() -> orderService.confirmOrder(1L, 1L))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("IN_SERVICE 状态不能直接 confirm")
        void inService_cannotConfirm() {
            mockOrder.setStatus(OrderStatus.IN_SERVICE.name());
            when(orderMapper.selectById(1L)).thenReturn(mockOrder);

            assertThatThrownBy(() -> orderService.confirmOrder(1L, 1L))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("CANCELLED 状态不能接单")
        void cancelled_cannotAccept() {
            mockOrder.setStatus(OrderStatus.CANCELLED.name());
            when(orderMapper.selectById(1L)).thenReturn(mockOrder);

            assertThatThrownBy(() -> orderService.acceptOrder(1L, 1L))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("OWNER_CONFIRMED 状态不能再确认")
        void confirmed_cannotConfirmAgain() {
            mockOrder.setStatus(OrderStatus.OWNER_CONFIRMED.name());
            when(orderMapper.selectById(1L)).thenReturn(mockOrder);

            assertThatThrownBy(() -> orderService.confirmOrder(1L, 1L))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("参数校验边界")
    class ValidationEdgeCasesTest {

        @Test
        @DisplayName("订单不存在时所有操作抛异常")
        void allOps_orderNotFound_throws() {
            when(orderMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> orderService.acceptOrder(999L, 1L))
                    .isInstanceOf(BusinessException.class).hasMessageContaining("不存在");
            assertThatThrownBy(() -> orderService.confirmOrder(999L, 1L))
                    .isInstanceOf(BusinessException.class).hasMessageContaining("不存在");
            assertThatThrownBy(() -> orderService.getOrderDetail(999L))
                    .isInstanceOf(BusinessException.class).hasMessageContaining("不存在");
        }

        @Test
        @DisplayName("创建订单 - 服务类型不存在")
        void createOrder_serviceTypeNotFound() {
            Owner owner = new Owner();
            owner.setId(1L);
            when(ownerMapper.selectById(1L)).thenReturn(owner);
            when(serviceTypeMapper.selectById(99L)).thenReturn(null);

            OrderCreateDTO dto = new OrderCreateDTO();
            dto.setServiceTypeId(99L);
            dto.setPetIds(List.of(1L));
            dto.setScheduledDate("2026-08-01");
            dto.setScheduledStartTime("10:00");
            dto.setScheduledEndTime("10:30");

            assertThatThrownBy(() -> orderService.createOrder(1L, dto))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("服务类型不存在");
        }

        @Test
        @DisplayName("创建订单 - 宠物列表为空")
        void createOrder_emptyPets() {
            Owner owner = new Owner();
            owner.setId(1L);
            ServiceType st = new ServiceType();
            st.setId(1L);
            st.setBasePrice(new BigDecimal("49"));
            st.setExtraPetPrice(new BigDecimal("20"));

            when(ownerMapper.selectById(1L)).thenReturn(owner);
            when(serviceTypeMapper.selectById(1L)).thenReturn(st);
            when(petMapper.selectBatchIds(any())).thenReturn(List.of());

            OrderCreateDTO dto = new OrderCreateDTO();
            dto.setServiceTypeId(1L);
            dto.setPetIds(List.of(999L));
            dto.setScheduledDate("2026-08-01");
            dto.setScheduledStartTime("10:00");
            dto.setScheduledEndTime("10:30");

            assertThatThrownBy(() -> orderService.createOrder(1L, dto))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("宠物列表为空");
        }
    }
}
