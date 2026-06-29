package com.petty.service.boundary;

import com.petty.common.exception.BusinessException;
import com.petty.dto.OrderCreateDTO;
import com.petty.dto.ServiceLogCreateDTO;
import com.petty.entity.*;
import com.petty.enums.OrderStatus;
import com.petty.mapper.*;
import com.petty.service.PaymentService;
import com.petty.service.impl.OrderServiceImpl;
import com.petty.service.matching.MatchingEngine;
import com.petty.vo.OrderDetailVO;
import com.petty.vo.OrderVO;
import com.petty.vo.ServiceLogVO;
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
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService - 分支覆盖提升")
class OrderBranchCoverageTest {

    @Mock private ServiceOrderMapper orderMapper;
    @Mock private OwnerMapper ownerMapper;
    @Mock private PetMapper petMapper;
    @Mock private SitterMapper sitterMapper;
    @Mock private ServiceTypeMapper serviceTypeMapper;
    @Mock private ServiceLogMapper serviceLogMapper;
    @Mock private OrderPetMapper orderPetMapper;
    @Mock private MatchingEngine matchingEngine;
    @Mock private PaymentService paymentService;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Nested
    @DisplayName("createOrder 分支覆盖")
    class CreateOrderBranches {

        private Owner owner;
        private ServiceType serviceType;
        private Pet pet;

        @BeforeEach
        void setUp() {
            owner = new Owner();
            owner.setId(1L);
            owner.setAddress("上海市浦东新区");
            owner.setLatitude(new BigDecimal("31.23"));
            owner.setLongitude(new BigDecimal("121.47"));

            serviceType = new ServiceType();
            serviceType.setId(1L);
            serviceType.setBasePrice(new BigDecimal("49"));
            serviceType.setExtraPetPrice(new BigDecimal("20"));

            pet = new Pet();
            pet.setId(1L);
            pet.setOwnerId(1L);
            pet.setSpecies("CAT");
        }

        @Test
        @DisplayName("指定喂养师 - ACTIVE状态 - 直接派单")
        void createOrder_preferredSitter_active_directAssign() {
            when(ownerMapper.selectById(1L)).thenReturn(owner);
            when(serviceTypeMapper.selectById(1L)).thenReturn(serviceType);
            when(petMapper.selectBatchIds(any())).thenReturn(List.of(pet));
            Sitter activeSitter = new Sitter();
            activeSitter.setId(10L);
            activeSitter.setStatus("ACTIVE");
            when(sitterMapper.selectById(10L)).thenReturn(activeSitter);

            OrderCreateDTO dto = new OrderCreateDTO();
            dto.setServiceTypeId(1L);
            dto.setPetIds(List.of(1L));
            dto.setScheduledDate("2026-08-01");
            dto.setScheduledStartTime("10:00");
            dto.setScheduledEndTime("10:30");
            dto.setPreferredSitterId(10L);

            orderService.createOrder(1L, dto);

            verify(orderMapper).insert(argThat((ServiceOrder o) ->
                    OrderStatus.PENDING_ACCEPT.name().equals(o.getStatus()) &&
                            o.getSitterId().equals(10L)));
        }

        @Test
        @DisplayName("指定喂养师 - 非ACTIVE状态 - 降级到匹配")
        void createOrder_preferredSitter_inactive_fallback() {
            when(ownerMapper.selectById(1L)).thenReturn(owner);
            when(serviceTypeMapper.selectById(1L)).thenReturn(serviceType);
            when(petMapper.selectBatchIds(any())).thenReturn(List.of(pet));
            Sitter inactiveSitter = new Sitter();
            inactiveSitter.setId(10L);
            inactiveSitter.setStatus("SUSPENDED");
            when(sitterMapper.selectById(10L)).thenReturn(inactiveSitter);

            OrderCreateDTO dto = new OrderCreateDTO();
            dto.setServiceTypeId(1L);
            dto.setPetIds(List.of(1L));
            dto.setScheduledDate("2026-08-01");
            dto.setScheduledStartTime("10:00");
            dto.setScheduledEndTime("10:30");
            dto.setPreferredSitterId(10L);

            orderService.createOrder(1L, dto);

            verify(orderMapper).insert(argThat((ServiceOrder o) ->
                    OrderStatus.PENDING_MATCH.name().equals(o.getStatus())));
        }

