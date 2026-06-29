package com.petty.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petty.entity.Owner;
import com.petty.mapper.OwnerMapper;
import com.petty.service.OwnerService;
import org.springframework.stereotype.Service;

@Service
public class OwnerServiceImpl extends ServiceImpl<OwnerMapper, Owner> implements OwnerService {
}
