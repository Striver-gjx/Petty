package com.petty.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petty.common.exception.BusinessException;
import com.petty.dto.ReviewCreateDTO;
import com.petty.entity.*;
import com.petty.enums.OrderStatus;
import com.petty.mapper.*;
import com.petty.service.ReviewService;
import com.petty.vo.ReviewVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewMapper reviewMapper;
    private final ServiceOrderMapper orderMapper;
    private final OwnerMapper ownerMapper;
    private final SitterMapper sitterMapper;

    @Override
    @Transactional
    public void createReview(Long reviewerId, String reviewerType, ReviewCreateDTO dto) {
        ServiceOrder order = orderMapper.selectById(dto.getOrderId());
        if (order == null) throw new BusinessException("订单不存在");

        if ("OWNER".equals(reviewerType) && !reviewerId.equals(order.getOwnerId())) {
            throw new BusinessException("无权评价此订单");
        }
        if ("SITTER".equals(reviewerType) && !reviewerId.equals(order.getSitterId())) {
            throw new BusinessException("无权评价此订单");
        }

        OrderStatus status = OrderStatus.valueOf(order.getStatus());
        if (status != OrderStatus.OWNER_CONFIRMED && status != OrderStatus.SERVICE_COMPLETED) {
            throw new BusinessException("订单未完成，不能评价");
        }

        Long existingCount = reviewMapper.selectCount(
                new LambdaQueryWrapper<Review>()
                        .eq(Review::getOrderId, dto.getOrderId())
                        .eq(Review::getReviewerId, reviewerId));
        if (existingCount > 0) {
            throw new BusinessException("已评价过该订单");
        }

        Long targetId = "OWNER".equals(reviewerType) ? order.getSitterId() : order.getOwnerId();
        if (targetId == null) throw new BusinessException("评价目标不存在");

        Review review = new Review();
        review.setOrderId(dto.getOrderId());
        review.setReviewerId(reviewerId);
        review.setReviewerType(reviewerType);
        review.setTargetId(targetId);
        review.setRating(dto.getRating());
        review.setContent(dto.getContent());
        review.setPhotoUrls(dto.getPhotoUrls() != null ? String.join(",", dto.getPhotoUrls()) : null);
        review.setTags(dto.getTags() != null ? String.join(",", dto.getTags()) : null);
        review.setIsAnonymous(Boolean.TRUE.equals(dto.getIsAnonymous()) ? 1 : 0);
        review.setStatus(1);
        review.setCreatedAt(java.time.LocalDateTime.now());
        reviewMapper.insert(review);

        if ("OWNER".equals(reviewerType)) {
            updateSitterRating(targetId);
        }

        log.info("评价创建成功: orderId={}, reviewer={}, rating={}", dto.getOrderId(), reviewerId, dto.getRating());
    }

    @Override
    public List<ReviewVO> listSitterReviews(Long sitterId) {
        List<Review> reviews = reviewMapper.selectList(
                new LambdaQueryWrapper<Review>()
                        .eq(Review::getTargetId, sitterId)
                        .eq(Review::getReviewerType, "OWNER")
                        .eq(Review::getStatus, 1)
                        .orderByDesc(Review::getCreatedAt));
        return reviews.stream().map(this::toVO).collect(Collectors.toList());
    }

    private void updateSitterRating(Long sitterId) {
        List<Review> reviews = reviewMapper.selectList(
                new LambdaQueryWrapper<Review>()
                        .eq(Review::getTargetId, sitterId)
                        .eq(Review::getReviewerType, "OWNER")
                        .eq(Review::getStatus, 1));

        if (reviews.isEmpty()) return;

        BigDecimal avg = reviews.stream()
                .map(Review::getRating)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(reviews.size()), 2, RoundingMode.HALF_UP);

        Sitter sitter = sitterMapper.selectById(sitterId);
        if (sitter != null) {
            sitter.setRating(avg);
            sitter.setTotalReviews(reviews.size());

            if (avg.compareTo(new BigDecimal("3.0")) < 0) {
                sitter.setStatus("BANNED");
                log.warn("喂养师 {} 评分低于3.0，强制下线", sitterId);
            } else if (avg.compareTo(new BigDecimal("3.5")) < 0) {
                sitter.setStatus("SUSPENDED");
                log.warn("喂养师 {} 评分低于3.5，暂停接单", sitterId);
            }
            sitterMapper.updateById(sitter);
        }
    }

    private ReviewVO toVO(Review review) {
        ReviewVO vo = new ReviewVO();
        vo.setId(review.getId());
        vo.setOrderId(review.getOrderId());
        vo.setReviewerType(review.getReviewerType());
        vo.setRating(review.getRating());
        vo.setContent(review.getContent());
        vo.setReplyContent(review.getReplyContent());
        vo.setCreatedAt(review.getCreatedAt());

        if (review.getPhotoUrls() != null && !review.getPhotoUrls().isEmpty()) {
            vo.setPhotoUrls(Arrays.asList(review.getPhotoUrls().split(",")));
        } else {
            vo.setPhotoUrls(Collections.emptyList());
        }
        if (review.getTags() != null && !review.getTags().isEmpty()) {
            vo.setTags(Arrays.asList(review.getTags().split(",")));
        } else {
            vo.setTags(Collections.emptyList());
        }

        if (review.getIsAnonymous() != null && review.getIsAnonymous() == 1) {
            vo.setReviewerNickname("匿名用户");
        } else {
            Owner owner = ownerMapper.selectById(review.getReviewerId());
            if (owner != null) {
                vo.setReviewerNickname(owner.getNickname());
                vo.setReviewerAvatar(owner.getAvatarUrl());
            }
        }
        return vo;
    }
}
