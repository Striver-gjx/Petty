package com.petty.service.boundary;

import com.petty.common.exception.BusinessException;
import com.petty.dto.PaymentInitDTO;
import com.petty.dto.RefundDTO;
import com.petty.dto.ReviewCreateDTO;
import com.petty.entity.*;
import com.petty.enums.OrderStatus;
import com.petty.mapper.*;
import com.petty.service.impl.PaymentServiceImpl;
import com.petty.service.impl.ReviewServiceImpl;
import com.petty.vo.PaymentVO;
import com.petty.vo.ReviewVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Review & Payment - 分支覆盖提升")
class ReviewPaymentBranchTest {

    @Nested
    @DisplayName("ReviewService 分支覆盖")
    class ReviewBranches {

        @Mock private ReviewMapper reviewMapper;
        @Mock private ServiceOrderMapper orderMapper;
        @Mock private OwnerMapper ownerMapper;
        @Mock private SitterMapper sitterMapper;

        @InjectMocks
        private ReviewServiceImpl reviewService;

        @Test
        @DisplayName("SITTER评价 - 不触发updateSitterRating")
        void createReview_sitterType_noRatingUpdate() {
            ServiceOrder order = new ServiceOrder();
            order.setId(1L);
            order.setOwnerId(1L);
            order.setSitterId(2L);
            order.setStatus(OrderStatus.OWNER_CONFIRMED.name());
            when(orderMapper.selectById(1L)).thenReturn(order);
            when(reviewMapper.selectCount(any())).thenReturn(0L);

            ReviewCreateDTO dto = new ReviewCreateDTO();
            dto.setOrderId(1L);
            dto.setRating(new BigDecimal("5.0"));
            dto.setContent("很配合");

            reviewService.createReview(2L, "SITTER", dto);

            verify(reviewMapper).insert(any(Review.class));
            verify(sitterMapper, never()).selectById(any());
        }

        @Test
        @DisplayName("评价目标为null - 抛异常")
        void createReview_targetNull_throws() {
            ServiceOrder order = new ServiceOrder();
            order.setId(1L);
            order.setOwnerId(1L);
            order.setSitterId(null);
            order.setStatus(OrderStatus.SERVICE_COMPLETED.name());
            when(orderMapper.selectById(1L)).thenReturn(order);
            when(reviewMapper.selectCount(any())).thenReturn(0L);

            ReviewCreateDTO dto = new ReviewCreateDTO();
            dto.setOrderId(1L);
            dto.setRating(new BigDecimal("5.0"));
            dto.setContent("test");

            assertThatThrownBy(() -> reviewService.createReview(1L, "OWNER", dto))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("评价目标不存在");
        }

        @Test
        @DisplayName("评价有photoUrls和tags - 正确存储")
        void createReview_withPhotosAndTags() {
            ServiceOrder order = new ServiceOrder();
            order.setId(1L);
            order.setOwnerId(1L);
            order.setSitterId(2L);
            order.setStatus(OrderStatus.OWNER_CONFIRMED.name());
            when(orderMapper.selectById(1L)).thenReturn(order);
            when(reviewMapper.selectCount(any())).thenReturn(0L);
            when(sitterMapper.selectById(2L)).thenReturn(createSitter(2L, "4.5"));
            when(reviewMapper.selectList(any())).thenReturn(List.of(createReview("5.0")));

            ReviewCreateDTO dto = new ReviewCreateDTO();
            dto.setOrderId(1L);
            dto.setRating(new BigDecimal("5.0"));
            dto.setContent("很好");
            dto.setPhotoUrls(List.of("p1.jpg", "p2.jpg"));
            dto.setTags(List.of("准时", "专业"));
            dto.setIsAnonymous(true);

            reviewService.createReview(1L, "OWNER", dto);

            verify(reviewMapper).insert(argThat((Review r) ->
                    "p1.jpg,p2.jpg".equals(r.getPhotoUrls()) &&
                            "准时,专业".equals(r.getTags()) &&
                            r.getIsAnonymous() == 1));
        }