        @Test
        @DisplayName("指定喂养师 - 不存在 - 降级到匹配")
        void createOrder_preferredSitter_notFound_fallback() {
            when(ownerMapper.selectById(1L)).thenReturn(owner);
            when(serviceTypeMapper.selectById(1L)).thenReturn(serviceType);
            when(petMapper.selectBatchIds(any())).thenReturn(List.of(pet));
            when(sitterMapper.selectById(999L)).thenReturn(null);

            OrderCreateDTO dto = new OrderCreateDTO();
            dto.setServiceTypeId(1L);
            dto.setPetIds(List.of(1L));
            dto.setScheduledDate("2026-08-01");
            dto.setScheduledStartTime("10:00");
            dto.setScheduledEndTime("10:30");
            dto.setPreferredSitterId(999L);

            orderService.createOrder(1L, dto);

            verify(orderMapper).insert(argThat((ServiceOrder o) ->
                    OrderStatus.PENDING_MATCH.name().equals(o.getStatus())));
        }

        @Test
        @DisplayName("无指定喂养师 + 自定义服务地址 - 使用DTO中的坐标")
        void createOrder_customAddress_usesDtoCoords() {
            when(ownerMapper.selectById(1L)).thenReturn(owner);
            when(serviceTypeMapper.selectById(1L)).thenReturn(serviceType);
            when(petMapper.selectBatchIds(any())).thenReturn(List.of(pet));
            when(sitterMapper.selectList(any())).thenReturn(Collections.emptyList());
            when(matchingEngine.filterByDistance(any(), any(), any())).thenReturn(Collections.emptyList());

            OrderCreateDTO dto = new OrderCreateDTO();
            dto.setServiceTypeId(1L);
            dto.setPetIds(List.of(1L));
            dto.setScheduledDate("2026-08-01");
            dto.setScheduledStartTime("10:00");
            dto.setScheduledEndTime("10:30");
            dto.setServiceAddress("自定义地址");
            dto.setLatitude(new BigDecimal("40.0"));
            dto.setLongitude(new BigDecimal("117.0"));

            orderService.createOrder(1L, dto);

            verify(orderMapper).insert(argThat((ServiceOrder o) ->
                    "自定义地址".equals(o.getServiceAddress()) &&
                            new BigDecimal("40.0").equals(o.getServiceLatitude())));
        }

        @Test
        @DisplayName("多宠物 - 附加费用计算正确")
        void createOrder_multiplePets_extraCharge() {
            Pet pet2 = new Pet();
            pet2.setId(2L);
            pet2.setOwnerId(1L);
            pet2.setSpecies("DOG");

            when(ownerMapper.selectById(1L)).thenReturn(owner);
            when(serviceTypeMapper.selectById(1L)).thenReturn(serviceType);
            when(petMapper.selectBatchIds(any())).thenReturn(List.of(pet, pet2));
            when(sitterMapper.selectList(any())).thenReturn(Collections.emptyList());
            when(matchingEngine.filterByDistance(any(), any(), any())).thenReturn(Collections.emptyList());

            OrderCreateDTO dto = new OrderCreateDTO();
            dto.setServiceTypeId(1L);
            dto.setPetIds(List.of(1L, 2L));
            dto.setScheduledDate("2026-08-01");
            dto.setScheduledStartTime("10:00");
            dto.setScheduledEndTime("10:30");

            orderService.createOrder(1L, dto);

            verify(orderMapper).insert(argThat((ServiceOrder o) ->
                    o.getTotalAmount().compareTo(new BigDecimal("69")) == 0 &&
                            o.getPetCount() == 2));
        }

