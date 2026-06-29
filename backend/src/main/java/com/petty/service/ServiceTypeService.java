package com.petty.service;

import com.petty.vo.ServiceTypeVO;
import java.util.List;

public interface ServiceTypeService {
    List<ServiceTypeVO> listActive();
    ServiceTypeVO getById(Long id);
}