        @Test
        @DisplayName("评价无photoUrls无tags - 存null")
        void createReview_noPhotosNoTags() {
            ServiceOrder order = new ServiceOrder();
            order.setId(1L);
            order.setOwnerId(1L);
            order.setSitterId(2L);
            order.setStatus(OrderStatus.OWNER_CONFIRMED.name());
            when(orderMapper.selectById(1L)).thenReturn(order);
            when(reviewMapper.selectCount(any())).thenReturn(0L);
            when(sitterMapper.selectById(2L)).thenReturn(createSitter(2L, "4.5"));
            when(reviewMapper.selectList(any())).thenReturn(List.of(createReview("5.0")));

            ReviewCreateDTO dto = new ReviewCreateDTO();
            dto.setOrderId(1L);
            dto.setRating(new BigDecimal("4.0"));
            dto.setContent("不错");
            dto.setPhotoUrls(null);
            dto.setTags(null);
            dto.setIsAnonymous(false);

            reviewService.createReview(1L, "OWNER", dto);

            verify(reviewMapper).insert(argThat((Review r) ->
                    r.getPhotoUrls() == null &&
                            r.getTags() == null &&
                            r.getIsAnonymous() == 0));
        }

        @Test
        @DisplayName("updateSitterRating - sitter查询返回null不crash")
        void updateSitterRating_sitterNull_noop() {
            ServiceOrder order = new ServiceOrder();
            order.setId(1L);
            order.setOwnerId(1L);
            order.setSitterId(2L);
            order.setStatus(OrderStatus.OWNER_CONFIRMED.name());
            when(orderMapper.selectById(1L)).thenReturn(order);
            when(reviewMapper.selectCount(any())).thenReturn(0L);
            when(reviewMapper.selectList(any())).thenReturn(List.of(createReview("5.0")));
            when(sitterMapper.selectById(2L)).thenReturn(null);

            ReviewCreateDTO dto = new ReviewCreateDTO();
            dto.setOrderId(1L);
            dto.setRating(new BigDecimal("5.0"));
            dto.setContent("ok");

            reviewService.createReview(1L, "OWNER", dto);

            verify(sitterMapper, never()).updateById(any(Sitter.class));
        }

        @Test
        @DisplayName("updateSitterRating - 评分正常不触发惩罚")
        void updateSitterRating_normalRating_noStatusChange() {
            ServiceOrder order = new ServiceOrder();
            order.setId(1L);
            order.setOwnerId(1L);
            order.setSitterId(2L);
            order.setStatus(OrderStatus.OWNER_CONFIRMED.name());
            when(orderMapper.selectById(1L)).thenReturn(order);
            when(reviewMapper.selectCount(any())).thenReturn(0L);

            Sitter sitter = createSitter(2L, "4.5");
            when(sitterMapper.selectById(2L)).thenReturn(sitter);
            when(reviewMapper.selectList(any())).thenReturn(List.of(createReview("4.0"), createReview("5.0")));

            ReviewCreateDTO dto = new ReviewCreateDTO();
            dto.setOrderId(1L);
            dto.setRating(new BigDecimal("5.0"));
            dto.setContent("great");

            reviewService.createReview(1L, "OWNER", dto);

            verify(sitterMapper).updateById(argThat((Sitter s) ->
                    "ACTIVE".equals(s.getStatus())));
        }

        @Test
        @DisplayName("listSitterReviews - 匿名评价")
        void listReviews_anonymous() {
            Review anon = new Review();
            anon.setId(1L);
            anon.setOrderId(1L);
            anon.setReviewerId(1L);
            anon.setReviewerType("OWNER");
            anon.setRating(new BigDecimal("5.0"));
            anon.setContent("好");
            anon.setIsAnonymous(1);
            anon.setPhotoUrls("p.jpg");
            anon.setTags("准时");
            anon.setCreatedAt(LocalDateTime.now());

            when(reviewMapper.selectList(any())).thenReturn(List.of(anon));

            List<ReviewVO> vos = reviewService.listSitterReviews(2L);
            assertThat(vos).hasSize(1);
            assertThat(vos.get(0).getReviewerNickname()).isEqualTo("匿名用户");
        }

        @Test
        @DisplayName("listSitterReviews - 非匿名 有owner")
        void listReviews_nonAnonymous_withOwner() {
            Review review = new Review();
            review.setId(1L);
            review.setOrderId(1L);
            review.setReviewerId(1L);
            review.setReviewerType("OWNER");
            review.setRating(new BigDecimal("4.0"));
            review.setContent("ok");
            review.setIsAnonymous(0);
            review.setPhotoUrls(null);
            review.setTags(null);
            review.setCreatedAt(LocalDateTime.now());

            Owner owner = new Owner();
            owner.setId(1L);
            owner.setNickname("张三");
            owner.setAvatarUrl("avatar.png");

            when(reviewMapper.selectList(any())).thenReturn(List.of(review));
            when(ownerMapper.selectById(1L)).thenReturn(owner);

            List<ReviewVO> vos = reviewService.listSitterReviews(2L);
            assertThat(vos.get(0).getReviewerNickname()).isEqualTo("张三");
            assertThat(vos.get(0).getReviewerAvatar()).isEqualTo("avatar.png");
            assertThat(vos.get(0).getPhotoUrls()).isEmpty();
            assertThat(vos.get(0).getTags()).isEmpty();
        }

