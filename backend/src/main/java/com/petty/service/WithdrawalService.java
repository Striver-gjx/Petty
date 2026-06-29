package com.petty.service;

import com.petty.dto.WithdrawalDTO;
import com.petty.vo.WithdrawalVO;
import java.util.List;

public interface WithdrawalService {
    void requestWithdrawal(Long sitterId, WithdrawalDTO dto);
    List<WithdrawalVO> listBySitter(Long sitterId);
}
