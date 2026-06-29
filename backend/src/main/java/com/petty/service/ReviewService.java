package com.petty.service;

import com.petty.dto.ReviewCreateDTO;
import com.petty.vo.ReviewVO;
import java.util.List;

public interface ReviewService {
    void createReview(Long reviewerId, String reviewerType, ReviewCreateDTO dto);
    List<ReviewVO> listSitterReviews(Long sitterId);
    void createAutoReview(Long orderId, Long ownerId, Long sitterId);
    boolean hasReview(Long orderId, Long reviewerId);
}