        @Test
        @DisplayName("listSitterReviews - 非匿名 owner不存在")
        void listReviews_nonAnonymous_ownerNull() {
            Review review = new Review();
            review.setId(1L);
            review.setOrderId(1L);
            review.setReviewerId(99L);
            review.setReviewerType("OWNER");
            review.setRating(new BigDecimal("4.0"));
            review.setContent("ok");
            review.setIsAnonymous(0);
            review.setPhotoUrls(null);
            review.setTags(null);
            review.setCreatedAt(LocalDateTime.now());

            when(reviewMapper.selectList(any())).thenReturn(List.of(review));
            when(ownerMapper.selectById(99L)).thenReturn(null);

            List<ReviewVO> vos = reviewService.listSitterReviews(2L);
            assertThat(vos.get(0).getReviewerNickname()).isNull();
        }

        private Sitter createSitter(Long id, String rating) {
            Sitter s = new Sitter();
            s.setId(id);
            s.setStatus("ACTIVE");
            s.setRating(new BigDecimal(rating));
            s.setTotalReviews(10);
            return s;
        }

        private Review createReview(String rating) {
            Review r = new Review();
            r.setRating(new BigDecimal(rating));
            return r;
        }
    }

    @Nested
    @DisplayName("PaymentService 分支覆盖")
    class PaymentBranches {

        @Mock private PaymentMapper paymentMapper;
        @Mock private ServiceOrderMapper orderMapper;

        @InjectMocks
        private PaymentServiceImpl paymentService;

        @Test
        @DisplayName("initiatePayment - 已有AUTHORIZED记录 - 拒绝")
        void initiate_existingAuthorized_throws() {
            ServiceOrder order = new ServiceOrder();
            order.setId(1L);
            order.setOwnerId(1L);
            order.setPaymentStatus("UNPAID");
            order.setTotalAmount(new BigDecimal("49.00"));
            when(orderMapper.selectById(1L)).thenReturn(order);

            Payment existing = new Payment();
            existing.setStatus("AUTHORIZED");
            when(paymentMapper.selectOne(any())).thenReturn(existing);

            PaymentInitDTO dto = new PaymentInitDTO();
            dto.setOrderId(1L);
            dto.setPaymentMethod("WECHAT");

            assertThatThrownBy(() -> paymentService.initiatePayment(1L, dto))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("预授权");
        }

        @Test
        @DisplayName("initiatePayment - 订单不存在 - 抛异常")
        void initiate_orderNotFound_throws() {
            when(orderMapper.selectById(99L)).thenReturn(null);

            PaymentInitDTO dto = new PaymentInitDTO();
            dto.setOrderId(99L);
            dto.setPaymentMethod("WECHAT");

            assertThatThrownBy(() -> paymentService.initiatePayment(1L, dto))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("订单不存在");
        }

        @Test
        @DisplayName("initiatePayment - 非订单主人 - 拒绝")
        void initiate_notOwner_throws() {
            ServiceOrder order = new ServiceOrder();
            order.setId(1L);
            order.setOwnerId(1L);
            order.setPaymentStatus("UNPAID");
            when(orderMapper.selectById(1L)).thenReturn(order);

            PaymentInitDTO dto = new PaymentInitDTO();
            dto.setOrderId(1L);
            dto.setPaymentMethod("WECHAT");

            assertThatThrownBy(() -> paymentService.initiatePayment(99L, dto))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("无权");
        }