        @Test
        @DisplayName("自动匹配 - 有合适喂养师 - 直接派单")
        void createOrder_autoMatch_success() {
            Sitter matchedSitter = new Sitter();
            matchedSitter.setId(5L);
            matchedSitter.setName("匹配喂养师");
            matchedSitter.setStatus("ACTIVE");

            when(ownerMapper.selectById(1L)).thenReturn(owner);
            when(serviceTypeMapper.selectById(1L)).thenReturn(serviceType);
            when(petMapper.selectBatchIds(any())).thenReturn(List.of(pet));
            when(sitterMapper.selectList(any())).thenReturn(List.of(matchedSitter));
            when(matchingEngine.filterByDistance(any(), any(), any())).thenReturn(List.of(matchedSitter));
            when(matchingEngine.filterBySpecies(any(), eq("CAT"))).thenReturn(List.of(matchedSitter));
            when(matchingEngine.rank(any(), any(), any())).thenReturn(
                    List.of(new MatchingEngine.ScoredSitter(matchedSitter, 90.0, 2.5)));

            OrderCreateDTO dto = new OrderCreateDTO();
            dto.setServiceTypeId(1L);
            dto.setPetIds(List.of(1L));
            dto.setScheduledDate("2026-08-01");
            dto.setScheduledStartTime("10:00");
            dto.setScheduledEndTime("10:30");

            orderService.createOrder(1L, dto);

            verify(orderMapper).insert(argThat((ServiceOrder o) ->
                    OrderStatus.PENDING_ACCEPT.name().equals(o.getStatus()) &&
                            o.getSitterId().equals(5L)));
        }

        @Test
        @DisplayName("自动匹配 - 物种过滤后无人 - 保持PENDING_MATCH")
        void createOrder_autoMatch_noSpeciesMatch() {
            Sitter nearSitter = new Sitter();
            nearSitter.setId(5L);
            nearSitter.setStatus("ACTIVE");

            when(ownerMapper.selectById(1L)).thenReturn(owner);
            when(serviceTypeMapper.selectById(1L)).thenReturn(serviceType);
            when(petMapper.selectBatchIds(any())).thenReturn(List.of(pet));
            when(sitterMapper.selectList(any())).thenReturn(List.of(nearSitter));
            when(matchingEngine.filterByDistance(any(), any(), any())).thenReturn(List.of(nearSitter));
            when(matchingEngine.filterBySpecies(any(), eq("CAT"))).thenReturn(Collections.emptyList());

            OrderCreateDTO dto = new OrderCreateDTO();
            dto.setServiceTypeId(1L);
            dto.setPetIds(List.of(1L));
            dto.setScheduledDate("2026-08-01");
            dto.setScheduledStartTime("10:00");
            dto.setScheduledEndTime("10:30");

            orderService.createOrder(1L, dto);

            verify(orderMapper).insert(argThat((ServiceOrder o) ->
                    OrderStatus.PENDING_MATCH.name().equals(o.getStatus())));
        }

        @Test
        @DisplayName("owner不存在 - 抛异常")
        void createOrder_ownerNotFound() {
            when(ownerMapper.selectById(99L)).thenReturn(null);

            OrderCreateDTO dto = new OrderCreateDTO();
            dto.setServiceTypeId(1L);
            dto.setPetIds(List.of(1L));
            dto.setScheduledDate("2026-08-01");
            dto.setScheduledStartTime("10:00");
            dto.setScheduledEndTime("10:30");

            assertThatThrownBy(() -> orderService.createOrder(99L, dto))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("宠物主人不存在");
        }
    }

    @Nested
    @DisplayName("佣金计算分支")
    class CommissionBranches {

