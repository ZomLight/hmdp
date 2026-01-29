package com.hmdp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.entity.Voucher;
import com.hmdp.mapper.VoucherMapper;
import com.hmdp.service.IVoucherService;
import org.springframework.stereotype.Service;


@Service
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, Voucher> implements IVoucherService {

    @Override
    public Result queryVoucherOfShop(Long shopId) {
        return null;
    }

    @Override
    public void addSeckillVoucher(Voucher voucher) {

    }
}