        @Test
        @DisplayName("getByOrderId - 记录不存在 - 404")
        void getByOrderId_notFound_throws() {
            when(paymentMapper.selectOne(any())).thenReturn(null);

            assertThatThrownBy(() -> paymentService.getByOrderId(99L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("支付记录不存在");
        }

        @Test
        @DisplayName("getByOrderId - 记录存在 - 返回VO")
        void getByOrderId_found_returnsVO() {
            Payment payment = new Payment();
            payment.setId(1L);
            payment.setOrderId(1L);
            payment.setAmount(new BigDecimal("49.00"));
            payment.setPaymentMethod("WECHAT");
            payment.setStatus("AUTHORIZED");
            payment.setTransactionNo("TXN123");
            payment.setAuthorizedAt(LocalDateTime.now());
            when(paymentMapper.selectOne(any())).thenReturn(payment);

            PaymentVO vo = paymentService.getByOrderId(1L);
            assertThat(vo.getAmount()).isEqualTo(new BigDecimal("49.00"));
            assertThat(vo.getStatus()).isEqualTo("AUTHORIZED");
        }

        @Test
        @DisplayName("requestRefund - 支付记录不存在 - 抛异常")
        void refund_paymentNotFound_throws() {
            when(paymentMapper.selectOne(any())).thenReturn(null);

            RefundDTO dto = new RefundDTO();
            dto.setOrderId(99L);
            dto.setReason("取消");

            assertThatThrownBy(() -> paymentService.requestRefund(1L, dto))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("支付记录不存在");
        }

        @Test
        @DisplayName("requestRefund - 状态为REFUNDED - 不支持退款")
        void refund_alreadyRefunded_throws() {
            Payment payment = new Payment();
            payment.setOwnerId(1L);
            payment.setStatus("REFUNDED");
            when(paymentMapper.selectOne(any())).thenReturn(payment);

            RefundDTO dto = new RefundDTO();
            dto.setOrderId(1L);
            dto.setReason("again");

            assertThatThrownBy(() -> paymentService.requestRefund(1L, dto))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("不支持退款");
        }

        @Test
        @DisplayName("requestRefund - CAPTURED状态 - 可退款")
        void refund_capturedStatus_success() {
            Payment payment = new Payment();
            payment.setId(1L);
            payment.setOrderId(1L);
            payment.setOwnerId(1L);
            payment.setStatus("CAPTURED");
            payment.setAmount(new BigDecimal("49.00"));
            when(paymentMapper.selectOne(any())).thenReturn(payment);

            ServiceOrder order = new ServiceOrder();
            order.setId(1L);
            when(orderMapper.selectById(1L)).thenReturn(order);

            RefundDTO dto = new RefundDTO();
            dto.setOrderId(1L);
            dto.setReason("售后");

            paymentService.requestRefund(1L, dto);

            verify(paymentMapper).updateById(argThat((Payment p) ->
                    "REFUNDED".equals(p.getStatus())));
            verify(orderMapper).updateById(argThat((ServiceOrder o) ->
                    "REFUNDED".equals(o.getPaymentStatus())));
        }

        @Test
        @DisplayName("requestRefund - order为null - 不crash")
        void refund_orderNull_noCrash() {
            Payment payment = new Payment();
            payment.setId(1L);
            payment.setOrderId(1L);
            payment.setOwnerId(1L);
            payment.setStatus("AUTHORIZED");
            payment.setAmount(new BigDecimal("49.00"));
            when(paymentMapper.selectOne(any())).thenReturn(payment);
            when(orderMapper.selectById(1L)).thenReturn(null);

            RefundDTO dto = new RefundDTO();
            dto.setOrderId(1L);
            dto.setReason("test");

            paymentService.requestRefund(1L, dto);

            verify(paymentMapper).updateById(any(Payment.class));
            verify(orderMapper, never()).updateById(any(ServiceOrder.class));
        }

        @Test
        @DisplayName("capturePayment - 无支付记录 - 直接return")
        void capture_noPayment_noop() {
            when(paymentMapper.selectOne(any())).thenReturn(null);

            paymentService.capturePayment(99L);

            verify(paymentMapper, never()).updateById(any(Payment.class));
        }

        @Test
        @DisplayName("capturePayment - 状态非AUTHORIZED - 直接return")
        void capture_notAuthorized_noop() {
            Payment payment = new Payment();
            payment.setStatus("CAPTURED");
            when(paymentMapper.selectOne(any())).thenReturn(payment);

            paymentService.capturePayment(1L);

            verify(paymentMapper, never()).updateById(any(Payment.class));
        }

        @Test
        @DisplayName("capturePayment - AUTHORIZED状态 - 正常扣款")
        void capture_orderNull_noCrash() {
            Payment payment = new Payment();
            payment.setId(1L);
            payment.setOrderId(1L);
            payment.setStatus("AUTHORIZED");
            when(paymentMapper.selectOne(any())).thenReturn(payment);

            paymentService.capturePayment(1L);

            verify(paymentMapper).updateById(argThat((Payment p) ->
                    "CAPTURED".equals(p.getStatus())));
        }
    }
}