        @Test
        @DisplayName("100+单喂养师 - 15%佣金")
        void commission_over100orders() {
            ServiceOrder order = createBaseOrder(OrderStatus.SERVICE_COMPLETED);
            when(orderMapper.selectById(1L)).thenReturn(order);

            Sitter sitter = new Sitter();
            sitter.setId(1L);
            sitter.setTotalOrders(150);
            when(sitterMapper.selectById(1L)).thenReturn(sitter);

            orderService.confirmOrder(1L, 1L);

            verify(orderMapper).updateById(argThat((ServiceOrder o) ->
                    o.getPlatformCommission().compareTo(new BigDecimal("7.35")) == 0));
        }

        @Test
        @DisplayName("10-100单喂养师 - 20%佣金")
        void commission_10to100orders() {
            ServiceOrder order = createBaseOrder(OrderStatus.SERVICE_COMPLETED);
            when(orderMapper.selectById(1L)).thenReturn(order);

            Sitter sitter = new Sitter();
            sitter.setId(1L);
            sitter.setTotalOrders(50);
            when(sitterMapper.selectById(1L)).thenReturn(sitter);

            orderService.confirmOrder(1L, 1L);

            verify(orderMapper).updateById(argThat((ServiceOrder o) ->
                    o.getPlatformCommission().compareTo(new BigDecimal("9.80")) == 0));
        }

        @Test
        @DisplayName("新手喂养师 (<10单) - 25%佣金")
        void commission_lessThan10orders() {
            ServiceOrder order = createBaseOrder(OrderStatus.SERVICE_COMPLETED);
            when(orderMapper.selectById(1L)).thenReturn(order);

            Sitter sitter = new Sitter();
            sitter.setId(1L);
            sitter.setTotalOrders(5);
            when(sitterMapper.selectById(1L)).thenReturn(sitter);

            orderService.confirmOrder(1L, 1L);

            verify(orderMapper).updateById(argThat((ServiceOrder o) ->
                    o.getPlatformCommission().compareTo(new BigDecimal("12.25")) == 0));
        }

        @Test
        @DisplayName("喂养师不存在 - 默认20%佣金")
        void commission_sitterNotFound_default20() {
            ServiceOrder order = createBaseOrder(OrderStatus.SERVICE_COMPLETED);
            when(orderMapper.selectById(1L)).thenReturn(order);
            when(sitterMapper.selectById(1L)).thenReturn(null);

            orderService.confirmOrder(1L, 1L);

            verify(orderMapper).updateById(argThat((ServiceOrder o) ->
                    o.getPlatformCommission().compareTo(new BigDecimal("9.80")) == 0));
        }

        @Test
        @DisplayName("totalOrders为null - 视为0，使用25%")
        void commission_nullTotalOrders() {
            ServiceOrder order = createBaseOrder(OrderStatus.SERVICE_COMPLETED);
            when(orderMapper.selectById(1L)).thenReturn(order);

            Sitter sitter = new Sitter();
            sitter.setId(1L);
            sitter.setTotalOrders(null);
            when(sitterMapper.selectById(1L)).thenReturn(sitter);

            orderService.confirmOrder(1L, 1L);

            verify(orderMapper).updateById(argThat((ServiceOrder o) ->
                    o.getPlatformCommission().compareTo(new BigDecimal("12.25")) == 0));
        }
    }

    @Nested
    @DisplayName("listOrders 分支覆盖")
    class ListOrdersBranches {

        @Test
        @DisplayName("按状态过滤")
        void listOrders_byStatus() {
            when(orderMapper.selectList(any())).thenReturn(Collections.emptyList());
            List<OrderVO> result = orderService.listOrders("ACCEPTED", null, null);
            assertThat(result).isEmpty();
            verify(orderMapper).selectList(any());
        }

