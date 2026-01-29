package com.hmdp.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.VoucherOrderMapper;
import com.hmdp.service.IVoucherOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Override
    public Result seckillVoucher(Long voucherId) {
        return null;
    }

    @Override
    public void createVoucherOrder(VoucherOrder voucherOrder) {

    }

    @Override
    public void handleVoucherOrder(VoucherOrder voucherOrder) {

    }
}




