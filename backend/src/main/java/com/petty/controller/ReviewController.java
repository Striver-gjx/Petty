package com.petty.controller;

import com.petty.common.result.Result;
import com.petty.common.security.UserContext;
import com.petty.dto.ReviewCreateDTO;
import com.petty.service.ReviewService;
import com.petty.vo.ReviewVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public Result<Void> create(@Valid @RequestBody ReviewCreateDTO dto) {
        Long reviewerId = UserContext.getUserId();
        String role = UserContext.getRole();
        reviewService.createReview(reviewerId, role, dto);
        return Result.success();
    }

    @GetMapping("/sitter/{sitterId}")
    public Result<List<ReviewVO>> listSitterReviews(@PathVariable Long sitterId) {
        return Result.success(reviewService.listSitterReviews(sitterId));
    }
}