        @Test
        @DisplayName("按 ownerId 过滤")
        void listOrders_byOwner() {
            when(orderMapper.selectList(any())).thenReturn(Collections.emptyList());
            List<OrderVO> result = orderService.listOrders(null, 1L, null);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("按 sitterId 过滤")
        void listOrders_bySitter() {
            when(orderMapper.selectList(any())).thenReturn(Collections.emptyList());
            List<OrderVO> result = orderService.listOrders(null, null, 2L);
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getOrderDetail 分支覆盖")
    class GetOrderDetailBranches {

        @Test
        @DisplayName("订单无喂养师 - sitterPhone为null")
        void detail_noSitter() {
            ServiceOrder order = createBaseOrder(OrderStatus.ACCEPTED);
            order.setSitterId(null);
            when(orderMapper.selectById(1L)).thenReturn(order);
            when(serviceTypeMapper.selectById(any())).thenReturn(null);
            when(ownerMapper.selectById(any())).thenReturn(null);
            when(serviceLogMapper.selectList(any())).thenReturn(Collections.emptyList());

            OrderDetailVO detail = orderService.getOrderDetail(1L);
            assertThat(detail.getSitterPhone()).isNull();
        }

        @Test
        @DisplayName("sitter查询返回null - sitterPhone为null")
        void detail_sitterQueryReturnsNull() {
            ServiceOrder order = createBaseOrder(OrderStatus.ACCEPTED);
            when(orderMapper.selectById(1L)).thenReturn(order);
            when(sitterMapper.selectById(1L)).thenReturn(null);
            when(serviceTypeMapper.selectById(any())).thenReturn(null);
            when(ownerMapper.selectById(any())).thenReturn(null);
            when(serviceLogMapper.selectList(any())).thenReturn(Collections.emptyList());

            OrderDetailVO detail = orderService.getOrderDetail(1L);
            assertThat(detail.getSitterPhone()).isNull();
        }
    }

    @Nested
    @DisplayName("addServiceLog 分支覆盖")
    class AddServiceLogBranches {

        @Test
        @DisplayName("服务中 - 有photoUrls - 正常记录")
        void addLog_withPhotos_success() {
            ServiceOrder order = createBaseOrder(OrderStatus.IN_SERVICE);
            when(orderMapper.selectById(1L)).thenReturn(order);

            ServiceLogCreateDTO dto = new ServiceLogCreateDTO();
            dto.setLogType("FEEDING");
            dto.setDescription("喂猫粮");
            dto.setPhotoUrls(List.of("photo1.jpg", "photo2.jpg"));
            dto.setVideoUrl("video.mp4");
            dto.setLatitude(new BigDecimal("39.9"));
            dto.setLongitude(new BigDecimal("116.4"));
            dto.setPetStatus("正常");

            orderService.addServiceLog(1L, 1L, dto);

            verify(serviceLogMapper).insert(argThat((ServiceLog l) ->
                    "photo1.jpg,photo2.jpg".equals(l.getPhotoUrls()) &&
                            "video.mp4".equals(l.getVideoUrl())));
        }

        @Test
        @DisplayName("服务中 - 无photoUrls - photoUrls为null")
        void addLog_noPhotos_success() {
            ServiceOrder order = createBaseOrder(OrderStatus.IN_SERVICE);
            when(orderMapper.selectById(1L)).thenReturn(order);

            ServiceLogCreateDTO dto = new ServiceLogCreateDTO();
            dto.setLogType("FEEDING");
            dto.setDescription("喂猫粮");
            dto.setPhotoUrls(null);

            orderService.addServiceLog(1L, 1L, dto);

            verify(serviceLogMapper).insert(argThat((ServiceLog l) ->
                    l.getPhotoUrls() == null));
        }

        @Test
        @DisplayName("非服务中 - 不能添加记录")
        void addLog_notInService_throws() {
            ServiceOrder order = createBaseOrder(OrderStatus.ACCEPTED);
            when(orderMapper.selectById(1L)).thenReturn(order);

            ServiceLogCreateDTO dto = new ServiceLogCreateDTO();
            dto.setLogType("FEEDING");
            dto.setDescription("test");

            assertThatThrownBy(() -> orderService.addServiceLog(1L, 1L, dto))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("服务中");
        }
    }

    @Nested
    @DisplayName("copyToVO / toLogVO 分支覆盖")
    class VoConversionBranches {

        @Test
        @DisplayName("toLogVO - photoUrls 非空时分割")
        void toLogVO_withPhotos() {
            ServiceLog log = new ServiceLog();
            log.setId(1L);
            log.setOrderId(1L);
            log.setLogType("FEEDING");
            log.setDescription("喂食");
            log.setPhotoUrls("a.jpg,b.jpg");
            log.setCreatedAt(LocalDateTime.now());

            when(orderMapper.selectById(1L)).thenReturn(createBaseOrder(OrderStatus.IN_SERVICE));
            when(serviceTypeMapper.selectById(any())).thenReturn(null);
            when(ownerMapper.selectById(any())).thenReturn(null);
            when(sitterMapper.selectById(any())).thenReturn(null);
            when(serviceLogMapper.selectList(any())).thenReturn(List.of(log));

            OrderDetailVO detail = orderService.getOrderDetail(1L);
            assertThat(detail.getServiceLogs()).hasSize(1);
            assertThat(detail.getServiceLogs().get(0).getPhotoUrls()).containsExactly("a.jpg", "b.jpg");
        }

        @Test
        @DisplayName("toLogVO - photoUrls 为空时 photoUrls不设置")
        void toLogVO_emptyPhotos() {
            ServiceLog log = new ServiceLog();
            log.setId(1L);
            log.setOrderId(1L);
            log.setLogType("CHECK_IN");
            log.setDescription("到达");
            log.setPhotoUrls(null);
            log.setCreatedAt(LocalDateTime.now());

            when(orderMapper.selectById(1L)).thenReturn(createBaseOrder(OrderStatus.IN_SERVICE));
            when(serviceTypeMapper.selectById(any())).thenReturn(null);
            when(ownerMapper.selectById(any())).thenReturn(null);
            when(sitterMapper.selectById(any())).thenReturn(null);
            when(serviceLogMapper.selectList(any())).thenReturn(List.of(log));

            OrderDetailVO detail = orderService.getOrderDetail(1L);
            assertThat(detail.getServiceLogs().get(0).getPhotoUrls()).isNull();
        }

        @Test
        @DisplayName("copyToVO - 有ServiceType时设置名称")
        void copyToVO_withServiceType() {
            ServiceOrder order = createBaseOrder(OrderStatus.ACCEPTED);
            ServiceType st = new ServiceType();
            st.setId(1L);
            st.setName("上门喂猫");
            Owner owner = new Owner();
            owner.setId(1L);
            owner.setNickname("张三");
            Sitter sitter = new Sitter();
            sitter.setId(1L);
            sitter.setName("李四");
            sitter.setAvatarUrl("avatar.jpg");

            when(orderMapper.selectById(1L)).thenReturn(order);
            when(serviceTypeMapper.selectById(1L)).thenReturn(st);
            when(ownerMapper.selectById(1L)).thenReturn(owner);
            when(sitterMapper.selectById(1L)).thenReturn(sitter);
            when(serviceLogMapper.selectList(any())).thenReturn(Collections.emptyList());

            OrderDetailVO detail = orderService.getOrderDetail(1L);
            assertThat(detail.getServiceTypeName()).isEqualTo("上门喂猫");
            assertThat(detail.getOwnerNickname()).isEqualTo("张三");
            assertThat(detail.getSitterName()).isEqualTo("李四");
        }
    }

    private ServiceOrder createBaseOrder(OrderStatus status) {
        ServiceOrder order = new ServiceOrder();
        order.setId(1L);
        order.setOrderNo("ORD123");
        order.setOwnerId(1L);
        order.setSitterId(1L);
        order.setServiceTypeId(1L);
        order.setTotalAmount(new BigDecimal("49.00"));
        order.setStatus(status.name());
        order.setScheduledDate(LocalDate.now().plusDays(3));
        order.setScheduledStartTime(LocalTime.of(10, 0));
        order.setScheduledEndTime(LocalTime.of(11, 0));
        order.setCreatedAt(LocalDateTime.now());
        return order;
    }
}
