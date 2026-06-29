package com.petty.controller;

import com.petty.common.result.Result;
import com.petty.common.security.UserContext;
import com.petty.dto.WithdrawalDTO;
import com.petty.service.WithdrawalService;
import com.petty.vo.WithdrawalVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/withdrawals")
@RequiredArgsConstructor
public class WithdrawalController {

    private final WithdrawalService withdrawalService;

    @GetMapping
    public Result<List<WithdrawalVO>> list() {
        Long sitterId = UserContext.getUserId();
        return Result.success(withdrawalService.listBySitter(sitterId));
    }

    @PostMapping
    public Result<Void> request(@Valid @RequestBody WithdrawalDTO dto) {
        Long sitterId = UserContext.getUserId();
        withdrawalService.requestWithdrawal(sitterId, dto);
        return Result.success();
    }
}
